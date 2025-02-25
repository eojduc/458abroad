
package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramsService(
    ProgramRepository programRepository,
    FacultyLeadRepository facultyLeadRepository,
    ApplicationRepository applicationRepository,
    UserService userService
) {
  static Logger logger = LoggerFactory.getLogger(AdminProgramsService.class);
  public enum Sort {
    TITLE, SEM_DATE, APP_OPENS, APP_CLOSES, START_DATE, END_DATE, FACULTY_LEAD, APPLIED, ENROLLED,
    CANCELED, WITHDRAWN, TOTAL_ACTIVE
  }
  public enum TimeFilter { FUTURE, OPEN, REVIEW, RUNNING, ALL }
  public GetAllProgramsInfo getProgramInfo(
    HttpSession session,
    Sort sort,
    String nameFilter,
    String leadFilter,
    TimeFilter timeFilter,
    Boolean ascending
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetAllProgramsInfo.UserNotFound();
    }
    if (user.role() != User.Role.ADMIN) {
      return new GetAllProgramsInfo.UserNotAdmin();
    }
    return processAuthorizedRequest(sort, nameFilter, leadFilter, timeFilter, user, ascending);
  }

  public List<String> getKnownFacultyLeads() {
    return facultyLeadRepository.findAll().stream()
        .map(FacultyLead::username)
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      Sort sort,
      String nameFilter,
      String leadFilter,
      TimeFilter timeFilter,
      User user,
      Boolean ascending
  ) {
    var programsAndStatuses = programRepository.findAll()
      .stream()
      .filter(matchesNamePredicate(nameFilter, program -> List.of(program.title())))
      .filter(matchesNamePredicate(leadFilter, program -> extractFacultyLeadUsername(program)))
      .filter(getTimeFilterPredicate(timeFilter))
      .map(getProgramAndStatuses())
      .sorted(getSortComparator(sort, ascending))
      .toList();
    return new GetAllProgramsInfo.Success(
      programsAndStatuses,
        user
    );
  }

  public Function<Program, ProgramAndStatuses> getProgramAndStatuses() {
    return program -> {
      var applications = applicationRepository.findByProgramId(program.id());
      var counts = applications.stream()
          .collect(Collectors.groupingBy(Application::status, Collectors.counting()));
      return new ProgramAndStatuses(
          program,
          getFacultyLeads(program.id()).map(FacultyLead::username).toList(),
          counts.getOrDefault(Application.Status.APPLIED, 0L),
          counts.getOrDefault(Application.Status.ENROLLED, 0L),
          counts.getOrDefault(Application.Status.CANCELLED, 0L),
          counts.getOrDefault(Application.Status.WITHDRAWN, 0L),
          (long) applications.size()
      );
    };
  }

  public record ProgramAndStatuses(
    Program program,
    List<String> facultyLeads,
    Long applied,
    Long enrolled,
    Long canceled,
    Long withdrawn,
    Long totalActive
  ) {
  }

  private Stream<FacultyLead> getFacultyLeads(int programId) {
    return facultyLeadRepository.findById_ProgramId(programId).stream();
  }

  private List<String> extractFacultyLeadUsername(Program program) {
    return getFacultyLeads(program.id())
        .map(FacultyLead::username)
        .toList();
  }
  private Predicate<Program> matchesNamePredicate(String searchTerm, Function<Program, List<String>> fieldExtractor) {
    return program -> fieldExtractor.apply(program).stream().anyMatch(s -> s.toLowerCase().contains(searchTerm.toLowerCase()));
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
  private Comparator<ProgramAndStatuses> getSortComparator(Sort sort, Boolean ascending) {
    Comparator<ProgramAndStatuses> comparator =  switch (sort) {
      case TITLE -> Comparator.comparing(programAndStatuses -> programAndStatuses.program().title());
      case SEM_DATE -> Comparator
        .comparing((ProgramAndStatuses pas) -> pas.program().year(), Comparator.naturalOrder())
        .thenComparing(pas -> pas.program().semester(), Comparator.naturalOrder());
      case APP_OPENS -> Comparator.comparing(programAndStatuses -> programAndStatuses.program().applicationOpen());
      case APP_CLOSES -> Comparator.comparing(programAndStatuses -> programAndStatuses.program().applicationClose());
      case START_DATE -> Comparator.comparing(programAndStatuses -> programAndStatuses.program().startDate());
      case END_DATE -> Comparator.comparing(programAndStatuses -> programAndStatuses.program().endDate());
      case FACULTY_LEAD -> Comparator.comparing(programAndStatuses -> facultyLeadRepository.findById_ProgramId(programAndStatuses.program().id()).size());
      case APPLIED -> Comparator.comparing(ProgramAndStatuses::applied);
      case ENROLLED -> Comparator.comparing(ProgramAndStatuses::enrolled);
      case CANCELED -> Comparator.comparing(ProgramAndStatuses::canceled);
      case WITHDRAWN -> Comparator.comparing(ProgramAndStatuses::withdrawn);
      case TOTAL_ACTIVE -> Comparator.comparing(ProgramAndStatuses::totalActive);
    };
    return ascending ? comparator : comparator.reversed();
  }

  public sealed interface GetAllProgramsInfo {

    record Success(
        List<ProgramAndStatuses> programsAndStatuses,
        User user
    ) implements GetAllProgramsInfo {
    }
    record UserNotFound() implements GetAllProgramsInfo {
    }
    record UserNotAdmin() implements GetAllProgramsInfo {
    }
  }
}
