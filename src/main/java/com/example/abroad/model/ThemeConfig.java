package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;

@Entity
@Table(name = "theme_config")
public final class ThemeConfig {

  @Id
  private String key;

  @Column(nullable = false)
  private String value;

  public ThemeConfig() {
  }

  public ThemeConfig(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

}