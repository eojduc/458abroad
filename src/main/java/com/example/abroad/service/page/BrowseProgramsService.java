
package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

@Service
public record BrowseProgramsService(
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  UserService userService
) {
  public GetAllProgramsInfo getProgramInfo(
    HttpSession session,
    String nameFilter
  ) {
    return userService.findUserFromSession(session)
      .map(user -> processAuthorizedRequest(nameFilter, user))
      .orElse(new UserNotFound());
  }
  private GetAllProgramsInfo processAuthorizedRequest(
    String nameFilter,
    User user
  ) {

    return new Success(
      programRepository.findAll().stream()
        .filter(matchesNamePredicate(nameFilter))
        .map(getProgramAndStatus(user))
        .toList(),
      user
    );
  }

  public Function<Program, ProgramAndStatus> getProgramAndStatus(User user) {
    return program -> {
      var applicationStatus = applicationRepository.findByProgramIdAndStudent(program.id(), user.username())
        .map(Application::status)
        .orElse(null);

      return new ProgramAndStatus(
        program,
        switch (applicationStatus) {
          case APPLIED -> ProgramStatus.APPLIED;
          case ELIGIBLE -> ProgramStatus.ELIGIBLE;
          case APPROVED -> ProgramStatus.APPROVED;
          case CANCELLED -> ProgramStatus.CANCELLED;
          case WITHDRAWN -> ProgramStatus.WITHDRAWN;
          case ENROLLED -> program.endDate().isBefore(LocalDate.now()) ? ProgramStatus.COMPLETED : ProgramStatus.ENROLLED;
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
    ProgramStatus status
  ) {
  }


  private Predicate<Program> matchesNamePredicate(String searchTerm) {
    return program -> program.title().toLowerCase().contains(searchTerm.toLowerCase());
    // || program.facultyLead().toLowerCase().contains(searchTerm)
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
