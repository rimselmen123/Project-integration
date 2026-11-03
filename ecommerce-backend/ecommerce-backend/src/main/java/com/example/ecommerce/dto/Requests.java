package com.example.ecommerce.dto;

import com.example.ecommerce.entity.OrderStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class Requests {
  private Requests() {}

  public record Register(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @Email @Size(max = 150) String email,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}

  public record CategoryInput(
      @NotBlank @Size(max = 80) String name, @Size(max = 500) String description) {}

  public record ProductInput(
      @NotBlank @Size(max = 120) String name,
      @Size(max = 2000) String description,
      @NotNull @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2)
          BigDecimal price,
      @Min(0) @Max(1000000) int stock,
      @Size(max = 1000)
          @Pattern(
              regexp = "^(https?://[^\\s]+|/[^\\s]*|)$",
              message = "URL HTTP(S) ou chemin local attendu")
          String imageUrl,
      @NotNull Long categoryId,
      boolean active) {}

  public record AddItem(@NotNull Long productId, @Min(1) @Max(1000) int quantity) {}

  public record Quantity(@Min(1) @Max(1000) int quantity) {}

  public record Checkout(@NotBlank @Size(min = 10, max = 500) String shippingAddress) {}

  public record Status(@NotNull OrderStatus status) {}
}
