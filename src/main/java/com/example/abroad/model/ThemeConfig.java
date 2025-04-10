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

  @Column(nullable = false, length = 1000)
  private String value;

  @Column(name = "svg_data", columnDefinition = "TEXT")
  private String svgData; // Optional, nullable by default

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

  public String getSvgData() {
    return svgData;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setSvgData(String svgData) {
    this.svgData = svgData;
  }

  public boolean hasSvg() {
    return svgData != null && !svgData.isBlank();
  }
}
