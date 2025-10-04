package org.upnext.cartservice.Services.Implementation;

import jakarta.transaction.Transactional;
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

    private Optional<Cart> getCartObjectById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    public Result<CartDto> getCartById(Long id) {
        return getCartObjectById(id)
                .map(
                        cart -> Result.success(cartMapper.toCartDto(cart))
                        )
                .orElse(Result.failure(CartNotFound));
    }


    @Override
    public Result<List<CartDto>> getAllCarts() {
        List<Cart> allCarts = cartRepository.findAll();

        return Result.success(allCarts.stream()
                .map(cartMapper::toCartDto)
                .toList());
    }


    @Override
    @Transactional
    public Result<URI> addItemToCart(Long cartId, CartItemRequest cartItemRequest, UriComponentsBuilder urb) {
        Optional<Cart> cartOpt = getCartObjectById(cartId);
        if(cartOpt.isEmpty()){
            return Result.failure(CartNotFound);
        }

        Cart cart = cartOpt.get();

        ProductDto productDto = productsClient.getProduct(cartItemRequest.getProductId());

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(cartItemRequest.getQuantity());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(cartItemRequest.getProductId());
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }
        cartRepository.save(cart);

        URI uri = urb.
                path("/carts/{cartid}")
                .buildAndExpand(cartId)
                .toUri();
        return Result.success(uri);
    }

    @Override
    @Transactional
    public Result<Void> updateItemCart(Long cartId, CartItemRequest cartItemRequest) {
        Optional<Cart> cartOpt = getCartObjectById(cartId);
        if(cartOpt.isEmpty()){
            return Result.failure(CartNotFound);
        }

        Cart cart = cartOpt.get();

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
    public Result<Void> deleteItemFromCart(Long cartId, CartItemRequest cartItemRequest) {
        Optional<Cart> cartOpt = getCartObjectById(cartId);
        if(cartOpt.isEmpty()){
            return Result.failure(CartNotFound);
        }

        Cart cart = cartOpt.get();

        Optional<CartItem> cartItemOptional = cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if(cartItemOptional.isEmpty()){
            return Result.failure(new Error(
                    "Product_NOT_FOUND",
                    String.format("Product with id %d not found in the cart!", cartItemRequest.getProductId()),
                    404
            ));
        }
        CartItem cartItem = cartItemOptional.get();

        cart.getItems().remove(cartItem);
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
