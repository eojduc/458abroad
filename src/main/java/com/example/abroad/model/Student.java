package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public final class Student implements User {

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

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String email() {
    return email;
  }

  @Override
  public Role role() {
    return Role.ROLE_STUDENT;
  }

  public String displayName() {
    return displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Student student = (Student) o;

    if (!username.equals(student.username)) {
      return false;
    }
    if (!password.equals(student.password)) {
      return false;
    }
    if (!email.equals(student.email)) {
      return false;
    }
    return displayName.equals(student.displayName);
  }
}
