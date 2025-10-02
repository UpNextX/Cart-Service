package org.upnext.cartservice.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upnext.cartservice.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemDto;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Services.CartService;

import java.util.List;

@RestController
@RequestMapping("carts")
public class CartController {
    private final CartService cartService;

    CartController(CartService cartService){
        this.cartService = cartService;
    }

    // For admin
    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts(){
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDto> getCartById(@PathVariable Long id){
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @PostMapping
    public CartDto addItemToCart(@RequestBody CartItemDto cartItemDto){
        return null;
    }
}
