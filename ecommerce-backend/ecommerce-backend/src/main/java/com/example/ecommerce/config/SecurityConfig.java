package com.example.ecommerce.config;

import com.example.ecommerce.security.JwtFilter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http, JwtFilter jwt) throws Exception {
    return http.cors(c -> {})
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/api/auth/register", "/api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/admin")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.GET, "/api/products", "/api/products/*", "/api/categories")
                    .permitAll()
                    .requestMatchers("/api/products/**", "/api/categories/**", "/api/orders/all")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (q, r, x) -> {
                          r.setStatus(401);
                          r.setContentType("application/json;charset=UTF-8");
                          r.getWriter()
                              .write("{\"message\":\"Authentification requise.\",\"errors\":{}}");
                        })
                    .accessDeniedHandler(
                        (q, r, x) -> {
                          r.setStatus(403);
                          r.setContentType("application/json;charset=UTF-8");
                          r.getWriter().write("{\"message\":\"Acces refuse.\",\"errors\":{}}");
                        }))
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origins}") String origins) {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.asList(origins.split(",")));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/api/**", c);
    return s;
  }
}
