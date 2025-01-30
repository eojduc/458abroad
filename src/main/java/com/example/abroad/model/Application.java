package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "applications")
public final class Application {

  @Id
  private String id;
  @Column(nullable = false)
  private String student;
  @Column(nullable = false)
  private Integer programId;
  @Column(nullable = false)
  private LocalDate dateOfBirth;
  @Column(nullable = false)
  private Double gpa;
  @Column(nullable = false)
  private String major;
  @Column(nullable = false, length = 10000)
  private String answer1;
  @Column(nullable = false, length = 10000)
  private String answer2;
  @Column(nullable = false, length = 10000)
  private String answer3;
  @Column(nullable = false, length = 10000)
  private String answer4;
  @Column(nullable = false, length = 10000)
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


  public Application(String id, String student, Integer programId, LocalDate dateOfBirth,
    Double gpa, String major, String answer1, String answer2, String answer3, String answer4,
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

  public String id() {
    return id;
  }

  public String student() {
    return student;
  }

  public Integer programId() {
    return programId;
  }

  public LocalDate dateOfBirth() {
    return dateOfBirth;
  }

  public Double gpa() {
    return gpa;
  }

  public String major() {
    return major;
  }

  public String answer1() {
    return answer1;
  }

  public String answer2() {
    return answer2;
  }

  public String answer3() {
    return answer3;
  }

  public String answer4() {
    return answer4;
  }

  public String answer5() {
    return answer5;
  }

  public Status status() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Application that = (Application) o;

    if (!Objects.equals(id, that.id)) {
      return false;
    }
    if (!student.equals(that.student)) {
      return false;
    }
    if (!programId.equals(that.programId)) {
      return false;
    }
    if (!dateOfBirth.equals(that.dateOfBirth)) {
      return false;
    }
    if (!gpa.equals(that.gpa)) {
      return false;
    }
    if (!major.equals(that.major)) {
      return false;
    }
    if (!answer1.equals(that.answer1)) {
      return false;
    }
    if (!answer2.equals(that.answer2)) {
      return false;
    }
    if (!answer3.equals(that.answer3)) {
      return false;
    }
    if (!answer4.equals(that.answer4)) {
      return false;
    }
    if (!answer5.equals(that.answer5)) {
      return false;
    }
    return status == that.status;
  }

  @Override
  public String toString() {
    return "Application{" +
      "id='" + id + '\'' +
      ", student='" + student + '\'' +
      ", programId=" + programId +
      ", dateOfBirth=" + dateOfBirth +
      ", gpa=" + gpa +
      ", major='" + major + '\'' +
      ", answer1='" + answer1 + '\'' +
      ", answer2='" + answer2 + '\'' +
      ", answer3='" + answer3 + '\'' +
      ", answer4='" + answer4 + '\'' +
      ", answer5='" + answer5 + '\'' +
      ", status=" + status +
      '}';
  }

  public enum Status {
    APPLIED,
    ENROLLED,
    CANCELLED,
    WITHDRAWN
  }


}
