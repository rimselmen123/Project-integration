package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "customer_orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  public User user;

  @Column(nullable = false)
  public Instant createdAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public OrderStatus status = OrderStatus.EN_ATTENTE;

  @Column(nullable = false, length = 500)
  public String shippingAddress;

  @Column(nullable = false, precision = 19, scale = 2)
  public BigDecimal total;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id")
  public List<OrderItem> items = new ArrayList<>();
}
