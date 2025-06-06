package com.example.abroad.service.data;

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

  @Value("${data.local_users.path}")
  private String localUsersCsvFilePath;

  @Value("${data.sso_users.path}")
  private String ssoUsersCsvFilePath;

  @Value("${data.applications.path}")
  private String applicationCsvFilePath;

  @Value("${data.programs.path}")
  private String programCsvFilePath;

  @Value("${data.faculty_leads.path}")
  private String facultyLeadCsvFilePath;

  @Value("${data.notes.path}")
  private String notesCsvFilePath;

  @Value("${data.documents.path}")
  private String documentsCsvFilePath;

  @Value("${data.responses.path}")
  private String responsesCsvFilePath;

  @Value("${data.questions.path}")
  private String questionsCsvFilePath;

  @Value("${data.roles.path}")
  private String rolesCsvFilePath;

  @Value("${data.prereqs.path}")
  private String preReqCsvFilePath;

  @Value("${data.partners.path}")
  private String partnersCsvFilePath;

  @Value("${data.theme_config.path}")
  private String themeConfigCsvFilePath;

  @Value("${fillDb:false}")
  private boolean fillDb;

  @Value("${resetDb:false}")
  private boolean resetDb;

  @Autowired
  public DataInitializerService(DataInitializationService dataInitializationService) {
    this.dataInitializationService = dataInitializationService;
  }

  @PostConstruct
  public void initData() {
    if (fillDb) {
      if (resetDb) {
        logger.info("Resetting database.");
        dataInitializationService.resetDatabase();
      }
      dataInitializationService.initializeLocalUsers(localUsersCsvFilePath);
      dataInitializationService.initializeSsoUsers(ssoUsersCsvFilePath);
      dataInitializationService.initializeApplications(applicationCsvFilePath);
      dataInitializationService.initializePrograms(programCsvFilePath);
      dataInitializationService.initializeFacultyLeads(facultyLeadCsvFilePath);
      dataInitializationService.initializeNotes(notesCsvFilePath);
      dataInitializationService.initializeDocuments(documentsCsvFilePath);
      dataInitializationService.initializeResponses(responsesCsvFilePath);
      dataInitializationService.initializeQuestions(questionsCsvFilePath);
      dataInitializationService.initializeRoles(rolesCsvFilePath);
      dataInitializationService.initializePreReqs(preReqCsvFilePath);
      dataInitializationService.initializePartners(partnersCsvFilePath);
      dataInitializationService.initializeThemeConfig(themeConfigCsvFilePath);
    } else {
      logger.info("Skipping data initialization.");
    }
  }
}
