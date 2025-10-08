package org.upnext.cartservice.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping
    public ResponseEntity<?> getAllCarts(HttpServletRequest request) {
        System.out.println("Getting all carts");
        UserDto user = (UserDto) UserExtractor.userExtractor(request);

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

    @GetMapping("/me")
    public ResponseEntity<?> getCartById(HttpServletRequest request) {
        UserDto user = (UserDto) UserExtractor.userExtractor(request);
        System.out.println("USER" + user);
        if(user == null) {
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

    @PostMapping("/me")
    public ResponseEntity<?> addItemToCart(HttpServletRequest request, @Valid @RequestBody CartItemRequest cartItemRequest
            , UriComponentsBuilder urb) {
        UserDto user = (UserDto) UserExtractor.userExtractor(request);


        Result<URI> result = cartService.addItemToCart(user.getId(), cartItemRequest, urb);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.created(result.getValue()).build();
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateItemCart(HttpServletRequest request, @Valid @RequestBody CartItemRequest cartItemRequest) {
        UserDto user = (UserDto) UserExtractor.userExtractor(request);

        Result<Void> result = cartService.updateItemCart(user.getId(), cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteItemCart(HttpServletRequest request, @Valid @RequestBody CartItemRequest cartItemRequest) {
        UserDto user = (UserDto) UserExtractor.userExtractor(request);

        Result<Void> result = cartService.deleteItemFromCart(user.getId(), cartItemRequest);
        if (result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        UserDto user = (UserDto) UserExtractor.userExtractor(request);
        Result<Void> result = cartService.clearCart(user.getId());
        if(result.getIsFailure()) {
            return ResponseEntity
                    .status(result.getError().getStatusCode())
                    .body(result.getError());
        }
        return ResponseEntity.noContent().build();
    }
}
