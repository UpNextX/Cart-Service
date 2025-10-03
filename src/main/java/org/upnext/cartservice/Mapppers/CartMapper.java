package org.upnext.cartservice.Mapppers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.upnext.cartservice.Clients.ProductsClient;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.sharedlibrary.Dtos.CartItemDto;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Models.CartItem;
import org.upnext.sharedlibrary.Dtos.ProductDto;

@Component
public class CartMapper {
    private final ProductsClient productsClient;
    public CartMapper(ProductsClient productsClient){
        this.productsClient = productsClient;
    }
    public CartDto toCartDto(Cart cart){
        CartDto cartDto = CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(
                        cart.getItems()
                                .stream()
                                .map(this::toCartItemDto)
                                .toList()
                )
                .build();

        cartDto.setTotalCost(
                cartDto.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum()
        );
        return cartDto;
    };

    public CartItemDto toCartItemDto(CartItem cartItem){
        ProductDto productDto = productsClient.getProduct(cartItem.getProductId());
        return CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .price(productDto.getPrice())
                .productName(productDto.getName())
                .productImageUrl(productDto.getImageUrl())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
