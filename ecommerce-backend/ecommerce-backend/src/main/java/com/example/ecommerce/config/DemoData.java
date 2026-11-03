package com.example.ecommerce.config;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.*;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed-enabled", havingValue = "true")
public class DemoData implements CommandLineRunner {
  private final UserRepository users;
  private final CartRepository carts;
  private final CategoryRepository categories;
  private final ProductRepository products;
  private final PasswordEncoder encoder;
  private final String password;

  public DemoData(
      UserRepository users,
      CartRepository carts,
      CategoryRepository categories,
      ProductRepository products,
      PasswordEncoder encoder,
      @Value("${app.demo-password}") String password) {
    this.users = users;
    this.carts = carts;
    this.categories = categories;
    this.products = products;
    this.encoder = encoder;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    account("admin@demo.com", "Administrateur", Role.ADMIN);
    account("client@demo.com", "Alex Martin", Role.CLIENT);
    if (categories.count() > 0 || products.count() > 0) return;
    Category tech = category("Tech & accessoires", "Les essentiels de votre quotidien.");
    Category home = category("Maison", "Un espace qui vous ressemble.");
    Category style = category("Style & lifestyle", "Simple, utile et intemporel.");
    product(
        "Casque sans fil",
        "Un son clair et une autonomie de 30 heures pour etudier et se detendre.",
        "39.90",
        30,
        "/assets/headphones.svg",
        tech);
    product(
        "Lampe de bureau",
        "Une lumiere douce et orientable pour votre espace de travail.",
        "24.90",
        25,
        "/assets/lamp.svg",
        home);
    product(
        "Sac urbain",
        "Un sac leger avec compartiment ordinateur et poches pratiques.",
        "49.00",
        20,
        "/assets/bag.svg",
        style);
    product(
        "Gourde isotherme",
        "Votre boisson reste fraiche ou chaude toute la journee.",
        "19.90",
        40,
        "/assets/bottle.svg",
        style);
    product(
        "Clavier compact",
        "Un clavier confortable au format compact pour votre bureau.",
        "59.00",
        15,
        "/assets/keyboard.svg",
        tech);
    product(
        "Carnet premium",
        "Des pages creme et une couverture solide pour vos idees.",
        "12.50",
        50,
        "/assets/notebook.svg",
        home);
  }

  private void account(String email, String name, Role role) {
    if (users.existsByEmail(email)) return;
    User u = new User();
    u.email = email;
    u.name = name;
    u.role = role;
    u.password = encoder.encode(password);
    users.save(u);
    Cart c = new Cart();
    c.user = u;
    carts.save(c);
  }

  private Category category(String name, String description) {
    Category c = new Category();
    c.name = name;
    c.description = description;
    return categories.save(c);
  }

  private void product(
      String name, String description, String price, int stock, String image, Category category) {
    Product p = new Product();
    p.name = name;
    p.description = description;
    p.price = new BigDecimal(price);
    p.stock = stock;
    p.imageUrl = image;
    p.category = category;
    products.save(p);
  }
}
