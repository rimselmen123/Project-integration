package com.example.ecommerce.exception;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  public record ErrorBody(String message, Map<String, String> errors) {}

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorBody> api(ApiException e) {
    return ResponseEntity.status(e.status).body(new ErrorBody(e.getMessage(), Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest().body(new ErrorBody("Veuillez corriger les champs.", errors));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ErrorBody> json() {
    return ResponseEntity.badRequest().body(new ErrorBody("Requete JSON invalide.", Map.of()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorBody> conflict() {
    return ResponseEntity.status(409)
        .body(new ErrorBody("Cette valeur existe deja ou est utilisee.", Map.of()));
  }
}
