package com.example.ecommerce.service;

import com.example.ecommerce.dto.Requests.*;
import com.example.ecommerce.dto.Responses.*;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.*;
import com.example.ecommerce.security.JwtService;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
  private final UserRepository users;
  private final CartRepository carts;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final ViewMapper views;

  public AuthService(
      UserRepository users,
      CartRepository carts,
      PasswordEncoder encoder,
      JwtService jwt,
      ViewMapper views) {
    this.users = users;
    this.carts = carts;
    this.encoder = encoder;
    this.jwt = jwt;
    this.views = views;
  }

  public Auth register(Register input) {
    String email = input.email().trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(email)) throw ApiException.conflict("Email deja inscrit.");
    User u = new User();
    u.name = input.name().trim();
    u.email = email;
    u.password = encoder.encode(input.password());
    users.saveAndFlush(u);
    Cart c = new Cart();
    c.user = u;
    carts.save(c);
    return new Auth(jwt.create(u.email), views.user(u));
  }

  public Auth login(Login input) {
    User u =
        users
            .findByEmail(input.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(
                () -> new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects."));
    if (!encoder.matches(input.password(), u.password))
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects.");
    return new Auth(jwt.create(u.email), views.user(u));
  }

  public User current(String email) {
    return users
        .findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Compte introuvable."));
  }

  public UserView me(String email) {
    return views.user(current(email));
  }
}
