package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;

@Entity
@Table(name = "programs")
public final class Program {

  @Id
  private Integer id;
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
  @Column(nullable = false, length = 10000)
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


  public Program(Integer id, String title, Year year, Semester semester, Instant applicationOpen,
    Instant applicationClose, LocalDate startDate, LocalDate endDate, String facultyLead,
    String description) {
    this.id = id;
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


  public Integer id() {
    return id;
  }

  public String title() {
    return title;
  }

  public Year year() {
    return year;
  }

  public Semester semester() {
    return semester;
  }

  public Instant applicationOpen() {
    return applicationOpen;
  }

  public Instant applicationClose() {
    return applicationClose;
  }

  public LocalDate startDate() {
    return startDate;
  }

  public LocalDate endDate() {
    return endDate;
  }

  public String facultyLead() {
    return facultyLead;
  }

  public String description() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Program program = (Program) o;

    if (!Objects.equals(id, program.id)) {
      return false;
    }
    if (!Objects.equals(title, program.title)) {
      return false;
    }
    if (!Objects.equals(year, program.year)) {
      return false;
    }
    if (semester != program.semester) {
      return false;
    }
    if (!Objects.equals(applicationOpen, program.applicationOpen)) {
      return false;
    }
    if (!Objects.equals(applicationClose, program.applicationClose)) {
      return false;
    }
    if (!Objects.equals(startDate, program.startDate)) {
      return false;
    }
    if (!Objects.equals(endDate, program.endDate)) {
      return false;
    }
    if (!Objects.equals(facultyLead, program.facultyLead)) {
      return false;
    }
    return Objects.equals(description, program.description);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setYear(Year of) {
    this.year = of;
  }

  public void setSemester(Semester semester) {
    this.semester = semester;
  }

  public void setApplicationOpen(Instant applicationOpen) {
    this.applicationOpen = applicationOpen;
  }

  public void setApplicationClose(Instant applicationClose) {
    this.applicationClose = applicationClose;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public void setFacultyLead(String facultyLead) {
    this.facultyLead = facultyLead;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public enum Semester {
    FALL, SPRING, SUMMER
  }
}
