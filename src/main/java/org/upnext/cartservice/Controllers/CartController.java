package org.upnext.cartservice.Controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;
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

    @PostMapping("/{cartId}")
    public ResponseEntity<CartDto> addItemToCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest cartItemRequest){
        return ResponseEntity.ok(cartService.addItemToCart(cartId, cartItemRequest));
    }

    @PutMapping("/{cartId}")
    public  ResponseEntity<CartDto> updateItemCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest cartItemRequest){
        return ResponseEntity.ok(cartService.updateItemCart(cartId, cartItemRequest));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<CartDto> deleteItemCart(@PathVariable Long cartId, @RequestBody CartItemRequest cartItemRequest){
        return ResponseEntity.ok(cartService.deleteItemFromCart(cartId, cartItemRequest));
    }
}
