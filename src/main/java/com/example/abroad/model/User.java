package com.example.abroad.model;

import java.io.Serializable;

public sealed interface User extends Serializable permits Admin, Student {

  String getUsername();

  String getPassword();

  String getDisplayName();

  String getEmail();

}
