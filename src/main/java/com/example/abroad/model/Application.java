package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Blob;
import java.time.Instant;
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
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status;

  public Application() {}

  public Application(String id, String student, Integer programId, LocalDate dateOfBirth,
      Double gpa, String major, Status status) {
    this.id = id;
    this.student = student;
    this.programId = programId;
    this.dateOfBirth = dateOfBirth;
    this.gpa = gpa;
    this.major = major;
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

  public Status status() {
    return status;
  }

  @Entity
  @Table(name = "responses")
  public static class Response {
    @Id
    private ID id;

    @Column(nullable = false, length = 10000)
    private String response;

    @Embeddable
    public static class ID implements Serializable {
      @Column(nullable = false)
      private String applicationId;
      @Enumerated(EnumType.STRING)
      @Column(nullable = false)
      private Question question;
      public ID() {}
      public ID(String applicationId, Question question) {
        this.applicationId = applicationId;
        this.question = question;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return Objects.equals(applicationId, id.applicationId) && question == id.question;
      }
      @Override
      public int hashCode() {
        return Objects.hash(applicationId, question);
      }
    }

    public Response() {
    }

    public Response(String applicationId, Question question, String response) {
      this.id = new ID(applicationId, question);
      this.response = response;
    }

    public String applicationId() {
      return id.applicationId;
    }

    public Question question() {
      return id.question;
    }

    public String response() {
      return response;
    }

    public enum Question {
      WHY_THIS_PROGRAM,
      ALIGN_WITH_CAREER,
      ANTICIPATED_CHALLENGES,
      ADAPTED_TO_ENVIRONMENT,
      UNIQUE_PERSPECTIVE;

      public String text() {
        return switch (this) {
          case WHY_THIS_PROGRAM -> "Why do you want to participate in this study abroad program?";
          case ALIGN_WITH_CAREER -> "How does this program align with your academic or career goals?";
          case ANTICIPATED_CHALLENGES -> "What challenges do you anticipate during this experience, and how will you address them?";
          case ADAPTED_TO_ENVIRONMENT -> "Describe a time you adapted to a new or unfamiliar environment.";
          case UNIQUE_PERSPECTIVE -> "What unique perspective or contribution will you bring to the group?";
        };
      }

      public String field() {
        return switch (this) {
          case WHY_THIS_PROGRAM -> "answer1";
          case ALIGN_WITH_CAREER -> "answer2";
          case ANTICIPATED_CHALLENGES -> "answer3";
          case ADAPTED_TO_ENVIRONMENT -> "answer4";
          case UNIQUE_PERSPECTIVE -> "answer5";
        };
      }
    }

  }

  public Application withStatus(Status status) {
    return new Application(id, student, programId, dateOfBirth, gpa, major, status);
  }
  public Application withMajor(String major) {
    return new Application(id, student, programId, dateOfBirth, gpa, major, status);
  }
  public Application withGpa(Double gpa) {
    return new Application(id, student, programId, dateOfBirth, gpa, major, status);
  }
  public Application withDateOfBirth(LocalDate dateOfBirth) {
    return new Application(id, student, programId, dateOfBirth, gpa, major, status);
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
    private ID id;

    @Embeddable
    public static class ID implements Serializable {
      @Enumerated(EnumType.STRING)
      @Column(nullable = false)
      private Type type;

      @Column(nullable = false)
      private String applicationId;
      public ID() {}
      public ID(Type type, String applicationId) {
        this.type = type;
        this.applicationId = applicationId;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return type == id.type && Objects.equals(applicationId, id.applicationId);
      }
      @Override
      public int hashCode() {
        return Objects.hash(type, applicationId);
      }
    }

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    @Lob
    private Blob file;

    public enum Type {
      ASSUMPTION_OF_RISK, CODE_OF_CONDUCT, MEDICAL_HISTORY, HOUSING;

      public String text() {
        return switch (this) {
          case ASSUMPTION_OF_RISK -> "Assumption of Risk";
          case CODE_OF_CONDUCT -> "Code of Conduct";
          case MEDICAL_HISTORY -> "Medical History";
          case HOUSING -> "Housing Form";
        };
      }
    }

    public Document() {
    }

    public Document(Type type, Instant timestamp, Blob file, String applicationId) {
      this.timestamp = timestamp;
      this.file = file;
      this.id = new ID(type, applicationId);
    }

    public Type type() {
      return id.type;
    }

    public String id() {
      return id.applicationId + "-" + id.type;
    }

    public Instant timestamp() {
      return timestamp;
    }

    public Blob file() {
      return file;
    }

    public String applicationId() {
      return id.applicationId;
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
