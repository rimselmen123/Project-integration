package com.example.ecommerce.dto;

import com.example.ecommerce.entity.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Responses {
  private Responses() {}

  public record UserView(Long id, String name, String email, Role role) {}

  public record Auth(String token, UserView user) {}

  public record CategoryView(Long id, String name, String description) {}

  public record ProductView(
      Long id,
      String name,
      String description,
      BigDecimal price,
      int stock,
      String imageUrl,
      Long categoryId,
      String categoryName,
      boolean active) {}

  public record CartItemView(
      Long id,
      Long productId,
      String productName,
      String imageUrl,
      BigDecimal price,
      int quantity,
      BigDecimal subtotal) {}

  public record CartView(Long id, List<CartItemView> items, BigDecimal total) {}

  public record OrderItemView(
      Long id,
      Long productId,
      String productName,
      BigDecimal price,
      int quantity,
      BigDecimal subtotal) {}

  public record OrderView(
      Long id,
      Long userId,
      String customerName,
      Instant createdAt,
      OrderStatus status,
      String shippingAddress,
      List<OrderItemView> items,
      BigDecimal total) {}
}
