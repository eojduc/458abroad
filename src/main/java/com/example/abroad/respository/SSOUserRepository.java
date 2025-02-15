package com.example.abroad.respository;

import com.example.abroad.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SSOUserRepository extends JpaRepository<User.SSOUser, String> {

  Optional<User.SSOUser> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

}
