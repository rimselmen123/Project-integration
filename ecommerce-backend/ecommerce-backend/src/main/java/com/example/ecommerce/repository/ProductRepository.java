package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  List<Product> findByActiveTrueOrderByIdAsc();

  boolean existsByCategoryId(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> lockById(@Param("id") Long id);
}
