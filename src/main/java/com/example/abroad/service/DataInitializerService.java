package com.example.abroad.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataInitializerService {

  private final DataInitializationService dataInitializationService;

  @Autowired
  public DataInitializerService(DataInitializationService dataInitializationService) {
    this.dataInitializationService = dataInitializationService;
  }

  @PostConstruct
  public void initData() {
    dataInitializationService.initializeStudents();
    dataInitializationService.initializeAdmins();
    dataInitializationService.initializeApplications();
    dataInitializationService.initializePrograms();
  }
}
