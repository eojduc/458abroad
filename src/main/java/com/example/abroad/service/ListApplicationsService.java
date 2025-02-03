package com.example.abroad.service;

import com.example.abroad.controller.student.BrowseProgramsController;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public record ListApplicationsService(
  ApplicationRepository applicationRepository, 
  ProgramRepository programRepository, 
  UserService userService
) {

  private static final Logger logger = LoggerFactory.getLogger(BrowseProgramsController.class);

  public GetApplicationsResult getApplications(HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetApplicationsResult.UserNotFound();
    }
    List<Application> applications = applicationRepository
      .findProgramApplicationsByStudent(user.username())
      .stream()
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());

    List<Application> filteredApps = new ArrayList<>();
    List<Program> programs = new ArrayList<>();
    for (Application app : applications) {
      Optional<Program> progOpt = programRepository.findById(app.programId());
      if (progOpt.isPresent()) {
        filteredApps.add(app);
        programs.add(progOpt.get());
      }
    }
    return new GetApplicationsResult.Success(filteredApps, programs, user);
  }

  public sealed interface GetApplicationsResult
    permits GetApplicationsResult.Success, GetApplicationsResult.UserNotFound {

    record Success(List<Application> applications, List<Program> programs, User user) implements GetApplicationsResult {}
    record UserNotFound() implements GetApplicationsResult {}
  }
}