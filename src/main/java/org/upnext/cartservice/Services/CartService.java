package org.upnext.cartservice.Services;

import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;

import org.upnext.sharedlibrary.Errors.Result;
import java.util.List;

public interface CartService {
    Result<CartDto> getCartById(Long id);

    Result<List<CartDto>> getAllCarts();

    Result<CartDto> addItemToCart(Long cartid, CartItemRequest cartItemRequest);

    Result<CartDto> updateItemCart(Long cartId, CartItemRequest cartItemRequest);

    Result<CartDto> deleteItemFromCart(Long cartId, CartItemRequest cartItemRequest);
}
