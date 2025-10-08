package org.upnext.cartservice.Services;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;

import org.upnext.sharedlibrary.Errors.Result;

import java.net.URI;
import java.util.List;

public interface CartService {
    Result<CartDto> getCartById(Long id);

    Result<List<CartDto>> getAllCarts();

    Result<CartDto> getCartByUserId(Long userId);

    Result<URI> addItemToCart(Long userId, CartItemRequest cartItemRequest, UriComponentsBuilder urb);

    Result<Void> updateItemCart(Long userId, CartItemRequest cartItemRequest);

    Result<Void> deleteItemFromCart(Long userId, CartItemRequest cartItemRequest);

    Result<Void> clearCart(Long userId);
}
