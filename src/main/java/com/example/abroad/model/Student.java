package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public final class Student extends User {

  @Id
  private String username;
  @Column(nullable = false)
  private String password;
  @Column(nullable = false)
  private String email;
  @Column(nullable = false)
  private String displayName;


  public Student() {
    this.username = null;
    this.password = null;
    this.email = null;
    this.displayName = null;
  }

  public Student(String username, String password, String email, String displayName) {
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
