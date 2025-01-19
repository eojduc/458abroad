package com.example.abroad.model;

public sealed interface User permits Admin, Student {

  String getUsername();

  String getPassword();

  String getDisplayName();

  String getEmail();

}
