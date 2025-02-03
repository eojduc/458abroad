package com.example.abroad.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataInitializerService {

  private final DataInitializationService dataInitializationService;
  private static final Logger logger = LoggerFactory.getLogger(DataInitializerService.class);

  @Value("${data.students.path}")
  private String studentCsvFilePath;

  @Value("${data.admins.path}")
  private String adminCsvFilePath;

  @Value("${data.applications.path}")
  private String applicationCsvFilePath;

  @Value("${data.programs.path}")
  private String programCsvFilePath;

  @Value("${fillDb:false}")
  private boolean fillDb;

  @Autowired
  public DataInitializerService(DataInitializationService dataInitializationService) {
    this.dataInitializationService = dataInitializationService;
  }

  @PostConstruct
  public void initData() {
    if (fillDb) {
      dataInitializationService.initializeStudents(studentCsvFilePath);
      dataInitializationService.initializeAdmins(adminCsvFilePath);
      dataInitializationService.initializeApplications(applicationCsvFilePath);
      dataInitializationService.initializePrograms(programCsvFilePath);
    } else {
      logger.info("Skipping data initialization.");
    }
  }
}
