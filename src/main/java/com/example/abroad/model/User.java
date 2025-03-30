package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.io.Serializable;
import jakarta.persistence.Id;
import java.util.Objects;


public sealed interface User extends Serializable {

  @Id
  String username();

  String displayName();

  String email();


  Theme theme();

  User withTheme(Theme theme);

  default boolean isLocal() {
    return this instanceof LocalUser;
  }

  @Entity
  @Table(name = "roles")
  final class Role {
    @Embeddable
    public static class ID implements Serializable {
      @Enumerated(EnumType.STRING)
      @Column(nullable = false)
      private Type type;
      @Column(nullable = false)
      private String username;

      public ID() {
        this.type = null;
        this.username = null;
      }

      public ID(Type type, String username) {
        this.type = type;
        this.username = username;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return type == id.type && Objects.equals(username, id.username);
      }
      @Override
      public int hashCode() {
        return Objects.hash(type, username);
      }
    }

    public enum Type {
      FACULTY,
      ADMIN,
      REVIEWER,
    }

    @Id
    private ID id;

    public Role() {
      this.id = null;
    }

    public Role(Role.Type type, String username) {
      this.id = new ID(type, username);
    }
    public String username() {
      return id.username;
    }

    public Type type() {
      return id.type;
    }
  }


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
    private String displayName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme;

    public LocalUser() {
      this.username = null;
      this.password = null;
      this.email = null;
      this.displayName = null;
    }


    public LocalUser(String username, String password, String email, String displayName, Theme theme) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.displayName = displayName;
      this.theme = theme;
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
    public Theme theme() {
      return theme;
    }

    @Override
    public User withTheme(Theme theme) {
      return new LocalUser(
              this.username,
              this.password,
              this.email,
              this.displayName,
              theme
      );
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
    private String displayName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme;

    public SSOUser() {
      this.username = null;
      this.email = null;
      this.displayName = null;
    }

    public SSOUser(String username, String email, String displayName, Theme theme) {
      this.username = username;
      this.email = email;
      this.displayName = displayName;
      this.theme = theme;
    }

    public String username() {
      return username;
    }
    public String email() {
      return email;
    }
    public String displayName() {
      return displayName;
    }
    public Theme theme() {
      return theme;
    }



    @Override
    public User withTheme(Theme theme) {
      return new SSOUser(
              this.username,
              this.email,
              this.displayName,
              theme
      );
    }

  }
  enum Theme {
    LIGHT, DARK, CUPCAKE, BUMBLEBEE, EMERALD, CORPORATE, SYNTHWAVE, RETRO, CYBERPUNK, VALENTINE,
    HALLOWEEN, GARDEN, FOREST, AQUA, LOFI, PASTEL, FANTASY, WIREFRAME, BLACK, LUXURY, DRACULA,
    CMYK, AUTUMN, BUSINESS, ACID, LEMONADE, NIGHT, COFFEE, WINTER, DIM, NORD, SUNSET, DEFAULT
  }

}
