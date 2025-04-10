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

  User withULink(String uLink);

  String uLink();

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
      PARTNER
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
    @Column(nullable = true)
    private String uLink;

    @Column(nullable = true)
    private Boolean mfaEnabled;
    @Column(nullable = true)
    private String mfaSecret;

    public LocalUser() {
      this.username = null;
      this.password = null;
      this.email = null;
      this.displayName = null;
      this.mfaEnabled = false;
      this.mfaSecret = null;
    }

    public LocalUser(String username, String password, String email, String displayName, Theme theme, String uLink) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.displayName = displayName;
      this.theme = theme;
      this.uLink = uLink;
      this.mfaEnabled = false;
      this.mfaSecret = null;
    }

    public LocalUser(String username, String password, String email, String displayName, Theme theme, String uLink, boolean mfaEnabled, String mfaSecret) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.displayName = displayName;
      this.theme = theme;
      this.uLink = uLink;
      this.mfaEnabled = mfaEnabled;
      this.mfaSecret = mfaSecret;
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
              theme,
              this.uLink
      );
    }

    public String uLink() {
      return uLink;
    }

    @Override
    public User withULink(String uLink) {
      return new LocalUser(
              this.username,
              this.password,
              this.email,
              this.displayName,
              this.theme,
              uLink
      );
    }

    public User withMfa(boolean enabled, String secret) {
      return new LocalUser(
              this.username,
              this.password,
              this.email,
              this.displayName,
              this.theme,
              this.uLink,
              enabled,
              secret
      );
    }

    public boolean isMfaEnabled() {
      return mfaEnabled;
    }
    public String mfaSecret() {
      return mfaSecret;
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
    @Column(nullable = true)
    private String uLink;

    public SSOUser() {
    }

    public SSOUser(String username, String email, String displayName, Theme theme, String uLink) {
      this.username = username;
      this.email = email;
      this.displayName = displayName;
      this.theme = theme;
      this.uLink = uLink;
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
    public String uLink() {
      return uLink;
    }
    @Override
    public User withULink(String uLink) {
      return new SSOUser(
              this.username,
              this.email,
              this.displayName,
              this.theme,
              uLink
      );
    }



    @Override
    public User withTheme(Theme theme) {
      return new SSOUser(
              this.username,
              this.email,
              this.displayName,
              theme,
              this.uLink
      );
    }

  }

  @Entity
  @Table(name = "courses")
  final class Course {

    @Embeddable
    public static class ID implements Serializable {
      @Column(nullable = false)
      private String username;
      @Column(nullable = false)
      private String code;
      public ID() {
      }
      public ID(String username, String code) {
        this.username = username;
        this.code = code;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return Objects.equals(username, id.username) && Objects.equals(code, id.code);
      }
      @Override
      public int hashCode() {
        return Objects.hash(username, code);
      }

    }

    @Id
    private ID id;

    @Column(nullable = false)
    private String grade;

    public Course() {
    }

    public Course(String username, String code, String grade) {
      this.id = new ID(username, code);
      this.grade = grade;
    }
    public String username() {
      return id.username;
    }
    public String code() {
      return id.code;
    }
    public String grade() {
      return grade;
    }

    @Override
    public String toString() {
      return "Course{" +
              "username='" + username() + '\'' +
              ", code='" + code() + '\'' +
              ", grade='" + grade + '\'' +
              '}';
    }
  }
  enum Theme {
    LIGHT, DARK, CUPCAKE, BUMBLEBEE, EMERALD, CORPORATE, SYNTHWAVE, RETRO, CYBERPUNK, VALENTINE,
    HALLOWEEN, GARDEN, FOREST, AQUA, LOFI, PASTEL, FANTASY, WIREFRAME, BLACK, LUXURY, DRACULA,
    CMYK, AUTUMN, BUSINESS, ACID, LEMONADE, NIGHT, COFFEE, WINTER, DIM, NORD, SUNSET, DEFAULT
  }

}
