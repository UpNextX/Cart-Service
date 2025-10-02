package org.upnext.cartservice.Services;

import org.upnext.cartservice.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;

import java.util.List;

public interface CartService {
    CartDto getCartById(Long id);

    List<CartDto> getAllCarts();

    CartDto addItemToCart(Long id, CartItemRequest cartItemRequest);
}
