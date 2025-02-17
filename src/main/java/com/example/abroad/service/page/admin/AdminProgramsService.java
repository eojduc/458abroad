
package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramsService(
    ProgramRepository programRepository,
    ApplicationRepository applicationRepository,
    UserService userService
) {

  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsService.class);
  private static final TimeFilter DEFAULT_TIME_FILTER = TimeFilter.FUTURE;
  public enum Sort {
    TITLE,
    SEM_DATE,
    APP_OPENS,
    APP_CLOSES,
    START_DATE,
    END_DATE,
    FACULTY_LEAD,
    APPLIED,
    ENROLLED,
    CANCELED,
    WITHDRAWN,
    COUNT
  }
  public enum TimeFilter {
    FUTURE,
    OPEN,
    REVIEW,
    RUNNING,
    ALL
  }
  public GetAllProgramsInfo getProgramInfo(
    HttpSession session,
    Sort sort,
    String nameFilter,
    TimeFilter timeFilter,
    Boolean ascending
  ) {
    return userService.findUserFromSession(session)
        .map(user -> processAuthorizedRequest(sort, nameFilter, timeFilter, user,
            ascending))
        .orElse(new GetAllProgramsInfo.UserNotFound());
  }

  public void clearSessionData(HttpSession session) {
    session.removeAttribute("nameFilter");
    session.removeAttribute("timeFilter");
    session.removeAttribute("lastSort");
    session.removeAttribute("title");
    session.removeAttribute("appOpens");
    session.removeAttribute("appCloses");
    session.removeAttribute("semDate");
    session.removeAttribute("startDate");
    session.removeAttribute("endDate");
    session.removeAttribute("applied");
    session.removeAttribute("enrolled");
    session.removeAttribute("canceled");
    session.removeAttribute("withdrawn");
    session.removeAttribute("count");
    session.removeAttribute("facultyLead");
    session.removeAttribute("totalActive");
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      Sort sort,
      String nameFilter,
      TimeFilter timeFilter,
      User user,
      Boolean ascending
  ) {
    if (user.role() != User.Role.ADMIN) {
      return new GetAllProgramsInfo.UserNotAdmin();
    }

    List<Program> programs = programRepository.findAll();
    List<Application> allApplications = applicationRepository.findAll();
    var comparator = getSortComparator(sort, allApplications, programs);
    var programsToDisplay = programs.stream()
      .filter(matchesNamePredicate(nameFilter))
      .filter(getTimeFilterPredicate(timeFilter))
      .sorted(ascending ? comparator : comparator.reversed())
      .toList();

    return new GetAllProgramsInfo.Success(
      programsToDisplay,
        allApplications,
        user
    );
  }


  public Map<Integer, Map<String, Integer>> getProgramStatus(List<Application> applications, List<Program> programs) {
    // Group applications by program ID
    Map<Integer, List<Application>> groupedApplications = applications.stream()
        .collect(Collectors.groupingBy(Application::programId));

    // Process in order of programs and store in a map
    return programs.stream()
      .collect(Collectors.toMap(
        Program::id,  // Key: programId
        program -> {
          List<Application> applicationList = groupedApplications.getOrDefault(program.id(), List.of());

          int applied = 0, enrolled = 0, canceled = 0, withdrawn = 0;
          for (Application app : applicationList) {
            switch (app.status()) {
              case APPLIED -> applied++;
              case ENROLLED -> enrolled++;
              case CANCELLED -> canceled++;
              case WITHDRAWN -> withdrawn++;
            }
          }
          int count = applied + enrolled;
          return Map.of(
            "applied", applied,
            "enrolled", enrolled,
            "canceled", canceled,
            "withdrawn", withdrawn,
            "count", count
          );
        }
      ));
  }

  private Predicate<Program> matchesNamePredicate(String searchTerm) {
    return program -> program.title().toLowerCase().contains(searchTerm.toLowerCase());
     // || program.facultyLead().toLowerCase().contains(searchTerm)
  }

  private Predicate<Program> getTimeFilterPredicate(TimeFilter timeFilter) {
    LocalDate today = LocalDate.now();
    return switch (timeFilter) {
      case FUTURE -> program -> !(program.endDate().isBefore(today));
      case OPEN -> program -> !(program.applicationOpen().isBefore(today) || program.applicationClose().isAfter(today));
      case REVIEW -> program -> !(program.applicationClose().isAfter(today) || program.startDate().isBefore(today));
      case RUNNING -> program -> !(program.startDate().isAfter(today) || program.endDate().isBefore(today));
      case ALL -> program -> true;
    };
  }
  private Comparator<Program> getSortComparator(Sort sort, List<Application> applications, List<Program> programs) {
    Map<Integer, Map<String, Integer>> programStatus = getProgramStatus(applications, programs);
    return switch (sort) {
      case TITLE -> Comparator.comparing(Program::title);
      case SEM_DATE -> Comparator.comparing(Program::year).thenComparing(Program::semester);
      case APP_OPENS -> Comparator.comparing(Program::applicationOpen);
      case APP_CLOSES -> Comparator.comparing(Program::applicationClose);
      case START_DATE -> Comparator.comparing(Program::startDate);
      case END_DATE -> Comparator.comparing(Program::endDate);
      case FACULTY_LEAD -> Comparator.comparing(Program::title);
      case APPLIED -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("applied"));
      case ENROLLED -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("enrolled"));
      case CANCELED -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("canceled"));
      case WITHDRAWN -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("withdrawn"));
      case COUNT -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("count"));
    };
  }

  public sealed interface GetAllProgramsInfo permits
      GetAllProgramsInfo.Success,
      GetAllProgramsInfo.UserNotFound,
      GetAllProgramsInfo.UserNotAdmin {

    record Success(
        List<Program> programs,
        List<Application> applicants,
        User user
    ) implements GetAllProgramsInfo {

    }

    record UserNotFound() implements GetAllProgramsInfo {

    }

    record UserNotAdmin() implements GetAllProgramsInfo {

    }
  }
}
