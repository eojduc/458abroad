package com.example.abroad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;

@Entity
@Table(name = "programs")
public class Program {
    @Id
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



    public enum Semester {
        FALL, SPRING, SUMMER
    }


    // only dumb boilerplate code lies below






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
    String getId() {
        return id;
    }
    String getTitle() {
        return title;
    }
    Year getYear() {
        return year;
    }
    Semester getSemester() {
        return semester;
    }
    Instant getApplicationOpen() {
        return applicationOpen;
    }
    Instant getApplicationClose() {
        return applicationClose;
    }
    LocalDate getStartDate() {
        return startDate;
    }
    LocalDate getEndDate() {
        return endDate;
    }
    String getFacultyLead() {
        return facultyLead;
    }
    String getDescription() {
        return description;
    }
}
