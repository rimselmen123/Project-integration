package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<Order> findAllByOrderByCreatedAtDesc();
}
