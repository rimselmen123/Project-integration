package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Requests.*;
import com.example.ecommerce.dto.Responses.*;
import com.example.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Auth register(@Valid @RequestBody Register input) {
    return auth.register(input);
  }

  @PostMapping("/login")
  public Auth login(@Valid @RequestBody Login input) {
    return auth.login(input);
  }

  @GetMapping("/me")
  public UserView me(Principal p) {
    return auth.me(p.getName());
  }
}
