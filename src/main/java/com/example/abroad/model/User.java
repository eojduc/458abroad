package com.example.abroad.model;

import java.io.Serializable;

public sealed interface User extends Serializable permits Admin, Student {

  String username();

  String password();

  String displayName();

  String email();

  default boolean isAdmin() {
    return this instanceof Admin;
  }

}
