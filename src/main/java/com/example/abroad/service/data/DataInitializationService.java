package com.example.abroad.service.data;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.PaymentStatus;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.User.Theme;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.NoteRepository;
import com.example.abroad.respository.PartnerRepository;
import com.example.abroad.respository.PreReqRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.QuestionRepository;
import com.example.abroad.respository.ResponseRepository;
import com.example.abroad.respository.RoleRepository;
import com.example.abroad.respository.SSOUserRepository;

import com.example.abroad.respository.ThemeConfigRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;
import java.util.function.Function;
import javax.sql.rowset.serial.SerialBlob;
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
  private final ApplicationRepository applicationRepository;
  private final ProgramRepository programRepository;
  private final LocalUserRepository localUserRepository;
  private final SSOUserRepository ssoUserRepository;
  private final FacultyLeadRepository facultyLeadRepository;
  private final NoteRepository noteRepository;
  private final DocumentRepository documentRepository;
  private final ResponseRepository responseRepository;
  private final QuestionRepository questionRepository;
  private final RoleRepository roleRepository;
  private final PreReqRepository preReqRepository;
  private final PartnerRepository partnerRepository;
  private final ThemeConfigRepository themeConfigRepository;
  private final CSVFormat csvFormat;
  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  public DataInitializationService(
    LocalUserRepository localUserRepository,
      SSOUserRepository ssoUserRepository,
      ApplicationRepository applicationRepository,
      FacultyLeadRepository facultyLeadRepository,
      NoteRepository noteRepository,
      DocumentRepository documentRepository,
      ProgramRepository programRepository,
      ResponseRepository responseRepository,
      QuestionRepository questionRepository,
      RoleRepository roleRepository,
      PreReqRepository preReqRepository,
      PartnerRepository partnerRepository,
      ThemeConfigRepository themeConfigRepository
    ) {
    this.localUserRepository = localUserRepository;
    this.ssoUserRepository = ssoUserRepository;
    this.applicationRepository = applicationRepository;
    this.programRepository = programRepository;
    this.facultyLeadRepository = facultyLeadRepository;
    this.noteRepository = noteRepository;
    this.preReqRepository = preReqRepository;
    this.documentRepository = documentRepository;
    this.responseRepository = responseRepository;
    this.questionRepository = questionRepository;
    this.roleRepository = roleRepository;
    this.partnerRepository = partnerRepository;
    this.themeConfigRepository = themeConfigRepository;
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
  protected void initializeLocalUsers(String path) {
    initializeData(
        path,
        record -> new User.LocalUser(
            record.get("username"),
            passwordEncoder.encode(record.get("password")),
            record.get("email"),
            record.get("displayName"),
          Theme.DEFAULT,
          null
        ),
        localUserRepository
    );
  }

  @Transactional
  protected void initializeSsoUsers(String path) {
    initializeData(
        path,
        record -> new User.SSOUser(
            record.get("username"),
            record.get("email"),
            record.get("displayName"),
          Theme.DEFAULT,
          null
        ),
        ssoUserRepository
    );
  }

  @Transactional
  protected void initializeApplications(String path) {
    initializeData(
        path,
        record -> new Application(
            record.get("student"),
            Integer.parseInt(record.get("programId")),
            LocalDate.parse(record.get("dateOfBirth")),
            Double.parseDouble(record.get("gpa")),
            record.get("major"),
            Application.Status.valueOf(record.get("status").toUpperCase()),
          PaymentStatus.UNPAID
        ),
        applicationRepository
    );
  }

  @Transactional
  protected void initializeFacultyLeads(String path) {
    initializeData(
        path,
        record -> new FacultyLead(Integer.parseInt(record.get("programId")), record.get("username")),
        facultyLeadRepository
    );
  }
  @Transactional
  protected void initializeDocuments(String path) {
    initializeData(
        path,
        record -> {
          try {
            return new Application.Document(
              Document.Type.valueOf(record.get("type").toUpperCase()),
              Instant.parse(record.get("timestamp")),
              new SerialBlob("hello world!".getBytes()),
                Integer.parseInt(record.get("programId")),
                record.get("student")
            );
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        },
        documentRepository
    );
  }

  @Transactional
  protected void initializePreReqs(String path) {
    initializeData(
        path,
        record -> new Program.PreReq(
          Integer.parseInt(record.get("programId")),
            record.get("courseCode")
        ),
        preReqRepository
    );
  }

  @Transactional
  protected void initializeResponses(String path) {
    initializeData(
        path,
        record -> new Application.Response(
          Integer.parseInt(record.get("programId")),
            record.get("student"),
            Integer.valueOf(record.get("question")),
            record.get("response")
        ),
        responseRepository
    );
  }

  @Transactional
  protected void initializeQuestions(String path) {
    initializeData(
        path,
        record -> new Program.Question(
          Integer.parseInt(record.get("id")),
            record.get("text"),
            Integer.parseInt(record.get("programId"))
        ),
        questionRepository
    );
  }

  @Transactional
  protected void initializePartners(String path) {
    initializeData(
        path,
        record -> new Program.Partner(
          Integer.parseInt(record.get("programId")),
            record.get("username")
        ),
        partnerRepository
    );
  }

  @Transactional
  protected void initializeRoles(String path) {
    initializeData(
        path,
        record -> new User.Role(
          User.Role.Type.valueOf(record.get("type").toUpperCase()),
            record.get("username")
        ),
        roleRepository
    );
  }

  @Transactional
  protected void initializeNotes(String path) {
    initializeData(
        path,
        record -> new Application.Note(
          Integer.parseInt(record.get("programId")),
          record.get("student"),
            record.get("username"),
          record.get("content"),
          Instant.parse(record.get("timestamp"))
        ),
        noteRepository
    );
  }

  @Transactional
  protected void initializeThemeConfig(String path) {
    initializeData(
        path,
        record -> new ThemeConfig(
          record.get("key"),
            record.get("value")
        ),
        themeConfigRepository
    );

    String logoSvg = "<svg\n"
        + "    xmlns=\"http://www.w3.org/2000/svg\"\n"
        + "    xmlns:th=\"http://www.thymeleaf.org\"\n"
        + "    viewBox=\"0 0 563 443\"\n"
        + "    preserveAspectRatio=\"xMidYMid meet\">\n"
        + "  <g fill=\"currentColor\">\n"
        + "    <path d=\"M268 398.8 c-16.3 -1.8 -38.9 -8.5 -57 -17.1 -35.7 -16.9 -67 -47.4 -83.9 -81.7 -2.3 -4.7 -4.8 -9.6 -5.6 -11 -2.9 -5.5 -9.7 -28 -12.2 -40.4 -2.3 -11.6 -2.6 -15.5 -2.7 -34.1 0 -19.1 0.2 -22.2 2.7 -34 4.2 -20.3 7.8 -30.5 17.5 -50 15.2 -30.4 40.1 -57 70.7 -75.2 6.7 -4 27.2 -13.4 34 -15.6 23.2 -7.5 41.5 -10.1 65 -9.4 28.9 0.9 50.2 5.8 74 17.3 23.1 11.1 42.9 25.8 58.4 43.5 10.5 12 11.3 13 16.5 20.9 18.8 28.7 28.4 57.1 30.8 90.9 1.6 24.1 -0.3 41.6 -7.3 65.6 -3.3 11.3 -12.8 33.6 -15.5 36.2 -0.8 0.8 -1.4 1.9 -1.4 2.5 0 0.6 -2.8 5.2 -6.2 10.2 -22.5 33.3 -53.9 57.8 -91.8 71.9 -9.7 3.6 -30.2 8.4 -41.5 9.6 -11.5 1.3 -32.1 1.2 -44.5 -0.1z m52 -6.3 c26.4 -4.1 53.3 -15 75.5 -30.7 10 -7 29.2 -25.1 36.5 -34.3 28.3 -35.8 42.6 -81.3 39.1 -124.2 -3.2 -37.3 -14.7 -68.3 -36.2 -96.9 -30.6 -40.7 -77.1 -66.2 -128.6 -70.6 -89.5 -7.6 -171.2 52.6 -190.7 140.3 -8.1 36.9 -4.2 76.2 11.3 111.7 25.3 58.3 82.3 99.6 146.6 106.2 11.4 1.1 34.2 0.4 46.5 -1.5z\"/>\n"
        + "    <path d=\"M279.3 388.8 c-63.1 -3.5 -122.5 -45 -147.5 -103.1 -5.1 -11.8 -9 -23.9 -8.1 -24.8 0.5 -0.4 6 -2.1 12.3 -3.8 6.3 -1.7 18.9 -5.1 28 -7.6 9.1 -2.5 16.8 -4.5 17.2 -4.5 0.3 0 1.6 3 2.9 6.8 12.7 38.5 47.9 68.2 89.3 75.3 11.5 1.9 36.1 0.6 47.6 -2.5 36.3 -10.1 65.1 -36.8 77.5 -71.8 1.5 -4.3 3 -7.8 3.3 -7.8 0.5 0 22.2 5.8 43.2 11.5 4.7 1.3 10 2.7 11.8 3 1.7 0.4 3.2 1.1 3.2 1.7 0 0.5 -1.3 4.9 -2.9 9.7 -12.5 37.4 -39.1 70.9 -72.6 91.8 -31.7 19.8 -66 28.3 -105.2 26.1z m15.1 -25 c-0.3 -2.9 -0.7 -6.1 -0.9 -7 -0.7 -3.6 1.4 -1.6 5.1 4.7 3.5 5.9 4.1 6.5 7.1 6.5 l3.3 0 0 -11.4 c0 -6.3 0.3 -12.1 0.6 -13 0.5 -1.3 -0.1 -1.6 -3.3 -1.6 -4.4 0 -5.7 0.8 -4.4 2.4 1.2 1.5 2.5 14.6 1.5 14.6 -0.5 0 -2.9 -3.6 -5.4 -8 -4.4 -7.8 -4.6 -8 -8.4 -8 -3.6 0 -3.8 0.2 -3 2.3 0.5 1.2 0.8 7 0.9 13 l0 10.7 3.7 0 3.7 0 -0.5 -5.2z m-22.1 3.5 c0.4 -0.3 0.1 -1.3 -0.7 -2.2 -0.9 -1 -1.8 -5.7 -2.5 -12.5 -0.5 -6 -1.3 -11.2 -1.8 -11.7 -0.4 -0.4 -2.5 -1.1 -4.5 -1.4 -3.5 -0.7 -3.8 -0.6 -3.6 1.6 0.2 2.8 -7.3 16.6 -10.4 19.2 -3.1 2.6 -1.1 3.8 6 3.7 0.6 0 0.8 -0.5 1.1 -3.7 0.1 -1.5 5.9 -1.7 8 -0.4 0.9 0.6 1.1 1.8 0.6 3.9 -0.5 2.6 -0.3 3.1 1.7 3.5 3.8 0.7 5.4 0.7 6.1 0z m80.2 -10.5 c2.3 -1 3.6 -3.8 1.7 -3.8 -0.7 0 -4.2 -6.3 -4.2 -7.5 0 -0.6 8.1 -4.7 8.5 -4.3 1.2 1.4 3.5 7.1 3.5 8.8 0 2.4 0.3 2.4 4 0.5 2.9 -1.5 4.2 -4.5 2 -4.5 -1.3 0 -9 -16.4 -9 -19.1 0 -2.3 -0.3 -2.3 -4 -0.4 -2.8 1.5 -4.2 4.5 -2 4.5 0.5 0 1.6 1.3 2.5 3 1.4 2.8 1.4 3 -0.7 4.4 -1.3 0.8 -3.5 1.7 -4.9 2.1 -2.1 0.5 -2.7 0.2 -3.7 -2 -0.7 -1.4 -1.2 -3.5 -1.2 -4.5 0 -2.3 0.2 -2.3 -3.9 -0.9 -3.1 1.1 -4.1 3.2 -2.1 4.4 1.3 0.8 9 17.2 9 19.1 0 1.7 1.1 1.8 4.5 0.2z m-115.7 -6.5 c1.3 -2.4 2 -4.7 1.7 -5.3 -1.2 -2 -7.8 -4.1 -8.9 -3 -0.8 0.8 -0.4 1.6 1.2 2.9 l2.3 1.7 -2 2.5 c-1.9 2.3 -2.1 2.3 -4.8 0.9 -1.8 -1 -2.9 -2.5 -3.1 -4.2 -0.4 -3.7 2.2 -9.3 5.2 -11.3 3.3 -2.2 7.6 -1.3 7.6 1.5 0 2.4 0.9 2.5 3.6 0.6 2.7 -2.1 0.9 -4.6 -4.8 -7 -4.5 -1.8 -4.6 -1.8 -8.4 0 -6.8 3.3 -10.7 13.7 -7.5 19.7 2 3.7 6.1 5.7 11.1 5.4 4.1 -0.2 4.6 -0.5 6.8 -4.4z m153.7 -16.5 c3.6 -2.8 6.5 -5.5 6.5 -5.8 0 -0.4 -0.9 -1.5 -2 -2.5 -2 -1.8 -2.1 -1.8 -2.6 0.2 -0.3 1.1 -2 3.1 -3.7 4.3 l-3 2.3 -1.9 -2.3 c-2.4 -2.9 -2 -5.4 1 -5.8 2.7 -0.4 2.8 -1.4 0.1 -3.8 -2 -1.8 -2.1 -1.8 -3.9 0.6 -1.8 2.4 -1.9 2.4 -3.9 0.6 -1.2 -1.1 -2.1 -2.3 -2.1 -2.8 0 -1.5 4.3 -4.7 6.3 -4.8 2.1 0 2.2 -2.3 0.2 -4 -1.7 -1.4 -1.3 -1.6 -9.7 5.3 -4.9 3.9 -5.9 5.7 -3.5 5.7 1.6 0 13.7 14.8 13.7 16.8 0 2.2 2 1.3 8.5 -4z m-186.5 0.7 c1.6 -0.8 4.1 -3 5.5 -4.9 5.6 -7.3 3.9 -15.2 -4 -18.7 -5 -2.2 -8.2 -1.5 -12.8 2.9 -7.8 7.4 -7.5 15.3 0.7 20.3 3.7 2.3 6.7 2.4 10.6 0.4z m206 -19.2 c2 -2.6 2.2 -5 0.4 -4.6 -0.5 0.1 -2 -0.7 -3.4 -1.7 -2 -1.5 -2.2 -2 -1.1 -3.4 1.9 -2.2 4.7 -2 7.1 0.7 l2.1 2.2 2.9 -3.5 c2.8 -3.4 3 -5.6 0.5 -4.6 -0.7 0.2 -2.9 0 -4.9 -0.6 -2.7 -0.9 -3.6 -1.7 -3.6 -3.3 0 -3.6 -4.4 -7.5 -8.4 -7.5 -2.9 0 -4.2 0.8 -8.5 5.7 -5.1 5.7 -6.3 8.3 -3.7 8.3 2 0 16.6 11.9 16.6 13.6 0 2.2 1.7 1.7 4 -1.3z m-234.6 -0.9 c2.3 -0.9 2 -2.2 -1.4 -5.1 -1.6 -1.3 -3 -3 -3 -3.7 0 -1.6 12.4 -11.6 14.4 -11.6 2.6 0 2.8 -1.9 0.5 -5 -2.4 -3.2 -3.9 -3.8 -3.9 -1.7 0 1.7 -15.3 13.7 -17.4 13.7 -0.9 0 -1.6 0.3 -1.6 0.6 0 0.7 10 13.4 10.5 13.4 0.2 0 1 -0.3 1.9 -0.6z m257.4 -34 c3.6 -7.3 3.6 -7.6 1.8 -8.9 -2.2 -1.7 -3.6 -1.1 -3.6 1.5 0 2.9 -2.2 7 -3.7 7 -2.3 0 -5.3 -2 -5.3 -3.4 0 -0.8 0.7 -1.9 1.5 -2.6 2.1 -1.7 1.9 -2.5 -1 -3.8 -3.4 -1.6 -3.3 -1.6 -3.7 1.5 -0.3 3.2 -2.5 4 -5.2 2 -1.7 -1.3 -1.7 -1.5 -0.2 -4.5 0.9 -1.8 2.1 -3.2 2.6 -3.2 1.9 0 1 -2.9 -1.1 -3.5 -2.5 -0.8 -2 -1.3 -5.8 6.2 -3.6 7.3 -3.9 9.3 -1.1 9.3 2.3 0 17.5 7.4 18.5 9 1.3 2.1 2.6 0.8 6.3 -6.6z m-269.3 4.6 c1.3 -1.4 2 -4.1 2.3 -8.3 0.2 -3.4 0.8 -6.6 1.3 -7.1 1.4 -1.6 5 2.4 4.7 5.3 -0.3 2.2 0.1 2.6 2.2 2.6 2.2 0 2.5 -0.4 2.2 -3 -0.7 -6.1 -6.2 -11.2 -10.8 -10.1 -3.2 0.8 -5.4 5.8 -5.4 12.5 0 2.7 -0.4 5.2 -0.9 5.5 -2.9 1.8 -6.7 -4.1 -5.6 -8.6 0.9 -3.3 -2 -4.6 -3.6 -1.7 -1.5 2.9 -0.3 8.3 2.7 11.9 3.1 3.7 8.1 4.1 10.9 1z\"/>\n"
        + "    <path d=\"M260.8 355.3 c-1.7 -0.4 -1.7 -0.7 -0.2 -3.5 1.2 -2.4 1.8 -2.8 2.5 -1.7 1.1 1.7 1.2 5.9 0.2 5.8 -0.5 -0.1 -1.6 -0.3 -2.5 -0.6z\"/>\n"
        + "    <path d=\"M193.7 329.2 c-2.6 -2.8 -2.1 -5.4 1.8 -10 4.3 -5 6.8 -5.9 10 -3.8 2.9 1.9 3.1 4.5 1 8.6 -3.2 6.2 -9.6 8.8 -12.8 5.2z\"/>\n"
        + "    <path d=\"M397.6 301.5 c-2.9 -2.3 -2.9 -2.4 -1.2 -4.9 1.9 -2.9 4.7 -3.3 7 -1 2.1 2.1 2 4.7 -0.2 6.7 -2.3 2.1 -2.1 2.1 -5.6 -0.8z\"/>\n"
        + "    <path d=\"M272.3 321.5 c-22.9 -4.1 -43.5 -15.5 -59.9 -32.9 -18.1 -19.2 -27.5 -41.1 -29.1 -67.8 -1 -17.6 2.3 -35.2 9.7 -51.4 3 -6.4 9.3 -16.9 10.8 -17.8 0.5 -0.3 2.2 1.8 3.8 4.7 l2.9 5.2 0.6 42 c0.5 37.7 0.8 42.7 2.6 49 6.2 21.9 16.7 33.2 49.7 53.5 7.7 4.7 16 10.4 18.5 12.7 l4.6 4.2 -3.5 0 c-1.9 -0.1 -6.7 -0.7 -10.7 -1.4z\"/>\n"
        + "    <path d=\"M299.5 320 c1.6 -1.6 6.5 -5.3 10.9 -8.2 39.1 -25 47.1 -31.8 53.9 -46.1 6.7 -14 7 -15.7 7.6 -62.2 0.6 -41 0.7 -42.1 2.8 -46.2 2.7 -5 3.9 -5.9 5.8 -4.3 2.7 2.2 13.4 25.4 15.5 33.5 6.7 25.8 4.5 51 -6.4 74.1 -14.9 31.5 -41.9 52.9 -75.8 60.3 -5.4 1.2 -11.4 2.1 -13.5 2.1 l-3.7 0 2.9 -3z\"/>\n"
        + "    <path d=\"M284.8 314.2 c-3.1 -2.6 -9.7 -7.1 -14.5 -10.1 -31.2 -19.3 -40.4 -27.2 -46.7 -40.3 -6.5 -13.6 -6.8 -15.8 -7.5 -62.3 -0.5 -37 -0.8 -42.4 -2.3 -45.1 -1 -1.7 -1.4 -3.4 -1.1 -3.8 0.8 -0.8 156.8 -0.8 157.5 0 0.4 0.3 -0.1 2.2 -1 4.2 -1.4 3.2 -1.8 9.6 -2.3 45.7 -0.6 39.5 -0.7 42.4 -2.8 49.5 -6 20 -15.6 30.2 -45.7 48.4 -7.1 4.2 -16 10.2 -19.7 13.2 -3.7 3 -7 5.4 -7.5 5.3 -0.4 0 -3.3 -2.1 -6.4 -4.7z m4.2 -43.2 l0 -41 -34.6 0 -34.7 0 0.5 8.8 c1 15.2 7 28.4 17.9 38.9 6.7 6.4 9.8 8.7 26.9 19.3 7.4 4.6 15.5 9.8 17.9 11.7 2.4 1.8 4.8 3.3 5.2 3.3 0.5 0 0.9 -18.4 0.9 -41z m45.5 6.9 c5.8 -2.6 8.5 -5 8.5 -7.5 0 -1.4 -0.7 -2.7 -1.5 -3 -0.9 -0.4 -2 -1.5 -2.6 -2.6 -1.6 -3 1.1 -5.2 9.6 -7.7 4.2 -1.3 8.2 -2.9 9 -3.8 3.6 -3.6 0 -9.5 -7.9 -12.8 -4.5 -1.9 -18.5 -2.2 -25.8 -0.6 -10.6 2.4 -21.9 9.2 -26.2 15.8 -6.5 9.9 -3.1 18.4 9.2 22.9 4.6 1.8 23.3 1.3 27.7 -0.7z m-87.8 -62.9 l6.2 -10.1 -3.2 -1.9 c-1.8 -1.1 -3.8 -1.9 -4.6 -1.8 -1.1 0.2 -14.1 21.8 -14.1 23.4 0 0.2 2.1 0.4 4.8 0.4 l4.7 0 6.2 -10z m116.3 -16.2 c0.1 -24.5 0.8 -36.8 2.5 -41 0.7 -1.7 -1.4 -1.8 -35.4 -1.8 l-36.1 0 0 34.5 0 34.5 34.5 0 34.5 0 0 -26.2z m-109.4 -1.5 c-3.4 -2.9 -6.3 -3 -7.1 -0.2 -0.4 1.1 -0.5 2.2 -0.3 2.3 0.1 0.1 1.9 1.2 3.8 2.3 l3.4 2.2 1.5 -2.1 c1.3 -1.9 1.2 -2.3 -1.3 -4.5z m11.7 -4.3 c3.1 -3.6 5.1 -12.5 4.2 -18.2 -0.7 -3.7 -0.9 -4 -2 -2.5 -1.1 1.5 -1.3 1.3 -1.8 -1.9 -0.7 -3.9 -5.2 -12.4 -6.7 -12.4 -0.6 0 -1 1.1 -1 2.4 0 1.3 -2.7 7.7 -6 14.1 -7.8 15.1 -7.8 16.6 -0.3 20.4 2.3 1.1 4.4 2.5 4.8 3.1 0.9 1.4 5.7 -1.3 8.8 -5z\"/>\n"
        + "    <path d=\"M280 283.2 c0 -1.5 0.4 -3.2 1 -3.8 0.5 -0.5 1.1 -5.2 1.3 -10.4 l0.3 -9.5 0.2 9.7 c0.1 5.3 0.7 10 1.2 10.3 0.6 0.3 1 1.9 1 3.6 0 2.4 -0.4 2.9 -2.5 2.9 -2.1 0 -2.5 -0.5 -2.5 -2.8z\"/>\n"
        + "    <path d=\"M253 275 c-6.4 -1.1 -15.4 -4.7 -17.5 -6.9 -1.5 -1.7 -1.5 -2.6 -0.4 -7.6 0.7 -3.1 1.5 -5.8 1.8 -5.9 0.3 -0.2 4 1.6 8.1 4 7.2 4.2 7.8 4.4 14.8 4.3 4 -0.1 7.5 0.1 7.9 0.4 0.9 1 -1.8 10.1 -3.5 11.5 -1.7 1.3 -4.3 1.4 -11.2 0.2z\"/>\n"
        + "    <path d=\"M238.5 252.2 l-13 -7.7 9.5 -0.6 c21.5 -1.5 20.4 -1.7 32.9 5.8 6.2 3.7 10.8 6.9 10.3 7.1 -0.6 0.2 -5.4 -0.9 -10.9 -2.4 -13.4 -3.6 -14.3 -3.8 -14.3 -2.5 0 0.6 0.8 1.1 1.8 1.1 1.9 0 13.7 3 21.2 5.4 4.4 1.4 4.1 1.4 -10 1.5 l-14.5 0.1 -13 -7.8z\"/>\n"
        + "    <path d=\"M311 274.7 c0 -2.2 2.3 -3.7 5.7 -3.7 3.7 0 4.1 0.9 1.5 3.3 -2.2 2 -7.2 2.3 -7.2 0.4z\"/>\n"
        + "    <path d=\"M301.4 269.5 c-0.8 -2.1 0.2 -3.3 3.7 -4 4.4 -1 6.1 1 3 3.6 -2.8 2.3 -5.9 2.5 -6.7 0.4z\"/>\n"
        + "    <path d=\"M324.5 268.6 c-1.9 -1.4 -1.9 -1.5 -0.1 -3.5 2.1 -2.3 8.3 -3.8 9.7 -2.4 1.6 1.6 0 4 -3.6 5.7 -4 1.9 -3.7 1.9 -6 0.2z\"/>\n"
        + "    <path d=\"M299.6 261.2 c-1.4 -2.3 5.7 -5.8 8.5 -4.2 1.1 0.7 1 1.3 -0.5 2.9 -1.9 2.1 -7 2.9 -8 1.3z\"/>\n"
        + "    <path d=\"M307.3 253.4 c-0.8 -2.2 2.2 -4.4 5.9 -4.4 5 0 3.2 4 -2.5 5.4 -2.1 0.5 -2.9 0.2 -3.4 -1z\"/>\n"
        + "    <path d=\"M343.4 250.8 c-1 -1.5 3.2 -4.8 6.2 -4.8 3.3 0 4.1 2 1.4 4.2 -2.5 2 -6.5 2.4 -7.6 0.6z\"/>\n"
        + "    <path d=\"M320.7 247.6 c-1.2 -3.1 7.1 -6.4 8.9 -3.5 0.9 1.6 -1.6 3.8 -5.2 4.5 -2.3 0.4 -3.3 0.2 -3.7 -1z\"/>\n"
        + "    <path d=\"M333.3 246.4 c-0.8 -2.2 2.2 -4.4 5.9 -4.4 3.4 0 3.7 2.2 0.6 4.4 -2.9 2.1 -5.7 2 -6.5 0z\"/>\n"
        + "    <path d=\"M317.7 220.3 c-0.4 -0.3 -0.7 -3 -0.7 -5.8 0 -4 0.5 -5.6 2 -7 1.8 -1.6 1.9 -2 0.5 -2.7 -0.8 -0.5 -1.5 -1.8 -1.5 -2.9 0 -1.9 0.6 -2 11.2 -1.7 9.3 0.3 11.3 0.6 11.6 1.9 0.1 0.9 -0.7 2 -2 2.6 l-2.3 1 2.3 1.2 c1.9 1 2.2 2 2.2 7.6 l0 6.5 -11.3 0 c-6.3 0 -11.7 -0.3 -12 -0.7z m19.1 -2.2 c0.8 -0.5 1.2 -2.2 1 -4.2 l-0.3 -3.4 -8.2 -0.3 -8.3 -0.3 0 3.9 c0 2.1 0.3 4.2 0.7 4.5 0.9 1 13.6 0.8 15.1 -0.2z\"/>\n"
        + "    <path d=\"M328.7 193.5 c-0.3 -2.2 -1.1 -6 -1.9 -8.4 -1 -3.5 -1 -4.5 0 -4.8 0.6 -0.3 1.2 -2 1.2 -3.9 0 -2.7 0.9 -4.3 4.3 -7.6 4.6 -4.5 13.2 -8.8 17.6 -8.8 l2.7 0 -2.8 6 c-1.6 3.2 -3.3 6.2 -3.7 6.5 -0.5 0.3 -0.7 1.3 -0.4 2.4 0.6 2.3 -3.3 10.1 -5 10.1 -0.7 0 -1.8 0.9 -2.4 1.9 -0.7 1.1 -2.6 2.2 -4.3 2.6 -2.2 0.5 -3.2 1.4 -3.6 3.3 -0.3 1.5 -0.7 3.2 -0.8 3.7 -0.2 0.6 -0.6 -0.8 -0.9 -3z\"/>\n"
        + "    <path d=\"M144.6 230.8 c-2.3 -6.6 -6.8 -11.1 -13.4 -13.4 -2.9 -1 -5.2 -2.2 -5.2 -2.5 0 -0.4 2 -1.2 4.6 -1.8 6.1 -1.6 12.6 -8 14.3 -14.4 1.5 -5.3 2.6 -5.9 3.6 -1.9 1.9 7.9 8.9 15 16.3 16.7 1.7 0.3 3.2 1 3.2 1.3 0 0.4 -2.2 1.5 -5 2.5 -6.6 2.3 -11.3 7 -13.6 13.5 -1 2.8 -2.1 5.2 -2.4 5.2 -0.3 0 -1.4 -2.3 -2.4 -5.2z\"/>\n"
        + "    <path d=\"M433.6 230.8 c-2.3 -6.5 -7 -11.2 -13.6 -13.5 -2.8 -1 -5 -2.1 -5 -2.4 0 -0.4 2 -1.2 4.5 -1.8 6 -1.5 11.7 -7.1 14.1 -13.8 1 -2.9 2.1 -5.3 2.4 -5.3 0.3 0 1.4 2.4 2.4 5.3 2.3 6.6 8.1 12.3 14.2 13.9 2.4 0.7 4.4 1.5 4.4 1.8 0 0.3 -2.3 1.4 -5.2 2.4 -6.6 2.3 -11.1 6.8 -13.4 13.4 -1 2.9 -2.1 5.2 -2.4 5.2 -0.3 0 -1.4 -2.4 -2.4 -5.2z\"/>\n"
        + "    <path d=\"M164 180.6 c-8.5 -2.4 -20.9 -5.8 -27.5 -7.5 -6.6 -1.8 -12.4 -3.6 -12.8 -4 -0.5 -0.5 0.5 -5 2.2 -10.1 22.1 -65.9 84.3 -113.8 153.1 -117.8 53.3 -3.2 99.6 14.2 136.5 51.2 19.8 19.9 33.5 42.2 42.1 68.6 1.4 4.5 2.4 8.3 2.2 8.5 -0.6 0.5 -58.1 15.6 -58.4 15.3 -0.1 -0.2 -1.6 -4 -3.3 -8.5 -14.1 -38.4 -45.5 -64.6 -87.6 -73.4 -7.7 -1.6 -30.3 -1.6 -38 0 -32.3 6.7 -58.2 23.5 -75.2 48.6 -5.3 7.8 -11.5 20.1 -13.9 27.8 -1 3.1 -2.2 5.7 -2.8 5.6 -0.6 0 -8.1 -2 -16.6 -4.3z m-1 -21.3 c0 -0.7 -1 -1.3 -2.2 -1.3 -4 0 -5.3 -1.5 -4 -4.4 1.3 -3 2.8 -3.2 5.9 -1.1 2 1.4 2.3 1.4 3.3 -0.6 1.1 -1.9 0.7 -3.4 -0.8 -3.4 -1.7 0 -10.9 -3.9 -12.4 -5.2 -1.4 -1.3 -1.8 -1.2 -2.7 0.6 -1.7 3.1 -1.3 3.9 1.6 4.3 3.2 0.3 4 2.5 2 5.3 -1.4 1.8 -1.5 1.8 -4.2 0.1 -2.3 -1.5 -2.9 -1.6 -3.6 -0.5 -1.7 2.7 -0.9 3.5 5.7 5.9 3.6 1.4 7.1 3.1 7.7 3.8 0.9 1.1 1.3 1.1 2.4 -0.4 0.7 -1 1.3 -2.4 1.3 -3.1z m262.3 4.1 c0.3 -0.4 -0.1 -1.7 -1 -3 -1.7 -2.6 -1.2 -5.4 1.1 -5.4 0.7 0 1.6 0.9 1.9 2.1 0.5 1.8 0.9 1.9 2.7 0.9 1.7 -0.9 1.9 -1.4 0.9 -2.6 -1 -1.2 -1 -1.7 0 -2.7 1 -0.9 1.5 -0.8 2.7 0.7 0.8 1.1 1.4 2.6 1.4 3.3 0 0.7 0.7 1.3 1.5 1.3 1.9 0 1.9 -2 -0.1 -7 -1.6 -3.9 -3.1 -4.9 -4.9 -3.1 -0.6 0.5 -3.7 1.9 -7 3 -6.6 2.2 -6.8 2.7 -4 9.4 1.4 3.4 3.3 4.6 4.8 3.1z m-1.5 -15 c2.1 -1.4 2.1 -1.7 0.7 -4.5 -1.9 -3.6 -3.5 -3.8 -3.5 -0.4 0 2.8 -2.1 3.3 -4 1 -2.8 -3.4 5 -9.3 8.9 -6.8 0.9 0.6 1.5 1.8 1.5 2.6 -0.2 1.8 1.8 2.8 3.4 1.8 1.8 -1.1 -1.9 -7.4 -4.9 -8.5 -3.3 -1.1 -6.8 -0.3 -10.2 2.5 -3.6 2.8 -3.7 8 -0.2 11.4 2.8 2.9 5.1 3.1 8.3 0.9z m-251.8 -7.4 c0.9 -1.7 0.6 -2.2 -2.4 -3.4 -2.4 -1 -4.4 -3 -6.4 -6.6 l-3 -5.2 -1.7 2.6 c-1.7 2.5 -1.7 2.7 0.6 4.5 2.2 1.8 2.2 2 0.6 2.6 -0.9 0.4 -2.4 0.2 -3.2 -0.5 -1.1 -0.9 -1.7 -0.6 -2.9 1.9 l-1.6 3 5 -0.6 c5.3 -0.6 10.5 0.9 11.4 3.4 0.7 1.7 2.2 1 3.6 -1.7z m239 -7.5 c-1.1 -1.2 -2 -2.7 -2 -3.3 0 -2.2 3 -2.5 4.5 -0.6 1.1 1.5 1.6 1.6 2.6 0.7 1 -1 1 -1.5 0 -2.7 -0.7 -0.8 -1 -2 -0.6 -2.6 1.2 -1.9 3.5 -1 4.1 1.6 0.4 1.5 1.2 2.4 2.2 2.2 2.6 -0.5 2.4 -2.2 -0.8 -6.6 l-3 -4.2 -4.6 3.5 c-6.4 4.9 -8.4 5.9 -8.4 4.3 0 -0.8 -0.9 -2.1 -2 -3.1 -1.1 -0.9 -2 -2.1 -2 -2.6 0 -1 8.9 -8.1 10.2 -8.1 1.2 0 0.9 -1.2 -0.7 -3.4 -1.3 -1.7 -1.6 -1.6 -5.7 2.4 -5 4.8 -7.8 6.6 -7.8 5 0 -0.6 -1.2 -2.1 -2.6 -3.3 l-2.6 -2.3 5.1 -4.7 c5.5 -5.2 5.8 -5.8 3.7 -7.6 -1.2 -1 -2.6 0 -7.5 5.1 -3.4 3.4 -6.1 6.5 -6.1 6.9 0 0.4 1.9 2.5 4.3 4.6 3.9 3.4 8 8.5 16.1 19.6 2.3 3.1 3 3.6 4.2 2.6 1.3 -1 1.2 -1.5 -0.6 -3.4z m-233.1 -2.4 c0.8 -1.5 0.6 -2 -1.5 -3 -2.4 -1 -2.4 -1.2 -0.9 -4 0.8 -1.7 1.5 -3.9 1.5 -5 0 -2.2 -3.5 -5.1 -6.1 -5.1 -1.6 0 -8.9 7.4 -8.9 9 0 0.3 2.7 2.4 6 4.5 3.3 2.1 6 4.2 6 4.7 0 1.6 2.9 0.8 3.9 -1.1z m12.2 -15 c1.6 -1.6 2.9 -3.7 2.9 -4.6 0 -4.5 -6.3 -10.5 -10.9 -10.5 -3.4 0 -7.1 4.4 -7.1 8.4 0 2.6 0.8 4.1 3.5 6.5 4.5 3.9 7.8 4 11.6 0.2z m11.4 -10.5 c1.7 -1.2 1.6 -1.6 -2.8 -6.2 -5 -5.1 -5.6 -6.8 -3.2 -8.1 0.9 -0.5 1.1 -1.3 0.6 -2.2 -0.6 -0.9 -1.4 -1 -2.3 -0.5 -2.8 1.6 -8.8 7.3 -8.8 8.3 0 1.9 2.2 2.3 3.4 0.6 1.2 -1.6 1.6 -1.4 5.4 2.6 2.3 2.4 4.2 5 4.2 5.7 0 1.6 1.2 1.5 3.5 -0.2z m188.5 -3.5 c3.8 -3.8 4.7 -7.4 2.6 -10.7 -3.9 -5.8 -10.2 -5.6 -14.3 0.5 -3 4.4 -2.9 7.6 0.2 10.6 3.4 3.5 7.8 3.3 11.5 -0.4z m-180.1 -3.5 c1 -1.2 0.8 -1.9 -0.8 -3.5 -2.6 -2.6 -2.6 -3.3 0 -5.7 2.1 -1.8 2.2 -1.8 3.3 -0.1 2.9 4.8 3.2 5 5.4 3.8 2.1 -1.1 2.1 -1.2 -2.2 -6.9 -2.3 -3.1 -4.6 -6.5 -4.9 -7.5 -0.5 -1.4 -1.1 -1.6 -2.7 -0.7 -2.4 1.3 -2.5 1.8 -0.3 4.2 1.3 1.5 1.5 2.3 0.6 3.6 -1.5 2.4 -4.6 2.1 -5.8 -0.5 -1.1 -2.7 -1.4 -2.7 -3.8 -0.9 -1.7 1.3 -1.6 1.7 1.9 6.3 2 2.6 4.1 5.8 4.7 7.1 1.2 2.5 2.9 2.8 4.6 0.8z m164.2 -4.1 c0 -0.5 -1.2 -1.1 -2.6 -1.3 -4.8 -0.6 -5.4 -5.3 -1.2 -9.9 2.5 -2.8 4.3 -2.9 5.7 -0.3 1.3 2.4 2.8 2.6 3.7 0.3 0.9 -2.3 -3.2 -5.3 -7.1 -5.3 -4.3 0 -9.6 5.8 -9.6 10.5 0 2.8 5.7 7.7 8.5 7.3 1.4 -0.2 2.6 -0.7 2.6 -1.3z m-142.2 -9.1 c0.2 -0.2 0.1 -1 -0.3 -1.9 -0.5 -1.5 -1 -1.4 -3.7 0.6 -2.6 1.8 -3.3 1.9 -4 0.8 -1.3 -2.1 -1.1 -3.9 0.6 -3.9 1.7 0 2 -2.6 0.5 -3.5 -0.6 -0.4 -1.7 -0.2 -2.4 0.4 -0.9 0.7 -1.6 0.7 -2.4 -0.1 -1.8 -1.8 -1.5 -2.5 1.8 -3.7 3 -1 3.8 -2.5 2 -3.6 -0.7 -0.4 -8.2 2.6 -9.8 4 -0.2 0.2 1.3 3.9 3.5 8.3 l3.8 7.9 5 -2.5 c2.8 -1.3 5.2 -2.6 5.4 -2.8z m117.6 -5.6 c0.6 -2 2.8 -4.4 5.8 -6.4 l4.9 -3.4 -2.7 -1.2 c-2.3 -1 -2.9 -1 -4 0.5 -1.9 2.6 -3.8 2.1 -3.1 -0.8 0.5 -2 0.2 -2.7 -1.9 -3.7 -1.4 -0.6 -2.7 -0.9 -2.9 -0.7 -0.3 0.2 0.1 2.6 0.7 5.2 0.9 4 0.8 5.3 -0.7 8.4 -1.7 3.5 -1.7 3.8 0 5 2.1 1.5 2.5 1.2 3.9 -2.9z m-109.3 2.2 c1.1 0 1.1 0 -2.6 -8.3 l-2.6 -5.8 2.6 -0.9 c2.5 -1 2.5 -3 0 -3 -2.8 0 -11.6 4.3 -11.6 5.7 0 1.1 0.7 1.3 2.9 0.8 2.8 -0.6 3 -0.4 5.2 6 1.6 4.4 2.8 6.4 3.7 6 0.8 -0.3 1.8 -0.5 2.4 -0.5z m9.1 -3.6 c1.3 -0.4 1.3 -0.9 0.1 -3.2 -0.7 -1.5 -1.9 -5.1 -2.7 -8 -1.4 -5.5 -1.6 -5.7 -4.7 -4.9 -1.8 0.5 -1.8 0.7 -0.4 3.5 0.8 1.7 2 5.3 2.7 8.1 1.1 5 2 5.7 5 4.5z m91.6 -13.2 c1.7 0.2 2.6 -0.2 2.6 -1.1 0 -1.4 -11.2 -5.4 -12.7 -4.5 -1.6 1 -0.7 3.4 1.3 3.4 1.9 0 2.1 0.4 1.4 3.8 -0.3 2 -1.3 5 -2.1 6.6 -1.8 3.7 -1.8 3.5 0.9 4.5 2.1 0.8 2.3 0.4 4.1 -6.1 1.7 -6.4 2 -6.8 4.5 -6.6z m-78.1 9.2 c2.2 -1.5 3 -4.4 1.3 -4.4 -0.5 0 -1.4 0.7 -2.1 1.5 -1.8 2.2 -5.7 1.9 -7 -0.5 -0.6 -1.2 -1 -3.8 -0.8 -5.8 0.3 -3.2 0.6 -3.7 2.8 -3.6 2.3 0.1 3.9 -1 4 -2.8 0 -0.5 -1.6 -0.8 -3.5 -0.8 -6.5 0 -10.4 6.5 -7.4 12.3 3 5.8 8 7.4 12.7 4.1z m62.8 -1.4 c0.1 -1.4 0.8 -4.5 1.4 -7 1.7 -6.8 1.7 -7.5 -0.5 -7.8 -1.7 -0.2 -2.2 0.7 -3.6 6.5 -0.9 3.7 -1.9 7.5 -2.3 8.5 -0.5 1.1 -0.2 1.8 1.1 2.1 2.8 0.7 3.6 0.3 3.9 -2.3z m-55.2 0.4 c0.9 -0.3 1.6 -1.4 1.6 -2.4 0 -2 3.3 -3.6 5 -2.5 0.5 0.3 1 1.3 1 2.1 0 1.1 1.8 1.4 9 1.4 8.2 0 9 -0.2 9 -1.9 0 -1.7 -0.5 -1.9 -3.6 -1.4 l-3.6 0.6 0.4 -7.7 c0.3 -7.6 0.3 -7.6 -2.2 -7.6 -2.5 0 -2.5 0 -2.2 8 0.5 10.5 -1.2 10.7 -6.2 0.4 -3.3 -6.9 -3.9 -7.5 -6.4 -7.2 -1.9 0.2 -2.6 0.8 -2.4 2 0.3 1.7 -1.9 11.8 -3.3 15.1 -0.8 1.8 0.8 2.3 3.9 1.1z m46 -0.4 c3.7 -1.4 3.3 -3.3 -0.4 -2.5 -4.1 0.9 -6 -0.8 -6 -5.6 0 -2.8 0.6 -4.2 2.2 -5.3 1.9 -1.4 2.4 -1.4 3.9 -0.1 1.4 1.4 1.8 1.4 2.8 0.1 1.7 -2 0.8 -3.2 -2.7 -3.9 -6.4 -1.3 -10.2 2.4 -10.2 10.1 0 3.8 1.6 6.2 5 7.5 2.2 0.8 2.6 0.8 5.4 -0.3z\"/>\n"
        + "    <path d=\"M168.5 122.6 c-1.9 -1.4 -1.9 -1.6 -0.3 -3.2 2.2 -2.2 5 -1.1 4.6 1.8 -0.4 2.7 -1.9 3.2 -4.3 1.4z\"/>\n"
        + "    <path d=\"M182.6 113.9 c-3.7 -2.9 -5 -5.7 -3.5 -7.5 1.7 -2.1 4.7 -1.7 7.4 1.1 4.9 4.8 1.4 10.6 -3.9 6.4z\"/>\n"
        + "    <path d=\"M381.2 100.8 c-3 -3 1.1 -10.1 5.7 -9.6 3.6 0.4 3.9 3.7 0.6 7.7 -2.9 3.3 -4.4 3.8 -6.3 1.9z\"/>\n"
        + "    <path d=\"M273 64.5 c0 -0.8 0.5 -1.5 1 -1.5 0.6 0 1 0.7 1 1.5 0 0.8 -0.4 1.5 -1 1.5 -0.5 0 -1 -0.7 -1 -1.5z\"/>\n"
        + "    <path d=\"M208 146.1 c0 -0.5 4.2 -4.9 9.3 -9.7 14.5 -13.8 29.6 -21.9 50.2 -27 8.2 -2 12.5 -2.5 24 -2.5 12.3 0 15.4 0.3 25.6 2.9 20.5 5.3 37 14.6 51.4 29.1 l8 8.1 -29 0 -29 0 -1.1 -3.4 c-0.8 -2.5 -0.7 -3.8 0.4 -5.4 0.8 -1.2 1.4 -3.8 1.4 -5.9 0 -4.7 -3.7 -8.3 -8.7 -8.3 -2.5 0 -3.7 -0.6 -4.5 -2.1 -1.5 -2.7 -7.3 -5.1 -10.6 -4.3 -2.6 0.7 -2.6 0.6 -1.9 -3.3 0.5 -3.2 0.3 -4.2 -1 -4.7 -2.6 -1 -3.6 0.3 -3.3 4.1 l0.3 3.6 -4.5 0.3 c-3.6 0.2 -5 0.8 -7.1 3.3 -1.8 2.3 -3.4 3.1 -5.7 3.1 -4.7 0 -8.6 3.9 -8.5 8.4 0.1 2 0.7 4.7 1.5 5.9 0.7 1.2 1.2 3.7 1 5.5 l-0.3 3.2 -29 0 c-16 0 -28.9 -0.4 -28.9 -0.9z\"/>\n"
        + "    <path d=\"M269.6 137.8 c-2 -2.8 -2.1 -8.2 -0.2 -9.8 3 -2.4 5.8 -0.8 7.3 4.2 0.7 2.6 1.8 5 2.3 5.3 2.3 1.4 0.7 2.5 -3.4 2.5 -3.5 0 -4.8 -0.5 -6 -2.2z\"/>\n"
        + "    <path d=\"M284.2 138.8 c-2.2 -2.9 -4.2 -7.9 -4.2 -10.8 0 -3.9 3.3 -7.2 6.8 -6.8 2.6 0.3 2.7 0.6 2.8 6.3 0.2 9.1 -0.5 12.5 -2.6 12.5 -1 0 -2.2 -0.6 -2.8 -1.2z\"/>\n"
        + "    <path d=\"M293.5 132.7 c-0.9 -9.7 -0.6 -11.1 2.7 -11.5 4.2 -0.5 7.2 3.1 6.5 7.9 -0.6 4.9 -4.3 10.9 -6.7 10.9 -1.5 0 -1.9 -1.1 -2.5 -7.3z\"/>\n"
        + "    <path d=\"M303 139.2 c0 -0.4 0.6 -1.7 1.4 -2.8 0.8 -1.1 1.8 -3.6 2.3 -5.5 1 -3.8 4.2 -5.1 7 -2.8 1.7 1.5 1.3 8 -0.7 10.4 -1.3 1.6 -10 2.2 -10 0.7z\"/>\n"
        + "  </g>\n"
        + "</svg>";
    ThemeConfig logo = new ThemeConfig("logoSvg", "logo");
    logo.setSvgData(logoSvg);
    themeConfigRepository.save(logo);
  }



  @Transactional
  protected void initializePrograms(String path) {
    initializeData(
        path,
        record -> {
          Optional<Program> optionalProgram = programRepository.findById(
              Integer.parseInt(record.get("id")));
          if (optionalProgram.isPresent()) {
            return optionalProgram.get()
              .withTitle(record.get("title"))
              .withYear(Year.parse(record.get("year")))
              .withSemester(Program.Semester.valueOf(record.get("semester").toUpperCase()))
              .withApplicationOpen(LocalDate.parse(record.get("applicationOpen")))
              .withApplicationClose(LocalDate.parse(record.get("applicationClose")))
              .withStartDate(LocalDate.parse(record.get("startDate")))
              .withEndDate(LocalDate.parse(record.get("endDate")))
              .withDescription(record.get("description"));
          } else {
            return new Program(
              null,
                record.get("title"),
                Year.parse(record.get("year")),
                Program.Semester.valueOf(record.get("semester").toUpperCase()),
                LocalDate.parse(record.get("applicationOpen")),
                LocalDate.parse(record.get("applicationClose")),
                LocalDate.parse(record.get("documentDeadline")),
                LocalDate.parse(record.get("startDate")),
                LocalDate.parse(record.get("endDate")),
                record.get("description"),
                LocalDate.parse(record.get("documentDeadline")),
                Boolean.parseBoolean(record.get("trackPayment"))
            );
          }
        },
        programRepository
    );
  }

  @Transactional
  protected void resetDatabase() {
    applicationRepository.deleteAll();
    programRepository.deleteAll();
    localUserRepository.deleteAll();
    ssoUserRepository.deleteAll();
    facultyLeadRepository.deleteAll();
    noteRepository.deleteAll();
    documentRepository.deleteAll();
    responseRepository.deleteAll();
    questionRepository.deleteAll();
    roleRepository.deleteAll();
    preReqRepository.deleteAll();
    partnerRepository.deleteAll();
    themeConfigRepository.deleteAll();
    entityManager.createNativeQuery("ALTER SEQUENCE programs_seq RESTART WITH 1").executeUpdate();
  }
}