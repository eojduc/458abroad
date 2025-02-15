package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import jakarta.persistence.Id;


public sealed interface User extends Serializable {

  @Id
  String username();

  String displayName();

  String email();

  Role role();


  @Entity
  @Table(name = "local_users")
  final class LocalUser implements User {
    @Id
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private String displayName;

    public LocalUser() {
      this.username = null;
      this.password = null;
      this.email = null;
      this.role = null;
      this.displayName = null;
    }


    public LocalUser(String username, String password, String email, Role role, String displayName) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.role = role;
      this.displayName = displayName;
    }

    public String username() {
      return username;
    }
    public String password() {
      return password;
    }
    public String displayName() {
      return displayName;
    }
    public String email() {
      return email;
    }
    public Role role() {
      return role;
    }
  }

  @Entity
  @Table(name = "sso_users")
  final class SSOUser implements User {
    @Id
    private String username;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private String displayName;

    public SSOUser() {
      this.username = null;
      this.email = null;
      this.role = null;
      this.displayName = null;
    }

    public SSOUser(String username, String email, Role role, String displayName) {
      this.username = username;
      this.email = email;
      this.role = role;
      this.displayName = displayName;
    }

    public String username() {
      return username;
    }
    public String email() {
      return email;
    }
    public Role role() {
      return role;
    }
    public String displayName() {
      return displayName;
    }

  }


  enum Role {
    STUDENT,
    ADMIN
  }

}
