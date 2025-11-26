package org.upnext.cartservice.Services.Implementation;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.cartservice.Mapppers.CartMapper;
import org.upnext.cartservice.Clients.ProductsClient;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Models.CartItem;
import org.upnext.cartservice.Repositories.CartRepository;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Dtos.ProductDto;
import org.upnext.sharedlibrary.Errors.Error;
import org.upnext.sharedlibrary.Errors.Result;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.upnext.cartservice.Errors.CartErrors.CartNotFound;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductsClient productsClient;

    CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper, ProductsClient productsClient) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.productsClient = productsClient;
    }


    @Override
    public Result<List<CartDto>> getAllCarts() {
        List<Cart> allCarts = cartRepository.findAll();

        return Result.success(allCarts.stream()
                .map(cartMapper::toCartDto)
                .toList());
    }

    // returns the cart object by cartId
    private Optional<Cart> getCartObjectById(Long cartId) {

        return cartRepository.findById(cartId);
    }

    public Result<CartDto> getCartById(Long id) {
        return getCartObjectById(id)
                .map(
                        cart -> Result.success(cartMapper.toCartDto(cart))
                        )
                .orElseGet(()->Result.failure(CartNotFound));
    }

    private Cart createCart(Long userId){
        System.out.println("Creating cart for user: " + userId);
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    // returns the cart object by userId
    @Transactional
    public Cart getCartObjectByUserId(Long userId){
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if(optionalCart.isPresent()){
            return optionalCart.get();
        }else{
            try {
                return createCart(userId);
            } catch (DataIntegrityViolationException e) {
                return cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("System Error: Cart creation failed but cart not found."));
            }
        }
    }

    @Override
    @Transactional
    public Result<CartDto> getCartByUserId(Long userId) {
        return Result.success(cartMapper.toCartDto(getCartObjectByUserId(userId)));
    }


    @Override
    @Transactional
    public Result<URI> addItemToCart(Long userId, CartItemRequest cartItemRequest, UriComponentsBuilder urb) {
        Cart cart = getCartObjectByUserId(userId);

        ProductDto productDto = productsClient.getProduct(cartItemRequest.getProductId());

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            return Result.failure(new Error("ALREADY.EXISTS", "item already exists in the cart", 409));
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(cartItemRequest.getProductId());
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }
        cartRepository.save(cart);

        URI uri = urb.
                path("/carts/me")
                .buildAndExpand()
                .toUri();
        return Result.success(uri);
    }

    @Override
    @Transactional
    public Result<Void> updateItemCart(Long userId, CartItemRequest cartItemRequest) {

        Cart cart = getCartObjectByUserId(userId);

        ProductDto productDto = productsClient.getProduct(cartItemRequest.getProductId());

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(cartItemRequest.getQuantity());
        } else {
            return Result.failure(new Error(
                    "Product_NOT_FOUND",
                    String.format("Product with id %d not found in the cart!", cartItemRequest.getProductId()),
                    404
            ));
        }
        cartRepository.save(cart);

        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deleteItemFromCart(Long userId, Long id) {

        Cart cart = getCartObjectByUserId(userId);


        Optional<CartItem> cartItemOptional = cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(id))
                .findFirst();

        if(cartItemOptional.isEmpty()){
            return Result.failure(new Error(
                    "Product_NOT_FOUND",
                    String.format("Product with id %d not found in the cart!", id),
                    404
            ));
        }
        CartItem cartItem = cartItemOptional.get();

        cart.getItems().remove(cartItem);
        cartRepository.save(cart);

        return Result.success();

    }

    @Override
    @Transactional
    public Result<Void> clearCart(Long userId) {
        Cart cart = getCartObjectByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return Result.success();
    }

    public Double getTotalCost(Cart cart) {
        return cart.getItems()
                .stream()
                .mapToDouble(item -> {
                    Double price = productsClient
                            .getProduct(item.getProductId())
                            .getPrice();
                    return price * item.getQuantity();
                }).sum();
    }
}
