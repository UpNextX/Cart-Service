package org.upnext.cartservice.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    Long id;
    Long userId;
    List<CartItemDto> items;
    Double totalCost;

}
