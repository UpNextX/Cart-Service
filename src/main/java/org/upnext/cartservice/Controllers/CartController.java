package org.upnext.cartservice.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.cartservice.Utils.UserExtractor;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllCarts(@AuthenticationPrincipal UserDto user) {
        System.out.println("Getting all carts");

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: missing or invalid user header");
        }

        Result<List<CartDto>> result = cartService.getAllCarts();
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<?> getCartById(@AuthenticationPrincipal UserDto user) {
        System.out.println("USER" + user);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Result<CartDto> result = cartService.getCartByUserId(user.getId());
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me")
    public ResponseEntity<?> addItemToCart(@AuthenticationPrincipal UserDto user, @Valid @RequestBody CartItemRequest cartItemRequest
            , UriComponentsBuilder urb) {


        Result<URI> result = cartService.addItemToCart(user.getId(), cartItemRequest, urb);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.created(result.getValue()).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<?> updateItemCart(@AuthenticationPrincipal UserDto user, @Valid @RequestBody CartItemRequest cartItemRequest) {

        Result<Void> result = cartService.updateItemCart(user.getId(), cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me/{id}")
    public ResponseEntity<?> deleteItemCart(@AuthenticationPrincipal UserDto user, @PathVariable("id") Long id) {

        Result<Void> result = cartService.deleteItemFromCart(user.getId(), id);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDto user) {
        Result<Void> result = cartService.clearCart(user.getId());
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }
}
