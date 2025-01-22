package com.example.abroad.respository;

import com.example.abroad.model.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {

  Optional<Admin> findByUsername(String username);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);


}
