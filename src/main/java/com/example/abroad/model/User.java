package com.example.abroad.model;

import java.io.Serializable;
import jakarta.persistence.Id;


public sealed interface User extends Serializable permits Admin, Student {

  @Id
  String username();



  String password();

  String displayName();

  String email();

  Role role();

  default boolean isAdmin() {
    return role() == Role.ROLE_ADMIN;
  }

}
