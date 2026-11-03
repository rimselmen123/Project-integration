package com.example.ecommerce.service;

import com.example.ecommerce.dto.Responses.*;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
  private final OrderRepository orders;
  private final ProductRepository products;
  private final CartService carts;
  private final AuthService auth;
  private final ViewMapper views;

  public OrderService(
      OrderRepository orders,
      ProductRepository products,
      CartService carts,
      AuthService auth,
      ViewMapper views) {
    this.orders = orders;
    this.products = products;
    this.carts = carts;
    this.auth = auth;
    this.views = views;
  }

  public OrderView create(String email, String address) {
    Cart c = carts.lockedCart(email);
    if (c.items.isEmpty()) throw ApiException.conflict("Le panier est vide.");
    // Ordre stable de verrouillage des produits : pas de double vente du stock.
    Map<Long, Product> locked = new HashMap<>();
    c.items.stream()
        .map(i -> i.product.id)
        .sorted()
        .forEach(
            id ->
                locked.put(
                    id,
                    products
                        .lockById(id)
                        .orElseThrow(() -> ApiException.missing("Produit introuvable."))));
    Order o = new Order();
    o.user = c.user;
    o.shippingAddress = address.trim();
    o.total = BigDecimal.ZERO;
    for (CartItem i : c.items) {
      Product p = locked.get(i.product.id);
      em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
      if (!p.active || p.stock < i.quantity)
        throw ApiException.conflict("Stock insuffisant pour " + p.name);
      p.stock -= i.quantity;
      OrderItem line = new OrderItem();
      line.order = o;
      line.productId = p.id;
      line.productName = p.name;
      line.price = p.price;
      line.quantity = i.quantity;
      o.items.add(line);
      o.total = o.total.add(p.price.multiply(BigDecimal.valueOf(i.quantity)));
    }
    orders.saveAndFlush(o);
    c.items.clear();
    return views.order(o);
  }

  public List<OrderView> mine(String email) {
    return orders.findByUserIdOrderByCreatedAtDesc(auth.current(email).id).stream()
        .map(views::order)
        .toList();
  }

  public List<OrderView> all() {
    return orders.findAllByOrderByCreatedAtDesc().stream().map(views::order).toList();
  }

  public OrderView detail(Long id, String email) {
    Order o = find(id);
    User u = auth.current(email);
    if (u.role != Role.ADMIN && !o.user.id.equals(u.id))
      throw new ApiException(HttpStatus.FORBIDDEN, "Cette commande ne vous appartient pas.");
    return views.order(o);
  }

  public OrderView status(Long id, OrderStatus status) {
    Order o = find(id);
    o.status = status;
    return views.order(o);
  }

  private Order find(Long id) {
    return orders.findById(id).orElseThrow(() -> ApiException.missing("Commande introuvable."));
  }
}
