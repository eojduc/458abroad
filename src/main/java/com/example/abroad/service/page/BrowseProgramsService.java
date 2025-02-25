
package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record BrowseProgramsService(
    ProgramRepository programRepository,
    ApplicationRepository applicationRepository,
    FacultyLeadRepository facultyLeadRepository,
    UserService userService
) {

  public GetAllProgramsInfo getProgramInfo(
      HttpSession session,
      String nameFilter,
      String leadFilter
  ) {
    return userService.findUserFromSession(session)
        .map(user -> processAuthorizedRequest(nameFilter, leadFilter, user))
        .orElse(new UserNotFound());
  }

  public List<String> getKnownFacultyLeads() {
    return facultyLeadRepository.findAll().stream()
        .map(FacultyLead::username)
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  // Default: sort by application close date. If the dates are the same, sort by program title.
  // Programs whose application close date is in the past are at the end of the list.
  private Comparator<ProgramAndStatus> getStudentDateComparator() {
    LocalDate today = LocalDate.now();
    return Comparator.comparing((ProgramAndStatus pas) -> pas.program().applicationClose().isBefore(today))
        .thenComparing(pas -> pas.program().applicationClose())
        .thenComparing(pas -> pas.program().title());
  }

  private Stream<FacultyLead> getFacultyLeads(int programId) {
    return facultyLeadRepository.findById_ProgramId(programId).stream();
  }

  private GetAllProgramsInfo processAuthorizedRequest(
      String nameFilter,
      String leadFilter,
      User user
  ) {

    return new Success(
        programRepository.findAll().stream()
            .filter(matchesNamePredicate(nameFilter, program -> List.of(program.title())))
            .filter(matchesNamePredicate(leadFilter, program -> extractFacultyLeadUsername(program)))
            .map(getProgramAndStatus(user))
            .sorted(getStudentDateComparator())
            .toList(),
        user
    );
  }

  private List<String> extractFacultyLeadUsername(Program program) {
    return getFacultyLeads(program.id())
        .map(FacultyLead::username)
        .toList();
  }
  private Predicate<Program> matchesNamePredicate(String searchTerm, Function<Program, List<String>> fieldExtractor) {
    return program -> fieldExtractor.apply(program).stream().anyMatch(s -> s.toLowerCase().contains(searchTerm.toLowerCase()));
  }

  public Function<Program, ProgramAndStatus> getProgramAndStatus(User user) {
    return program -> {
      var applicationStatus = applicationRepository.findByProgramIdAndStudent(program.id(),
              user.username())
          .map(Application::status)
          .orElse(null);

      return new ProgramAndStatus(
          program,
          getFacultyLeads(program.id()).map(FacultyLead::username).toList(),
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
