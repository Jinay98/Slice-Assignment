package com.slice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new Item.
 *
 * Uses Bean Validation annotations:
 *  @NotBlank   — field must not be null or blank string
 *  @Size       — min/max length
 *  @DecimalMin — numeric lower bound
 *
 * INTERVIEW TIP: Design DTOs to be strict about inputs.
 * Never let invalid data reach the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
}
