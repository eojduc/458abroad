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

  @Column(nullable = false)
  private LocalDate paymentDeadline;

  @Column(nullable = false)
  private Boolean trackPayment;

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


  public Program(Integer id, String title, Year year, Semester semester, LocalDate applicationOpen,
    LocalDate applicationClose, LocalDate documentDeadline,
    LocalDate startDate, LocalDate endDate,
    String description, LocalDate paymentDeadline, Boolean trackPayment) {
    this.id = id;
    this.title = title;
    this.year = year;
    this.semester = semester;
    this.applicationOpen = applicationOpen;
    this.applicationClose = applicationClose;
    this.documentDeadline = documentDeadline;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.paymentDeadline = paymentDeadline;
    this.trackPayment = trackPayment;
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
  public LocalDate paymentDeadline() {
    return paymentDeadline;
  }
  public Boolean trackPayment() {
    return trackPayment;
  }

  public Program withTitle(String title) {
    return new Program(
      this.id, title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }

  public Program withYear(Year year) {
    return new Program(
      this.id, this.title, year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withSemester(Semester semester) {
    return new Program(
      this.id, this.title, this.year, semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withApplicationOpen(LocalDate applicationOpen) {
    return new Program(
      this.id, this.title, this.year, this.semester, applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withApplicationClose(LocalDate applicationClose) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withStartDate(LocalDate startDate) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withEndDate(LocalDate endDate) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }

  public Program withPaymentDate(LocalDate paymentDate) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      paymentDate, this.trackPayment
    );
  }

  public Program withTrackPayment(Boolean trackPayment) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, trackPayment
    );
  }

  public Program withDescription(String description) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      this.documentDeadline, this.startDate, this.endDate, description,
      this.paymentDeadline, this.trackPayment
    );
  }
  public Program withDocumentDeadline(LocalDate documentDeadline) {
    return new Program(
      this.id, this.title, this.year, this.semester, this.applicationOpen, this.applicationClose,
      documentDeadline, this.startDate, this.endDate, this.description,
      this.paymentDeadline, this.trackPayment
    );
  }

  public enum Semester {
    FALL, SPRING, SUMMER
  }

  @Entity
  @Table(name = "questions")
  public static class Question {

    @Embeddable
    public static class ID implements Serializable {
      private Integer id;
      private Integer programId;
      public ID() {}
      public ID(Integer programId, Integer id) {
        this.programId = programId;
        this.id = id;
      }
      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }
        ID id1 = (ID) o;
        return Objects.equals(id, id1.id) && Objects.equals(programId, id1.programId);
      }

      @Override
      public int hashCode() {
        return Objects.hash(id, programId);
      }
    }

    @Id
    private ID id;

    @Column(nullable = false)
    private String text;

    public Question() {}

    public Question(Integer id, String text, Integer programId) {
      this.id = new ID(programId, id);
      this.text = text;
    }

    public Integer id() {
      return id.id;
    }

    public Integer programId() {
      return id.programId;
    }

    public String text() {
      return text;
    }
  }

  @Entity
  @Table(name = "faculty_leads")
  public static class FacultyLead {

    @Embeddable
    public record ID(Integer programId, String username) { }

    @Id
    private ID id;


    public FacultyLead() {
      this.id = null;
    }

    public FacultyLead(Integer programId, String username) {
      this.id = new ID(programId, username);
    }

    public Integer programId() {
      return id.programId();
    }
    public String username() {
      return id.username();
    }
  }


  @Entity
  @Table(name = "prereqs")
  public static class PreReq {

    @Embeddable
    public record ID(Integer programId, String courseCode) {
    }

    @Id
    private ID id;

    public PreReq() {
    }
    public PreReq(Integer programId, String courseCode) {
      this.id = new ID(programId, courseCode);
    }

    public Integer programId() {
      return id.programId();
    }
    public String courseCode() {
      return id.courseCode();
    }

  }

  @Entity
  @Table(name = "partners")
  public static class Partner {

    @Embeddable
    public record ID(Integer programId, String username) {
    }

    @Id
    private ID id;

    public Partner() {
      this.id = null;
    }

    public Partner(Integer programId, String username) {
      this.id = new ID(programId, username);
    }

    public Integer programId() {
      return id.programId();
    }
    public String username() {
      return id.username();
    }
  }


}
