package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Requests.CategoryInput;
import com.example.ecommerce.dto.Responses.CategoryView;
import com.example.ecommerce.service.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final CatalogService catalog;

  public CategoryController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  public List<CategoryView> list() {
    return catalog.categories();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryView create(@Valid @RequestBody CategoryInput input) {
    return catalog.saveCategory(null, input);
  }

  @PutMapping("/{id}")
  public CategoryView update(@PathVariable Long id, @Valid @RequestBody CategoryInput input) {
    return catalog.saveCategory(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    catalog.deleteCategory(id);
  }
}
