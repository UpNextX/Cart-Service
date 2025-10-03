package org.upnext.cartservice.Services;

import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;

import java.util.List;

public interface CartService {
    CartDto getCartById(Long id);

    List<CartDto> getAllCarts();

    CartDto addItemToCart(Long cartid, CartItemRequest cartItemRequest);

    CartDto updateItemCart(Long cartId, CartItemRequest cartItemRequest);

    CartDto deleteItemFromCart(Long cartId, CartItemRequest cartItemRequest);
}
