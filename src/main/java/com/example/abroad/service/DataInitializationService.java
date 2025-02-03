package com.example.abroad.service;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService {

  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private AdminRepository adminRepository;
  @Autowired
  private ApplicationRepository applicationRepository;
  @Autowired
  private ProgramRepository programRepository;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
  private static final String STUDENT_CSV_FILE_PATH = "data/students.csv";
  private static final String ADMIN_CSV_FILE_PATH = "data/admins.csv";
  private static final String APPLICATION_CSV_FILE_PATH = "applications.csv";
  private static final String PROGRAM_CSV_FILE_PATH = "data/programs.csv";

  @Transactional
  protected void initializeStudents() {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream(STUDENT_CSV_FILE_PATH)) {

      if (inputStream == null) {
        logger.error("Resource not found: {}", STUDENT_CSV_FILE_PATH);
        return;
      }
      Reader reader = new InputStreamReader(inputStream);
      CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
          .parse(reader);

      for (CSVRecord record : parser) {
        String encryptedPassword = passwordEncoder.encode(record.get("password"));

        Student student = new Student(
            record.get("username"),
            encryptedPassword,
            record.get("email"),
            record.get("displayName")
        );

        studentRepository.save(student);
      }
      logger.info("Student data initialized successfully");
    } catch (IOException e) {
      logger.error("Error reading student data from CSV file: {}", e.getMessage());
    }
  }

  @Transactional
  protected void initializeAdmins() {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream(ADMIN_CSV_FILE_PATH)) {

      if (inputStream == null) {
        logger.error("Resource not found: {}", ADMIN_CSV_FILE_PATH);
        return;
      }
      Reader reader = new InputStreamReader(inputStream);

      CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
          .parse(reader);

      for (CSVRecord record : parser) {
        String encryptedPassword = passwordEncoder.encode(record.get("password"));

        Admin admin = new Admin(
            record.get("username"),
            encryptedPassword,
            record.get("email"),
            record.get("displayName")
        );

        adminRepository.save(admin);
      }
      logger.info("Admin data initialized successfully");
    } catch (IOException e) {
      logger.error("Error reading admin data from CSV file: {}", e.getMessage());
    }
  }

  @Transactional
  protected void initializeApplications() {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream(APPLICATION_CSV_FILE_PATH)) {

      if (inputStream == null) {
        logger.error("Resource not found: {}", APPLICATION_CSV_FILE_PATH);
        return;
      }
      Reader reader = new InputStreamReader(inputStream);
      CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
          .parse(reader);
      for (CSVRecord record : parser) {
        Application application = new Application(
            record.get("id"),
            record.get("student"),
            Integer.parseInt(record.get("programId")),
            LocalDate.parse(record.get("dateOfBirth")),
            Double.parseDouble(record.get("gpa")),
            record.get("major"),
            record.get("answer1"),
            record.get("answer2"),
            record.get("answer3"),
            record.get("answer4"),
            record.get("answer5"),
            Application.Status.valueOf(record.get("status").toUpperCase())
        );

        applicationRepository.save(application);
      }
      logger.info("Application data initialized successfully");
    } catch (IOException e) {
      logger.error("Error reading application data from CSV file: {}", e.getMessage());
    }
  }

  @Transactional
  protected void initializePrograms() {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream(PROGRAM_CSV_FILE_PATH)) {

      if (inputStream == null) {
        logger.error("Resource not found: {}", PROGRAM_CSV_FILE_PATH);
        return;
      }
      Reader reader = new InputStreamReader(inputStream);

      CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
          .parse(reader);

      for (CSVRecord record : parser) {
        Program program = new Program(
            record.get("title"),
            Year.parse(record.get("year")),
            Program.Semester.valueOf(record.get("semester").toUpperCase()),
            Instant.parse(record.get("applicationOpen")),
            Instant.parse(record.get("applicationClose")),
            LocalDate.parse(record.get("startDate")),
            LocalDate.parse(record.get("endDate")),
            record.get("facultyLead"),
            record.get("description")
        );

        programRepository.save(program);
      }
      logger.info("Program data initialized successfully");
    } catch (IOException e) {
      logger.error("Error reading program data from CSV file: {}", e.getMessage());
    }
  }
}

