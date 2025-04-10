
package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramsService(
    ProgramService programService,
    ApplicationService applicationService,
    UserService userService
) {

  static Logger logger = LoggerFactory.getLogger(AdminProgramsService.class);

  public enum Sort {
    TITLE, SEM_DATE, APP_OPENS, APP_CLOSES, START_DATE, END_DATE, ESSENTIAL_DOCS, PAYMENT_DATE, FACULTY_LEAD, APPLIED, ELIGIBLE, ENROLLED,
    APPROVED, CANCELED, WITHDRAWN, TOTAL_ACTIVE, COMPLETED
  }

  public enum TimeFilter {FUTURE, OPEN, REVIEW, RUNNING, ALL}

  public GetAllProgramsInfo getProgramInfo(
      HttpSession session,
      Sort sort,
      String nameFilter,
      List<String> leadFilter,
      TimeFilter timeFilter,
      Boolean ascending
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetAllProgramsInfo.UserNotFound();
    }
    var userIsAdmin = userService.isAdmin(user);
    var userIsReviewer = userService.isReviewer(user);
    var userIsFaculty = userService.isFaculty(user);
    if (!userIsAdmin && !userIsReviewer && !userIsFaculty) {
      return new GetAllProgramsInfo.UserLacksPermission();
    }
    return processAuthorizedRequest(sort, nameFilter, leadFilter, timeFilter, user, ascending);
  }

  public List<? extends  User> getKnownFacultyLeads() {
    return userService.findFacultyLeads();
  }

  public List<Program> getMyPrograms(User user) {
    if (!userService.isFaculty(user) && !userService.isHeadAdmin(user)) {
      logger.error("User {} is not a faculty member", user.username());
      return List.of();
    }
    return programService.findFacultyPrograms(user);
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      Sort sort,
      String nameFilter,
      List<String> leadFilter,
      TimeFilter timeFilter,
      User user,
      Boolean ascending
  ) {
    var programsAndStatuses = programService.findAll()
        .stream()
        .filter(matchesNamePredicate(nameFilter, program -> List.of(program.title())))
        .filter(program -> leadFilter.isEmpty() ||
            extractFacultyLeadUsername(program, User::username).stream().anyMatch(leadFilter::contains))
        .filter(getTimeFilterPredicate(timeFilter))
        .map(getProgramAndStatuses())
        .sorted(getSortComparator(sort, ascending))
        .toList();
    return new GetAllProgramsInfo.Success(
        programsAndStatuses,
        user,
        userService.isAdmin(user)
    );
  }

  public Function<Program, ProgramAndStatuses> getProgramAndStatuses() {
    return program -> {
      var applications = applicationService.findByProgram(program);
      var counts = applications.stream()
          .collect(Collectors.groupingBy(Application::status, Collectors.counting()));
      long completedCount = applications.stream()
          .filter(app -> app.status() == Application.Status.ENROLLED && LocalDate.now().isAfter(program.endDate()))
          .count();
      long enrolledCount = counts.getOrDefault(Application.Status.ENROLLED, 0L) - completedCount;
      long activeCount = applications.stream()
          .filter(app -> app.status() != Application.Status.CANCELLED && app.status() != Application.Status.WITHDRAWN)
          .count();
      return new ProgramAndStatuses(
          program,
          extractFacultyLeadUsername(program, User::displayName),
          counts.getOrDefault(Application.Status.APPLIED, 0L),
          counts.getOrDefault(Application.Status.ELIGIBLE, 0L),
          counts.getOrDefault(Application.Status.APPROVED, 0L),
          enrolledCount,
          counts.getOrDefault(Application.Status.CANCELLED, 0L),
          counts.getOrDefault(Application.Status.WITHDRAWN, 0L),
          completedCount,
          activeCount
      );
    };
  }

  public record ProgramAndStatuses(
      Program program,
      List<String> facultyLeads,
      Long applied,
      Long eligible,
      Long approved,
      Long enrolled,
      Long canceled,
      Long withdrawn,
      Long completed,
      Long totalActive
  ) {

  }

  private List<String> extractFacultyLeadUsername(Program program, Function<User, String> mapper) {
    return programService.findFacultyLeads(program).stream()
        .map(mapper)
        .toList();
  }

  private Predicate<Program> matchesNamePredicate(String searchTerm,
      Function<Program, List<String>> fieldExtractor) {
    return program -> fieldExtractor.apply(program).stream()
        .anyMatch(s -> s.toLowerCase().contains(searchTerm.toLowerCase()));
  }

  private Predicate<Program> getTimeFilterPredicate(TimeFilter timeFilter) {
    LocalDate today = LocalDate.now();
    return switch (timeFilter) {
      case FUTURE -> program -> !(program.endDate().isBefore(today));
      case OPEN ->
          program -> !(program.applicationOpen().isAfter(today) || program.applicationClose()
              .isBefore(today));
      case REVIEW -> program -> !(program.applicationClose().isAfter(today) || program.startDate()
          .isBefore(today));
      case RUNNING ->
          program -> !(program.startDate().isAfter(today) || program.endDate().isBefore(today));
      case ALL -> program -> true;
    };
  }

  private Comparator<ProgramAndStatuses> getSortComparator(Sort sort, Boolean ascending) {
    Comparator<ProgramAndStatuses> comparator = switch (sort) {
      case TITLE ->
          Comparator.comparing(programAndStatuses -> programAndStatuses.program().title());
      case SEM_DATE -> Comparator
          .comparing((ProgramAndStatuses pas) -> pas.program().year(), Comparator.naturalOrder())
          .thenComparing(pas -> pas.program().semester(), Comparator.naturalOrder());
      case APP_OPENS -> Comparator.comparing(
          programAndStatuses -> programAndStatuses.program().applicationOpen());
      case APP_CLOSES -> Comparator.comparing(
          programAndStatuses -> programAndStatuses.program().applicationClose());
      case ESSENTIAL_DOCS -> Comparator.comparing(
          programAndStatuses -> programAndStatuses.program().documentDeadline());
      case PAYMENT_DATE -> Comparator.comparing(
          programAndStatuses -> programAndStatuses.program().paymentDeadline());
      case START_DATE ->
          Comparator.comparing(programAndStatuses -> programAndStatuses.program().startDate());
      case END_DATE ->
          Comparator.comparing(programAndStatuses -> programAndStatuses.program().endDate());
      case FACULTY_LEAD -> Comparator.comparing(
          (ProgramAndStatuses pas) -> programService.findFacultyLeads(pas.program())
              .size()).thenComparing(pas -> programService.findFacultyLeads(pas.program()).getFirst().displayName()); // very slow
      case APPLIED -> Comparator.comparing(ProgramAndStatuses::applied);
      case ELIGIBLE -> Comparator.comparing(ProgramAndStatuses::eligible);
      case APPROVED -> Comparator.comparing(ProgramAndStatuses::approved);
      case ENROLLED -> Comparator.comparing(ProgramAndStatuses::enrolled);
      case CANCELED -> Comparator.comparing(ProgramAndStatuses::canceled);
      case WITHDRAWN -> Comparator.comparing(ProgramAndStatuses::withdrawn);
      case COMPLETED -> Comparator.comparing(ProgramAndStatuses::completed);
      case TOTAL_ACTIVE -> Comparator.comparing(ProgramAndStatuses::totalActive);
    };
    return ascending ? comparator : comparator.reversed();
  }

  public sealed interface GetAllProgramsInfo {

    record Success(
        List<ProgramAndStatuses> programsAndStatuses,
        User user,
        Boolean isAdmin
    ) implements GetAllProgramsInfo {

    }

    record UserNotFound() implements GetAllProgramsInfo {

    }

    record UserLacksPermission() implements GetAllProgramsInfo {

    }
  }
}
