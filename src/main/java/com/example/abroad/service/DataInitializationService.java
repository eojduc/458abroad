package com.example.abroad.service;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService {

  private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
  private final BCryptPasswordEncoder passwordEncoder;
  private final StudentRepository studentRepository;
  private final AdminRepository adminRepository;
  private final ApplicationRepository applicationRepository;
  private final ProgramRepository programRepository;
  private final CSVFormat csvFormat;
  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  public DataInitializationService(
      StudentRepository studentRepository,
      AdminRepository adminRepository,
      ApplicationRepository applicationRepository,
      ProgramRepository programRepository) {
    this.studentRepository = studentRepository;
    this.adminRepository = adminRepository;
    this.applicationRepository = applicationRepository;
    this.programRepository = programRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.csvFormat = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build();
  }

  protected <T> void initializeData(String path, Function<CSVRecord, T> recordMapper,
      JpaRepository<T, ?> repository) {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      if (inputStream == null) {
        logger.error("Resource not found: {}", path);
        return;
      }

      try (Reader reader = new InputStreamReader(inputStream);
          CSVParser parser = csvFormat.parse(reader)) {

        parser.forEach(record -> {
          try {
            T entity = recordMapper.apply(record);
            repository.save(entity);
          } catch (Exception e) {
            logger.error("Error processing record: {}", e.getMessage());
          }
        });

        logger.info("Data initialized successfully for {}", path);
      }
    } catch (IOException e) {
      logger.error("Error reading data from CSV file {}: {}", path, e.getMessage());
    }
  }


  @Transactional
  protected void initializeStudents(String path) {
    initializeData(
        path,
        record -> new Student(
            record.get("username"),
            passwordEncoder.encode(record.get("password")),
            record.get("email"),
            record.get("displayName")
        ),
        studentRepository
    );
  }

  @Transactional
  protected void initializeAdmins(String path) {
    initializeData(
        path,
        record -> new Admin(
            record.get("username"),
            passwordEncoder.encode(record.get("password")),
            record.get("email"),
            record.get("displayName")
        ),
        adminRepository
    );
  }

  @Transactional
  protected void initializeApplications(String path) {
    initializeData(
        path,
        record -> new Application(
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
        ),
        applicationRepository
    );
  }

  @Transactional
  protected void initializePrograms(String path) {
    initializeData(
        path,
        record -> {
          Optional<Program> optionalProgram = programRepository.findById(
              Integer.parseInt(record.get("id")));
          if (optionalProgram.isPresent()) {
            Program existingProgram = optionalProgram.get();
            existingProgram.setTitle(record.get("title"));
            existingProgram.setYear(Year.parse(record.get("year")));
            existingProgram.setSemester(
                Program.Semester.valueOf(record.get("semester").toUpperCase()));
            existingProgram.setApplicationOpen(Instant.parse(record.get("applicationOpen")));
            existingProgram.setApplicationClose(Instant.parse(record.get("applicationClose")));
            existingProgram.setStartDate(LocalDate.parse(record.get("startDate")));
            existingProgram.setEndDate(LocalDate.parse(record.get("endDate")));
            existingProgram.setFacultyLead(record.get("facultyLead"));
            existingProgram.setDescription(record.get("description"));
            return existingProgram;
          } else {
            return new Program(
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
          }
        },
        programRepository
    );
  }

  @Transactional
  protected void resetDatabase() {
    studentRepository.deleteAll();
    adminRepository.deleteAll();
    applicationRepository.deleteAll();

    entityManager.createNativeQuery("ALTER SEQUENCE programs_seq RESTART WITH 1").executeUpdate();
    programRepository.deleteAll();
  }
}