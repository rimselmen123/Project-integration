package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Cart {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @OneToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(unique = true)
  public User user;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id")
  public List<CartItem> items = new ArrayList<>();
}
