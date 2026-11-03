package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCase(String name);
}
