package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.email = :email")
  Optional<User> lockByEmail(@Param("email") String email);
}
