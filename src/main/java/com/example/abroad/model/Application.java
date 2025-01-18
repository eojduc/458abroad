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





    enum Status {
        APPLIED,
        ENROLLED,
        CANCELLED,
        WITHDRAWN
    }



    // only dumb boilerplate code lies below






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


}
