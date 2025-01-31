
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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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
      String timeFilter
  ) {
    return userService.getUser(session)
        .map(user -> processAuthorizedRequest(session, sort, nameFilter, timeFilter, user))
        .orElse(new GetAllProgramsInfo.UserNotFound());
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      HttpSession session,
      String sort,
      String nameFilter,
      String timeFilter,
      User user
  ) {
    if (!user.isAdmin()) {
      return new GetAllProgramsInfo.UserNotAdmin();
    }

    List<Program> programs = programRepository.findAll();
    applyFilters(session, programs, nameFilter, timeFilter);
    applySorting(session, programs, sort);

    return new GetAllProgramsInfo.Success(
        programs,
        applicationRepository.findAll(),
        user
    );
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
          program.applicationOpen().isBefore(nowInstant) ||
              program.applicationClose().isAfter(nowInstant);
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

  private void applySorting(HttpSession session, List<Program> programs, String newSort) {
    String storedSort = (String) session.getAttribute("lastSort");
    String effectiveSort = Optional.ofNullable(newSort).orElse(storedSort);

    if (effectiveSort != null) {
      session.setAttribute("lastSort", effectiveSort);
      sortPrograms(session, programs, effectiveSort, newSort != null);
    }
  }

  private void sortPrograms(
      HttpSession session,
      List<Program> programs,
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

    Comparator<Program> comparator = getSortComparator(sort);
    if (comparator != null) {
      if ("descending".equals(session.getAttribute(sort))) {
        comparator = comparator.reversed();
      }
      programs.sort(comparator);
      logger.info("Sorted programs by {}", sort);
    }
  }

  private Comparator<Program> getSortComparator(String sort) {
    return switch (sort) {
      case "title" -> Comparator.comparing(Program::title);
      case "semDate" -> Comparator.comparing(Program::year)
          .thenComparing(Program::semester);
      case "startDate" -> Comparator.comparing(Program::startDate);
      case "endDate" -> Comparator.comparing(Program::endDate);
      case "facultyLead" -> Comparator.comparing(Program::facultyLead);
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
