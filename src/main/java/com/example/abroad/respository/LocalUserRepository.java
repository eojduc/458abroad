package com.example.abroad.respository;

import com.example.abroad.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalUserRepository extends JpaRepository<User.LocalUser, String> {

  Optional<User.LocalUser> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

}
