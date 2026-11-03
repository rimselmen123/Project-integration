package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true, length = 80)
  public String name;

  @Column(length = 500)
  public String description;
}
