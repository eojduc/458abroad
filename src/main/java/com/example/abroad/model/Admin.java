package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "admins")
@Entity
public final class Admin extends User {

  @Id
  private String username;
  @Column(nullable = false)
  private String password;
  @Column(nullable = false)
  private String email;
  @Column(nullable = false)
  private String displayName;


  public Admin() {
    this.username = null;
    this.password = null;
    this.email = null;
    this.displayName = null;
  }

  public Admin(String username, String password, String email, String displayName) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.displayName = displayName;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }
}
