package com.example.ecommerce.service;

import com.example.ecommerce.dto.Responses.*;
import com.example.ecommerce.entity.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ViewMapper {
  public UserView user(User u) {
    return new UserView(u.id, u.name, u.email, u.role);
  }

  public CategoryView category(Category c) {
    return new CategoryView(c.id, c.name, c.description);
  }

  public ProductView product(Product p) {
    return new ProductView(
        p.id,
        p.name,
        p.description,
        p.price,
        p.stock,
        p.imageUrl,
        p.category.id,
        p.category.name,
        p.active);
  }

  public CartView cart(Cart c) {
    var items =
        c.items.stream()
            .map(
                i ->
                    new CartItemView(
                        i.id,
                        i.product.id,
                        i.product.name,
                        i.product.imageUrl,
                        i.product.price,
                        i.quantity,
                        i.product.price.multiply(BigDecimal.valueOf(i.quantity))))
            .toList();
    return new CartView(
        c.id,
        items,
        items.stream().map(CartItemView::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  public OrderView order(Order o) {
    return new OrderView(
        o.id,
        o.user.id,
        o.user.name,
        o.createdAt,
        o.status,
        o.shippingAddress,
        o.items.stream()
            .map(
                i ->
                    new OrderItemView(
                        i.id,
                        i.productId,
                        i.productName,
                        i.price,
                        i.quantity,
                        i.price.multiply(BigDecimal.valueOf(i.quantity))))
            .toList(),
        o.total);
  }
}
