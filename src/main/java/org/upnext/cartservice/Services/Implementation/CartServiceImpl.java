package org.upnext.cartservice.Services.Implementation;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.cartservice.Mapppers.CartMapper;
import org.upnext.cartservice.Clients.ProductsClient;
import org.upnext.cartservice.Dtos.CartDto;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Models.CartItem;
import org.upnext.cartservice.Repositories.CartRepository;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Dtos.ProductDto;
import org.upnext.sharedlibrary.Exceptions.NotFoundException;

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

    @Override
    public CartDto getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Cart with id %d not found!", id)));

        return cartMapper.toCartDto(cart);
    }

    @Override
    public List<CartDto> getAllCarts() {
        List<Cart> allCarts = cartRepository.findAll();

        return allCarts.stream()
                .map(cartMapper::toCartDto)
                .toList();
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Long id, CartItemRequest cartItemRequest) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Cart with id %d not found!", id)));

        ProductDto productDto = productsClient.getProduct(cartItemRequest.getProductId());

        Optional< CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                .findFirst();

        if(existingItemOpt.isPresent()){
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(cartItemRequest.getQuantity());
        }else{
            CartItem cartItem = new CartItem();
            cartItem.setProductId(cartItemRequest.getProductId());
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }

        return cartMapper.toCartDto(cartRepository.save(cart));
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
