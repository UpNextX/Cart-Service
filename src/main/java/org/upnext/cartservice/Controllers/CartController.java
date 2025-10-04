package org.upnext.cartservice.Controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Errors.Result;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("carts")
public class CartController {
    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // For admin
    @GetMapping
    public ResponseEntity<?> getAllCarts() {
        Result<List<CartDto>> result = cartService.getAllCarts();
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        Result<CartDto> result = cartService.getCartById(id);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PostMapping("/{cartId}")
    public ResponseEntity<?> addItemToCart(@PathVariable Long cartId,
                                           @Valid @RequestBody CartItemRequest cartItemRequest
            , UriComponentsBuilder urb) {

        Result<URI> result = cartService.addItemToCart(cartId, cartItemRequest, urb);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.created(result.getValue()).build();
    }

    @PutMapping("/{cartId}")
    public ResponseEntity<?> updateItemCart(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest cartItemRequest) {
        Result<Void> result = cartService.updateItemCart(cartId, cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> deleteItemCart(@PathVariable Long cartId, @RequestBody CartItemRequest cartItemRequest) {
        Result<Void> result = cartService.deleteItemFromCart(cartId, cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }
}
