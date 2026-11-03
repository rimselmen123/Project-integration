package com.example.ecommerce.security;

import com.example.ecommerce.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserRepository users;

  public JwtFilter(JwtService jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        users
            .findByEmail(jwt.subject(header.substring(7)))
            .ifPresent(
                u ->
                    SecurityContextHolder.getContext()
                        .setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                u.email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.role.name())))));
      } catch (JwtException | IllegalArgumentException e) {
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
