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

  @Embeddable
  static class ID implements Serializable {
    @Column(nullable = false)
    private String student;
    @Column(nullable = false)
    private Integer programId;
    public ID() {}
    public ID(String student, Integer programId) {
      this.student = student;
      this.programId = programId;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ID id = (ID) o;
      return Objects.equals(student, id.student) && Objects.equals(programId, id.programId);
    }
    @Override
    public int hashCode() {
      return Objects.hash(student, programId);
    }
  }

  @Id
  private ID id;
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

  public Application(String student, Integer programId, LocalDate dateOfBirth,
      Double gpa, String major, Status status) {
    this.id = new ID(student, programId);
    this.dateOfBirth = dateOfBirth;
    this.gpa = gpa;
    this.major = major;
    this.status = status;
  }

  public String student() {
    return id.student;
  }

  public Integer programId() {
    return id.programId;
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
      private String student;
      @Column(nullable = false)
      private Integer programId;
      @Enumerated(EnumType.STRING)
      @Column(nullable = false)
      private Question question;
      public ID() {}
      public ID(Integer programId, String student, Question question) {
        this.programId = programId;
        this.student = student;
        this.question = question;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return Objects.equals(student, id.student) && Objects.equals(programId, id.programId) && question == id.question;
      }
      @Override
      public int hashCode() {
        return Objects.hash(student, programId, question);
      }
    }

    public Response() {
    }

    public Response(Integer programId, String student, Question question, String response) {
      this.id = new ID(programId, student, question);
      this.response = response;
    }
    public Integer programId() {
      return id.programId;
    }

    public String student() {
      return id.student;
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
    return new Application(id.student, id.programId, dateOfBirth, gpa, major, status);
  }
  public Application withMajor(String major) {
    return new Application(id.student, id.programId, dateOfBirth, gpa, major, status);
  }
  public Application withGpa(Double gpa) {
    return new Application(id.student, id.programId, dateOfBirth, gpa, major, status);
  }
  public Application withDateOfBirth(LocalDate dateOfBirth) {
    return new Application(id.student, id.programId, dateOfBirth, gpa, major, status);
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
      private Integer programId;
      @Column(nullable = false)
      private String student;
      public ID() {}
      public ID(Type type, Integer programId, String student) {
        this.type = type;
        this.programId = programId;
        this.student = student;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return type == id.type && Objects.equals(programId, id.programId) && Objects.equals(student, id.student);
      }
      @Override
      public int hashCode() {
        return Objects.hash(type, programId, student);
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

    public Document(Type type, Instant timestamp, Blob file, Integer programId, String student) {
      this.timestamp = timestamp;
      this.file = file;
      this.id = new ID(type, programId, student);
    }

    public Type type() {
      return id.type;
    }

    public Instant timestamp() {
      return timestamp;
    }

    public Blob file() {
      return file;
    }

    public Integer programId() {
      return id.programId;
    }
    public String student() {
      return id.student;
    }
  }

  @Entity
  @Table(name = "notes")
  public static class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private Integer programId;

    @Column(nullable = false)
    private String student;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;

    public Note() {
    }

    public Note(Integer programId, String student, String author, String content, Instant timestamp) {
      this.programId = programId;
      this.author = author;
      this.content = content;
      this.timestamp = timestamp;
      this.student = student;
    }

    public Integer id() {
      return id;
    }

    public String content() {
      return content;
    }

    public Instant timestamp() {
      return timestamp;
    }

    public String author() {
      return author;
    }

    public String student() {
      return student;
    }

    public Integer programId() {
      return programId;
    }
  }

}
