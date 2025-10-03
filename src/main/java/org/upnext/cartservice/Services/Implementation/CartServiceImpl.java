package org.upnext.cartservice.Services.Implementation;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
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

import java.util.List;
import java.util.Optional;

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
                .orElse(Result.failure(new Error(
                        "CART_NOT_FOUND",
                        String.format("Cart with id %d not found!", id),
                        404
                )));
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
    public Result<CartDto> addItemToCart(Long cartId, CartItemRequest cartItemRequest) {
        Optional<Cart> cartOpt = getCartObjectById(cartId);
        if(cartOpt.isEmpty()){
            return Result.failure(new Error(
                    "CART_NOT_FOUND",
                    String.format("Cart with id %d not found!", cartId),
                    404
            ));
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

        return Result.success(cartMapper.toCartDto(cartRepository.save(cart)));
    }

    @Override
    @Transactional
    public Result<CartDto> updateItemCart(Long cartId, CartItemRequest cartItemRequest) {
        return addItemToCart(cartId, cartItemRequest);
    }

    @Override
    @Transactional
    public Result<CartDto> deleteItemFromCart(Long cartId, CartItemRequest cartItemRequest) {
        Optional<Cart> cartOpt = getCartObjectById(cartId);
        if(cartOpt.isEmpty()){
            return Result.failure(new Error(
                    "CART_NOT_FOUND",
                    String.format("Cart with id %d not found!", cartId),
                    404
            ));
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

        return Result.success(cartMapper.toCartDto(cartRepository.save(cart)));


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
