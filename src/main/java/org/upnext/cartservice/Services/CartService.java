package org.upnext.cartservice.Services;

import org.upnext.cartservice.Dtos.CartDto;

import java.util.List;

public interface CartService {
    CartDto getCartById(Long id);

    List<CartDto> getAllCarts();
}
