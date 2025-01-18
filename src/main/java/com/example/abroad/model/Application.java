package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "applications")
public class Application {

  @GeneratedValue(generator = "uuid")
  @Id
  private String id;
  @Column(nullable = false)
  private String student;
  @Column(nullable = false)
  private String programId;
  @Column(nullable = false)
  private LocalDate dateOfBirth;
  @Column(nullable = false)
  private Float gpa;
  @Column(nullable = false)
  private String major;
  @Column(nullable = false)
  private String answer1;
  @Column(nullable = false)
  private String answer2;
  @Column(nullable = false)
  private String answer3;
  @Column(nullable = false)
  private String answer4;
  @Column(nullable = false)
  private String answer5;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status;


  public Application() {
    this.id = null;
    this.student = null;
    this.programId = null;
    this.dateOfBirth = null;
    this.gpa = null;
    this.major = null;
    this.answer1 = null;
    this.answer2 = null;
    this.answer3 = null;
    this.answer4 = null;
    this.answer5 = null;
    this.status = null;
  }


  public Application(String id, String student, String programId, LocalDate dateOfBirth,
      Float gpa, String major, String answer1, String answer2, String answer3, String answer4,
      String answer5, Status status) {
    this.id = id;
    this.student = student;
    this.programId = programId;
    this.dateOfBirth = dateOfBirth;
    this.gpa = gpa;
    this.major = major;
    this.answer1 = answer1;
    this.answer2 = answer2;
    this.answer3 = answer3;
    this.answer4 = answer4;
    this.answer5 = answer5;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public String getStudent() {
    return student;
  }

  public String getProgramId() {
    return programId;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public Float getGpa() {
    return gpa;
  }

  public String getMajor() {
    return major;
  }

  public String getAnswer1() {
    return answer1;
  }

  public String getAnswer2() {
    return answer2;
  }

  public String getAnswer3() {
    return answer3;
  }

  public String getAnswer4() {
    return answer4;
  }

  public String getAnswer5() {
    return answer5;
  }

  public Status getStatus() {
    return status;
  }

  enum Status {
    APPLIED,
    ENROLLED,
    CANCELLED,
    WITHDRAWN
  }


}
