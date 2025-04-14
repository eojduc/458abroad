
package com.example.abroad.service.page.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.student.BrowseProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.student.BrowseProgramsService.GetAllProgramsInfo.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record BrowseProgramsService(
    ProgramService programService,
    ApplicationService applicationService,
    UserService userService
) {

  public GetAllProgramsInfo getProgramInfo(
      HttpSession session,
      String nameFilter,
      List<String> leadFilter
  ) {
    return userService.findUserFromSession(session)
        .map(user -> processAuthorizedRequest(nameFilter, leadFilter, user))
        .orElse(new UserNotFound());
  }

  public List<? extends  User> getKnownFacultyLeads() {
    return userService.findFacultyLeads();
  }

  // Default: sort by application close date. If the dates are the same, sort by program title.
  // Programs whose application close date is in the past are at the end of the list.
  private Comparator<ProgramAndStatus> getStudentDateComparator() {
    LocalDate today = LocalDate.now();
    return Comparator.comparing((ProgramAndStatus pas) -> pas.program().applicationClose().isBefore(today))
        .thenComparing(pas -> pas.program().applicationClose())
        .thenComparing(pas -> pas.program().title());
  }

  private Stream<? extends User> getFacultyLeads(int programId) {
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return Stream.empty();
    }
    return programService.findFacultyLeads(program).stream();
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      String nameFilter,
      List<String> leadFilter,
      User user
  ) {

    return new Success(
        programService.findAll().stream()
            .filter(matchesNamePredicate(nameFilter, program -> List.of(program.title())))
            .filter(program -> leadFilter.isEmpty() ||
                extractFacultyLeadUsername(program, User::username).stream().anyMatch(leadFilter::contains))
            .filter(program -> program.endDate().isAfter(LocalDate.now())) // Hide programs that have ended
            .map(getProgramAndStatus(user))
            .sorted(getStudentDateComparator())
            .toList(),
        user
    );
  }

  private List<String> extractFacultyLeadUsername(Program program, Function<User, String> mapper) {
    return getFacultyLeads(program.id())
        .map(mapper)
        .toList();
  }
  private Predicate<Program> matchesNamePredicate(String searchTerm, Function<Program, List<String>> fieldExtractor) {
    return program -> fieldExtractor.apply(program).stream().anyMatch(s -> s.toLowerCase().contains(searchTerm.toLowerCase()));
  }

  public Function<Program, ProgramAndStatus> getProgramAndStatus(User user) {
    return program -> {
      var applicationStatus = applicationService.findByProgramAndStudent(program, user)
          .map(Application::status)
          .orElse(null);

      return new ProgramAndStatus(
          program,
          getFacultyLeads(program.id()).map(User::displayName).toList(),
          switch (applicationStatus) {
            case APPLIED -> ProgramStatus.APPLIED;
            case ELIGIBLE -> ProgramStatus.ELIGIBLE;
            case APPROVED -> ProgramStatus.APPROVED;
            case CANCELLED -> ProgramStatus.CANCELLED;
            case WITHDRAWN -> ProgramStatus.WITHDRAWN;
            case ENROLLED -> program.endDate().isBefore(LocalDate.now()) ? ProgramStatus.COMPLETED
                : ProgramStatus.ENROLLED;
            case null -> {
              if (program.applicationOpen().isAfter(LocalDate.now())) {
                yield ProgramStatus.UPCOMING;
              } else if (program.applicationClose().isBefore(LocalDate.now())) {
                yield ProgramStatus.CLOSED;
              } else {
                yield ProgramStatus.OPEN;
              }
            }
          }
      );
    };
  }

  public enum ProgramStatus {
    APPLIED,
    ENROLLED,
    CANCELLED,
    WITHDRAWN,
    ELIGIBLE,
    APPROVED,
    COMPLETED,
    UPCOMING,
    OPEN,
    CLOSED
  }

  public record ProgramAndStatus(
      Program program,
      List<String> facultyLeads,
      ProgramStatus status
  ) {

  }

  public sealed interface GetAllProgramsInfo {

    record Success(
        List<ProgramAndStatus> programAndStatuses,
        User user
    ) implements GetAllProgramsInfo {

    }

    record UserNotFound() implements GetAllProgramsInfo {

    }

    record UserNotAdmin() implements GetAllProgramsInfo {

    }
  }
}
