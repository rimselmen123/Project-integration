package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  public Order order;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false, length = 120)
  public String productName;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal price;

  @Column(nullable = false)
  public int quantity;
}
