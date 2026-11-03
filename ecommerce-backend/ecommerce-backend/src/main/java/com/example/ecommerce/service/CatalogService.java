package com.example.ecommerce.service;

import com.example.ecommerce.dto.Requests.*;
import com.example.ecommerce.dto.Responses.*;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogService {
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ViewMapper views;

  public CatalogService(
      ProductRepository products, CategoryRepository categories, ViewMapper views) {
    this.products = products;
    this.categories = categories;
    this.views = views;
  }

  public List<ProductView> products(boolean admin) {
    return (admin ? products.findAll() : products.findByActiveTrueOrderByIdAsc())
        .stream().map(views::product).toList();
  }

  public ProductView product(Long id, boolean admin) {
    Product p = findProduct(id);
    if (!admin && !p.active) throw ApiException.missing("Produit introuvable.");
    return views.product(p);
  }

  public Product findProduct(Long id) {
    return products.findById(id).orElseThrow(() -> ApiException.missing("Produit introuvable."));
  }

  private Category category(Long id) {
    return categories
        .findById(id)
        .orElseThrow(() -> ApiException.missing("Categorie introuvable."));
  }

  public ProductView saveProduct(Long id, ProductInput input) {
    Product p =
        id == null
            ? new Product()
            : products.lockById(id).orElseThrow(() -> ApiException.missing("Produit introuvable."));
    p.name = input.name().trim();
    p.description = input.description();
    p.price = input.price();
    p.stock = input.stock();
    p.imageUrl = input.imageUrl();
    p.category = category(input.categoryId());
    p.active = input.active();
    return views.product(products.saveAndFlush(p));
  }

  public void deleteProduct(Long id) {
    Product p =
        products.lockById(id).orElseThrow(() -> ApiException.missing("Produit introuvable."));
    p.active = false;
  }

  public List<CategoryView> categories() {
    return categories.findAll().stream().map(views::category).toList();
  }

  public CategoryView saveCategory(Long id, CategoryInput input) {
    Category c = id == null ? new Category() : category(id);
    c.name = input.name().trim();
    c.description = input.description();
    return views.category(categories.saveAndFlush(c));
  }

  public void deleteCategory(Long id) {
    Category c = category(id);
    if (products.existsByCategoryId(id))
      throw ApiException.conflict("Cette categorie contient des produits.");
    categories.delete(c);
  }
}
