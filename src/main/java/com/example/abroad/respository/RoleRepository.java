package com.example.abroad.respository;

import com.example.abroad.model.User.Role;
import com.example.abroad.model.User.Role.ID;
import com.example.abroad.model.User.Role.Type;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, ID> {


  List<Role> findById_Username(String username);
  Optional<Role> findById_UsernameAndId_Type(String username, Type type);
  List<Role> findById_Type(Type type);


}
