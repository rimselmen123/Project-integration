package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Requests.ProductInput;
import com.example.ecommerce.dto.Responses.ProductView;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.service.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final CatalogService catalog;
  private final AuthService auth;

  public ProductController(CatalogService catalog, AuthService auth) {
    this.catalog = catalog;
    this.auth = auth;
  }

  @GetMapping
  public List<ProductView> list() {
    return catalog.products(false);
  }

  @GetMapping("/admin")
  public List<ProductView> admin() {
    return catalog.products(true);
  }

  @GetMapping("/{id}")
  public ProductView detail(@PathVariable Long id, Principal p) {
    return catalog.product(id, p != null && auth.current(p.getName()).role == Role.ADMIN);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductView create(@Valid @RequestBody ProductInput input) {
    return catalog.saveProduct(null, input);
  }

  @PutMapping("/{id}")
  public ProductView update(@PathVariable Long id, @Valid @RequestBody ProductInput input) {
    return catalog.saveProduct(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    catalog.deleteProduct(id);
  }
}
