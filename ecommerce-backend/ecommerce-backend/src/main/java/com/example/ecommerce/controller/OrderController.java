package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Requests.*;
import com.example.ecommerce.dto.Responses.OrderView;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderView create(Principal p, @Valid @RequestBody Checkout input) {
    return orders.create(p.getName(), input.shippingAddress());
  }

  @GetMapping
  public List<OrderView> mine(Principal p) {
    return orders.mine(p.getName());
  }

  @GetMapping("/all")
  public List<OrderView> all() {
    return orders.all();
  }

  @GetMapping("/{id}")
  public OrderView detail(Principal p, @PathVariable Long id) {
    return orders.detail(id, p.getName());
  }

  @PatchMapping("/{id}/status")
  public OrderView status(@PathVariable Long id, @Valid @RequestBody Status input) {
    return orders.status(id, input.status());
  }
}
