package com.example.abroad.respository;

import com.example.abroad.model.User.Role;
import com.example.abroad.model.User.Role.ID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, ID> {


  List<Role> findById_Username(String username);

}
