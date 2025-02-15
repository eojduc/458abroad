package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public record ListApplicationsService(
    ApplicationRepository applicationRepository,
    ProgramRepository programRepository,
    UserService userService) {

  public GetApplicationsResult getApplications(HttpSession session, String sort) {

    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetApplicationsResult.UserNotFound();
    }

    // retrieve student applications
    List<Application> applications = applicationRepository
        .findProgramApplicationsByStudent(user.username())
        .stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    // build parallel lists if corresponding program is found
    List<Application> filteredApps = new ArrayList<>();
    List<Program> programs = new ArrayList<>();
    for (Application app : applications) {
      Optional<Program> progOpt = programRepository.findById(app.programId());
      if (progOpt.isPresent()) {
        filteredApps.add(app);
        programs.add(progOpt.get());
      }
    }

    List<Pair> combined = new ArrayList<>();
    for (int i = 0; i < filteredApps.size(); i++) {
      combined.add(new Pair(filteredApps.get(i), programs.get(i)));
    }
    switch (sort.toLowerCase()) {
      case "title":
        combined.sort(Comparator.comparing(pair -> pair.prog().title()));
        break;
      case "yearsemester":
        combined.sort(Comparator.comparing(pair -> pair.prog().year().toString()
            + pair.prog().semester().toString()));
        break;
      case "faculty":
//        combined.sort(Comparator.comparing(pair -> pair.prog().facultyLead()));
        break;
      case "startdate":
        combined.sort(Comparator.comparing(pair -> pair.prog().startDate()));
        break;
      case "enddate":
        combined.sort(Comparator.comparing(pair -> pair.prog().endDate()));
        break;
      case "applicationopen":
        combined.sort(Comparator.comparing(pair -> pair.prog().applicationOpen()));
        break;
      case "applicationclosed":
        combined.sort(Comparator.comparing(pair -> pair.prog().applicationClose()));
        break;
      case "status":
        combined.sort(Comparator.comparing(pair -> pair.app().status().toString()));
        break;
      default:
        combined.sort(Comparator.comparing(pair -> pair.prog().title()));
        break;
    }

    return new GetApplicationsResult.Success(combined, user);
  }

  public record Pair(Application app, Program prog) {
  }

  public sealed interface GetApplicationsResult {

    record Success(List<Pair> data, User user) implements GetApplicationsResult {
    }

    record UserNotFound() implements GetApplicationsResult {
    }
  }
}