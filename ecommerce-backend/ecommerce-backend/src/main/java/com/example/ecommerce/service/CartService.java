package com.example.ecommerce.service;

import com.example.ecommerce.dto.Responses.CartView;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
  private final UserRepository users;
  private final CartRepository carts;
  private final ProductRepository products;
  private final ViewMapper views;

  public CartService(
      UserRepository users, CartRepository carts, ProductRepository products, ViewMapper views) {
    this.users = users;
    this.carts = carts;
    this.products = products;
    this.views = views;
  }

  // Le verrou utilisateur serialise toutes les mutations du panier d'un client.
  public Cart lockedCart(String email) {
    User u =
        users.lockByEmail(email).orElseThrow(() -> ApiException.missing("Compte introuvable."));
    return carts.findByUserId(u.id).orElseThrow(() -> ApiException.missing("Panier introuvable."));
  }

  public CartView get(String email) {
    return views.cart(lockedCart(email));
  }

  public CartView add(String email, Long productId, int quantity) {
    Cart c = lockedCart(email);
    Product p =
        products
            .findById(productId)
            .orElseThrow(() -> ApiException.missing("Produit introuvable."));
    CartItem item =
        c.items.stream().filter(i -> i.product.id.equals(productId)).findFirst().orElse(null);
    int next = quantity + (item == null ? 0 : item.quantity);
    check(p, next);
    if (item == null) {
      item = new CartItem();
      item.cart = c;
      item.product = p;
      c.items.add(item);
    }
    item.quantity = next;
    carts.saveAndFlush(c);
    return views.cart(c);
  }

  public CartView update(String email, Long itemId, int quantity) {
    Cart c = lockedCart(email);
    CartItem i = item(c, itemId);
    check(i.product, quantity);
    i.quantity = quantity;
    carts.saveAndFlush(c);
    return views.cart(c);
  }

  public void remove(String email, Long itemId) {
    Cart c = lockedCart(email);
    c.items.remove(item(c, itemId));
  }

  public void clear(String email) {
    lockedCart(email).items.clear();
  }

  private CartItem item(Cart c, Long id) {
    return c.items.stream()
        .filter(i -> i.id.equals(id))
        .findFirst()
        .orElseThrow(() -> ApiException.missing("Article introuvable dans votre panier."));
  }

  private void check(Product p, int quantity) {
    if (quantity < 1 || quantity > 1000) throw ApiException.conflict("Quantite invalide.");
    if (!p.active || quantity > p.stock)
      throw ApiException.conflict("Stock insuffisant ou produit indisponible.");
  }
}
