package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "app_users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 80)
  public String name;

  @Column(nullable = false, unique = true, length = 150)
  public String email;

  @Column(nullable = false)
  public String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Role role = Role.CLIENT;
}
