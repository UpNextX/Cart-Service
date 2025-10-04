package org.upnext.cartservice.Controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Dtos.UserDto;
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
    public ResponseEntity<?> getAllCarts(@AuthenticationPrincipal UserDto userDto) {
        if(userDto.getRole() != "ADMIN"){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Result<List<CartDto>> result = cartService.getAllCarts();
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCartById(@AuthenticationPrincipal UserDto userDto) {
        Result<CartDto> result = cartService.getCartByUserId(userDto.getId());
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PostMapping("/me")
    public ResponseEntity<?> addItemToCart(@AuthenticationPrincipal UserDto userDto, @Valid @RequestBody CartItemRequest cartItemRequest
            , UriComponentsBuilder urb) {

        Result<URI> result = cartService.addItemToCart(userDto.getId(), cartItemRequest, urb);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.created(result.getValue()).build();
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateItemCart(@AuthenticationPrincipal UserDto userDto, @Valid @RequestBody CartItemRequest cartItemRequest) {
        Result<Void> result = cartService.updateItemCart(userDto.getId(), cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteItemCart(@AuthenticationPrincipal UserDto userDto, @RequestBody CartItemRequest cartItemRequest) {
        Result<Void> result = cartService.deleteItemFromCart(userDto.getId(), cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }
}
