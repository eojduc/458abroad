package com.example.abroad;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;

public class TestConstants {

  public static final Program PROGRAM = new Program(
    "111111-1111-1111-1111",
    "Test Program",
    Year.of(2022),
    Program.Semester.FALL,
    Instant.parse("2021-09-01T00:00:00Z"),
    Instant.parse("2021-10-01T00:00:00Z"),
    LocalDate.parse("2022-01-01"),
    LocalDate.parse("2022-05-01"),
    "Test Faculty",
    "Test Description"
  );


  public static final Student STUDENT = new Student(
    "testUser",
    "testpass",
    "test@test.com",
    "Test User"
  );

  public static final Application APPLICATION = new Application(
    "testUser_111111-1111-1111-1111",
    "testUser",
    "111111-1111-1111-1111",
    LocalDate.parse("2021-09-01"),
    3.5,
    "Chemistry",
    "Test answer",
    "Test answer",
    "Test answer",
    "Test answer",
    "Test answer",
    Status.APPLIED
  );
}
