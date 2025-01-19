package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "programs")
public class Program {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;
  @Column(nullable = false)
  private String title;
  @Column(nullable = false)
  private Year year;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Semester semester;
  @Column(nullable = false)
  private Instant applicationOpen;
  @Column(nullable = false)
  private Instant applicationClose;
  @Column(nullable = false)
  private LocalDate startDate;
  @Column(nullable = false)
  private LocalDate endDate;
  @Column(nullable = false)
  private String facultyLead;
  @Column(nullable = false)
  private String description;


  public Program() {
    this.id = null;
    this.title = null;
    this.year = null;
    this.semester = null;
    this.applicationOpen = null;
    this.applicationClose = null;
    this.startDate = null;
    this.endDate = null;
    this.facultyLead = null;
    this.description = null;
  }


  public Program(String title, Year year, Semester semester, Instant applicationOpen,
    Instant applicationClose, LocalDate startDate, LocalDate endDate, String facultyLead,
    String description) {
    this.title = title;
    this.year = year;
    this.semester = semester;
    this.applicationOpen = applicationOpen;
    this.applicationClose = applicationClose;
    this.startDate = startDate;
    this.endDate = endDate;
    this.facultyLead = facultyLead;
    this.description = description;
  }

  @Override
  public String toString() {
    return "Program{" +
      "id='" + id + '\'' +
      ", title='" + title + '\'' +
      ", year=" + year +
      ", semester=" + semester +
      ", applicationOpen=" + applicationOpen +
      ", applicationClose=" + applicationClose +
      ", startDate=" + startDate +
      ", endDate=" + endDate +
      ", facultyLead='" + facultyLead + '\'' +
      ", description='" + description + '\'' +
      '}';
  }


  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Year getYear() {
    return year;
  }

  public Semester getSemester() {
    return semester;
  }

  public Instant getApplicationOpen() {
    return applicationOpen;
  }

  public Instant getApplicationClose() {
    return applicationClose;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public String getFacultyLead() {
    return facultyLead;
  }

  public String getDescription() {
    return description;
  }

  public enum Semester {
    FALL, SPRING, SUMMER
  }
}
