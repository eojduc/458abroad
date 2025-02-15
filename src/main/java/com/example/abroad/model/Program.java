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
import java.time.LocalDate;
import java.time.Year;
@Entity
@Table(name = "programs")
public final class Program {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Integer id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Year year;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Semester semester;

  @Column(nullable = false)
  private LocalDate applicationOpen;

  @Column(nullable = false)
  private LocalDate applicationClose;

  @Column(nullable = false)
  private LocalDate documentDeadline;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

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
    this.description = null;
  }


  public Program(String title, Year year, Semester semester, LocalDate applicationOpen,
    LocalDate applicationClose, LocalDate documentDeadline,
    LocalDate startDate, LocalDate endDate,
    String description) {
    this.title = title;
    this.year = year;
    this.semester = semester;
    this.applicationOpen = applicationOpen;
    this.applicationClose = applicationClose;
    this.documentDeadline = documentDeadline;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
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

  public LocalDate applicationOpen() {
    return applicationOpen;
  }

  public LocalDate applicationClose() {
    return applicationClose;
  }

  public LocalDate startDate() {
    return startDate;
  }

  public LocalDate endDate() {
    return endDate;
  }
  public String description() {
    return description;
  }

  public LocalDate documentDeadline() {
    return documentDeadline;
  }


  // ONLY FOR INITIALIZING SAMPLE DATA
  public void setId(Integer id) {
    this.id = id;
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

  public void setApplicationOpen(LocalDate applicationOpen) {
    this.applicationOpen = applicationOpen;
  }

  public void setApplicationClose(LocalDate applicationClose) {
    this.applicationClose = applicationClose;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public enum Semester {
    FALL, SPRING, SUMMER
  }


  @Entity
  @Table(name = "faculty_leads")
  public class FacultyLead {

    @Embeddable
    public record AppUser(String applicationId, String username) { }

    @Id
    private AppUser id;


    public FacultyLead() {
      this.id = null;
    }

    public FacultyLead(String applicationId, String username) {
      this.id = new AppUser(applicationId, username);
    }

    public String applicationId() {
      return id.applicationId();
    }
    public String username() {
      return id.username();
    }
  }

}
