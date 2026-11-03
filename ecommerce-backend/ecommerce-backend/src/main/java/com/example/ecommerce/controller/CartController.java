package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Requests.*;
import com.example.ecommerce.dto.Responses.CartView;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private final CartService carts;

  public CartController(CartService carts) {
    this.carts = carts;
  }

  @GetMapping
  public CartView get(Principal p) {
    return carts.get(p.getName());
  }

  @PostMapping("/items")
  public CartView add(Principal p, @Valid @RequestBody AddItem input) {
    return carts.add(p.getName(), input.productId(), input.quantity());
  }

  @PutMapping("/items/{id}")
  public CartView update(Principal p, @PathVariable Long id, @Valid @RequestBody Quantity input) {
    return carts.update(p.getName(), id, input.quantity());
  }

  @DeleteMapping("/items/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(Principal p, @PathVariable Long id) {
    carts.remove(p.getName(), id);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(Principal p) {
    carts.clear(p.getName());
  }
}
