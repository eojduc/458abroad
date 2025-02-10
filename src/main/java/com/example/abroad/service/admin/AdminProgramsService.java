
package com.example.abroad.service.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramsService(
    ProgramRepository programRepository,
    ApplicationRepository applicationRepository,
    UserService userService,
    StudentRepository studentRepository,
    AdminRepository adminRepository
) {

  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsService.class);
  private static final String DEFAULT_TIME_FILTER = "future";

  public GetAllProgramsInfo getProgramInfo(
      HttpSession session,
      String sort,
      String nameFilter,
      String timeFilter,
      boolean studentMode
  ) {
    return userService.getUser(session)
        .map(user -> processAuthorizedRequest(session, sort, nameFilter, timeFilter, user,
            studentMode))
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
      HttpSession session,
      String sort,
      String nameFilter,
      String timeFilter,
      User user,
      boolean studentMode
  ) {
    if (!studentMode && !user.isAdmin()) {
      return new GetAllProgramsInfo.UserNotAdmin();
    }

    List<Program> programs = programRepository.findAll();
    List<Application> studentApplications = applicationRepository.findProgramApplicationsByStudent(
            user.username())
        .stream()
        .map(opt -> opt.orElseGet(
            Application::new)) // Replace empty optionals with a new Application
        .toList();

    List<Application> allApplications = applicationRepository.findAll();

    applyFilters(session, programs, nameFilter, timeFilter);
    applySorting(session, programs, allApplications, sort, studentMode);

    return new GetAllProgramsInfo.Success(
        programs,
        studentMode ? studentApplications : allApplications,
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

  private void applyFilters(
      HttpSession session,
      List<Program> programs,
      String nameFilter,
      String timeFilter
  ) {
    applyNameFilter(session, programs, nameFilter);
    applyTimeFilter(session, programs, timeFilter);
  }

  private void applyNameFilter(HttpSession session, List<Program> programs, String newNameFilter) {
    String storedNameFilter = (String) session.getAttribute("nameFilter");
    String effectiveFilter = Optional.ofNullable(newNameFilter).orElse(storedNameFilter);

    if (effectiveFilter != null) {
      session.setAttribute("nameFilter", effectiveFilter);
      String searchTerm = effectiveFilter.toLowerCase();
      programs.removeIf(program -> !matchesNameFilter(program, searchTerm));
    }
  }

  private boolean matchesNameFilter(Program program, String searchTerm) {
    return program.title().toLowerCase().contains(searchTerm) ||
        program.facultyLead().toLowerCase().contains(searchTerm);
  }

  private void applyTimeFilter(HttpSession session, List<Program> programs, String newTimeFilter) {
    String storedTimeFilter = (String) session.getAttribute("timeFilter");
    String effectiveFilter = Optional.ofNullable(newTimeFilter)
        .orElse(Optional.ofNullable(storedTimeFilter).orElse(DEFAULT_TIME_FILTER));

    session.setAttribute("timeFilter", effectiveFilter);
    programs.removeIf(getTimeFilterPredicate(effectiveFilter));
    logger.info("Filtered programs by {}", effectiveFilter);
  }

  private Predicate<Program> getTimeFilterPredicate(String timeFilter) {
    LocalDate now = LocalDate.now();
    Instant nowInstant = Instant.now();

    return switch (timeFilter) {
      case "future" -> program -> program.endDate().isBefore(now);
      case "open" -> program ->
          program.applicationOpen().isAfter(nowInstant) ||
              program.applicationClose().isBefore(nowInstant);
      case "review" -> program ->
          program.applicationClose().isAfter(nowInstant) ||
              program.startDate().isBefore(now);
      case "running" -> program ->
          program.startDate().isAfter(now) ||
              program.endDate().isBefore(now);
      case "all" -> program -> false;
      default -> program -> false;
    };
  }

  private void applySorting(HttpSession session, List<Program> programs, List<Application> applications, String newSort,
      boolean studentMode) {
    String storedSort = (String) session.getAttribute("lastSort");
    String effectiveSort = Optional.ofNullable(newSort).orElse(storedSort);

    if (effectiveSort != null) {
      session.setAttribute("lastSort", effectiveSort);
      sortPrograms(session, programs, applications, effectiveSort, newSort != null && !studentMode);
    }
  }

  private void sortPrograms(
      HttpSession session,
      List<Program> programs,
      List<Application> applications,
      String sort,
      boolean flipSortOrder
  ) {
    String currentSortDirection = (String) session.getAttribute(sort);
    if (flipSortOrder) {
      session.setAttribute(
          sort,
          "ascending".equals(currentSortDirection) ? "descending" : "ascending"
      );
    }

    Map<Integer, Map<String, Integer>> programStatus = getProgramStatus(applications, programs);

    Comparator<Program> comparator = getSortComparator(sort, programStatus);
    if (comparator != null) {
      if ("descending".equals(session.getAttribute(sort))) {
        comparator = comparator.reversed();
      }
      programs.sort(comparator);
      logger.info("Sorted programs by {}", sort);
    }
  }

  private Comparator<Program> getSortComparator(String sort, Map<Integer, Map<String, Integer>> programStatus) {
    return switch (sort) {
      case "title" -> Comparator.comparing(Program::title);
      case "semDate" -> Comparator.comparing(Program::year)
          .thenComparing(Program::semester);
      case "appOpens" -> Comparator.comparing(Program::applicationOpen);
      case "appCloses" -> Comparator.comparing(Program::applicationClose);
      case "startDate" -> Comparator.comparing(Program::startDate);
      case "endDate" -> Comparator.comparing(Program::endDate);
      case "facultyLead" -> Comparator.comparing(Program::facultyLead);
      case "applied" -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("applied"));
      case "enrolled" -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("enrolled"));
      case "canceled" -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("canceled"));
      case "withdrawn" -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("withdrawn"));
      case "count" -> Comparator.comparingInt(program -> programStatus.get(program.id()).get("count"));
      default -> null;
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
