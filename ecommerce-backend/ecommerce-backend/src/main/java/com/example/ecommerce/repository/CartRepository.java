package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Cart;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByUserId(Long userId);
}
