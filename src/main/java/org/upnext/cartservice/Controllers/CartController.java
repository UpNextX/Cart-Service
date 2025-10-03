package org.upnext.cartservice.Controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Errors.Result;

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
    public ResponseEntity<?> getAllCarts(){
        Result<List<CartDto>> result = cartService.getAllCarts();
        System.out.println("LOL");
        if(result.getIsFailure()){
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id){
        Result<CartDto> result = cartService.getCartById(id);
        if(result.getIsFailure()){
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PostMapping("/{cartId}")
    public ResponseEntity<?> addItemToCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest cartItemRequest){
        Result<CartDto> result = cartService.addItemToCart(cartId, cartItemRequest);
        if(result.getIsFailure()){
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PutMapping("/{cartId}")
    public  ResponseEntity<?> updateItemCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest cartItemRequest){
        Result<CartDto> result = cartService.updateItemCart(cartId, cartItemRequest);
        if(result.getIsFailure()){
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> deleteItemCart(@PathVariable Long cartId, @RequestBody CartItemRequest cartItemRequest){
        Result<CartDto> result = cartService.deleteItemFromCart(cartId, cartItemRequest);
        if(result.getIsFailure()){
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }
}
