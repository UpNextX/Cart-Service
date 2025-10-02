package org.upnext.cartservice.Services.Implementation;

import org.springframework.stereotype.Service;
import org.upnext.cartservice.Mapppers.CartMapper;
import org.upnext.cartservice.Clients.ProductsClient;
import org.upnext.cartservice.Dtos.CartDto;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Repositories.CartRepository;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Exceptions.NotFoundException;

import java.util.List;

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
