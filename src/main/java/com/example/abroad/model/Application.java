package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.sql.Blob;
import java.time.Instant;
import java.time.LocalDate;

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

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setGpa(Double gpa) {
    this.gpa = gpa;
  }

  public void setMajor(String major) {
    this.major = major;
  }

  public void setBirthdate(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public void setAnswer1(String answer1) {
    this.answer1 = answer1;
  }

  public void setAnswer2(String answer2) {
    this.answer2 = answer2;
  }

  public void setAnswer3(String answer3) {
    this.answer3 = answer3;
  }

  public void setAnswer4(String answer4) {
    this.answer4 = answer4;
  }

  public void setAnswer5(String answer5) {
    this.answer5 = answer5;
  }

  public enum Status {
    APPLIED,
    ENROLLED,
    CANCELLED,
    WITHDRAWN,
    ELIGIBLE,
    APPROVED,
  }

  @Entity
  @Table(name = "documents")
  public static class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    @Lob
    private Blob file;

    @Column(nullable = false)
    private String applicationId;

    public enum Type {
      ASSUMPTION_OF_RISK, CODE_OF_CONDUCT, MEDICAL_HISTORY, HOUSING
    }

    public Document() {
      this.id = null;
      this.type = null;
      this.timestamp = null;
      this.file = null;
      this.applicationId = null;
    }

    public Document(Type type, Instant timestamp, Blob file, String applicationId) {
      this.type = type;
      this.timestamp = timestamp;
      this.file = file;
      this.applicationId = applicationId;
    }

    public Integer id() {
      return id;
    }

    public Type type() {
      return type;
    }

    public Instant timestamp() {
      return timestamp;
    }

    public Blob file() {
      return file;
    }

    public String applicationId() {
      return applicationId;
    }
  }

  @Entity
  @Table(name = "notes")
  public static class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String applicationId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;

    public Note() {
      this.id = null;
      this.applicationId = null;
      this.username = null;
      this.content = null;
      this.timestamp = null;
    }

    public Note(String applicationId, String username, String content, Instant timestamp) {
      this.applicationId = applicationId;
      this.username = username;
      this.content = content;
      this.timestamp = timestamp;
    }

    public Integer id() {
      return id;
    }

    public String applicationId() {
      return applicationId;
    }

    public String username() {
      return username;
    }

    public String content() {
      return content;
    }

    public Instant timestamp() {
      return timestamp;
    }
  }

}
