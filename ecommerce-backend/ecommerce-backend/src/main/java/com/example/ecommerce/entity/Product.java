package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 120)
  public String name;

  @Column(length = 2000)
  public String description;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal price;

  @Column(nullable = false)
  public int stock;

  @Column(length = 1000)
  public String imageUrl;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  public Category category;

  @Column(nullable = false)
  public boolean active = true;
}
