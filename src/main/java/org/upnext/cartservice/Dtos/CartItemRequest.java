package org.upnext.cartservice.Dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    @NotNull(message = "Product Id must not be null")
    @Positive(message = "Product Id cannot be negative")
    Long id;

    @NotNull
    @Min(value = 1,message = "Quantity must be positive number")
    Integer quantity;

}
