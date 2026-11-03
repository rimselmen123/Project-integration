package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  public Cart cart;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  public Product product;

  @Column(nullable = false)
  public int quantity;
}
