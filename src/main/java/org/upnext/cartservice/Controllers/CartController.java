package org.upnext.cartservice.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Cart Management",
        description = "APIs for managing user carts, including items, totals, and checkout."
)
public class CartController {
    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // For admin
    @Operation(
            summary = "Get all cart for admins only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all carts",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden — user lacks admin privileges")
    })
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


    @Operation(
            summary = "Get the current user's cart"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user cart",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — user not logged in"),
            @ApiResponse(responseCode = "404", description = "Cart not found for this user")
    })

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
    @Operation(
            summary = "Add item to user's cart"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item successfully added to cart"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID or quantity or item already exists in the cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — user not logged in")
    })
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

    @Operation(
            summary = "Update item in user's cart"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or missing product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — user not logged in")
    })
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

    @Operation(
            summary = "Delete a specific item from user's cart",
            description = "Removes a specific product from the authenticated user's cart by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — user not logged in"),
            @ApiResponse(responseCode = "404", description = "Item not found in cart")
    })
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

    @Operation(
            summary = "Clear user's cart",
            description = "Removes all items from the authenticated user's cart."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart successfully cleared"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — user not logged in")
    })
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
