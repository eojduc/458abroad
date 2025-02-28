package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ApplicationService.Documents;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.ProgramNotFound;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.Success;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.UserNotAdmin;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramInfoService(
  ProgramService programService,
  ApplicationService applicationService,
  UserService userService
) {

  public DeleteProgram deleteProgram(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UserNotFound();
    }
    if (!user.isAdmin()) {
      return new UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound();
    }
    programService.deleteProgram(program);
    return new Success();
  }
  public sealed interface GetProgramInfo permits GetProgramInfo.Success,
    GetProgramInfo.ProgramNotFound, GetProgramInfo.UserNotFound,
    GetProgramInfo.UserNotAdmin {

    record Success(Program program, List<Applicant> applicants, User user, Boolean documentDeadlinePassed, Boolean programIsDone, List<? extends User> facultyLeads) implements
      GetProgramInfo {

    }

    record ProgramNotFound() implements GetProgramInfo {

    }

    record UserNotFound() implements GetProgramInfo {

    }

    record UserNotAdmin() implements GetProgramInfo {

    }
  }

  public GetProgramInfo getProgramInfo(Integer programId, HttpSession session,
    Optional<Column> column,
    Optional<Filter> filter,
    Optional<Sort> sort) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetProgramInfo.UserNotFound();
    }
    if (!user.isAdmin()) {
      return new GetProgramInfo.UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfo.ProgramNotFound();
    }
    Comparator<Applicant> sorter = switch (column.orElse(Column.NONE)) {
      case USERNAME, NONE -> Comparator.comparing(Applicant::username);
      case DISPLAY_NAME -> Comparator.comparing(Applicant::displayName);
      case EMAIL -> Comparator.comparing(Applicant::email);
      case MAJOR -> Comparator.comparing(Applicant::major);
      case GPA -> Comparator.comparing(Applicant::gpa);
      case DOB -> Comparator.comparing(Applicant::dob);
      case STATUS -> Comparator.comparing(Applicant::status);
      case NOTE_COUNT -> Comparator.comparing(Applicant::noteCount);
      case LATEST_NOTE -> Comparator.comparing(applicant -> applicant.latestNote().map(Note::timestamp).orElse(Instant.MIN));
      case MEDICAL_HISTORY -> Comparator.comparing(applicant -> applicant.documents().medicalHistory().map(Document::timestamp).orElse(Instant.MIN));
      case CODE_OF_CONDUCT -> Comparator.comparing(applicant -> applicant.documents().codeOfConduct().map(Document::timestamp).orElse(Instant.MIN));
      case ASSUMPTION_OF_RISK -> Comparator.comparing(applicant -> applicant.documents().assumptionOfRisk().map(Document::timestamp).orElse(Instant.MIN));
      case HOUSING -> Comparator.comparing(applicant -> applicant.documents().housing().map(Document::timestamp).orElse(Instant.MIN));
    };
    var reversed = sort.orElse(Sort.ASCENDING) == Sort.DESCENDING;
    var programIsDone = program.endDate().isBefore(LocalDate.now());
    Predicate<Applicant> filterer = switch (filter.orElse(Filter.NONE)) {
      case APPLIED -> applicant -> applicant.status() == Status.APPLIED;
      case ENROLLED -> applicant -> applicant.status() == Status.ENROLLED && !programIsDone;
      case CANCELLED -> applicant -> applicant.status() == Status.CANCELLED;
      case WITHDRAWN -> applicant -> applicant.status() == Status.WITHDRAWN;
      case NONE -> applicant -> true;
      case COMPLETED -> applicant -> applicant.status() == Status.ENROLLED && programIsDone;
      case ELIGIBLE -> applicant -> applicant.status() == Status.ELIGIBLE;
      case APPROVED -> applicant -> applicant.status() == Status.APPROVED;
    };

    var users = userService.findAll();
    var applicants = applicationService.findByProgram(program).stream()
      .flatMap(application -> applicants(users.stream(), application))
      .sorted(reversed ? sorter.reversed() : sorter)
      .filter(filterer)
      .toList();
    var documentDeadlinePassed = LocalDate.now().isAfter(program.documentDeadline());
    var facultyLeads = programService.findFacultyLeads(program);
    return new GetProgramInfo.Success(program, applicants, user, documentDeadlinePassed, programIsDone, facultyLeads);

  }

  private Stream<Applicant> applicants(Stream<? extends User> students, Application application) {
    var documents = applicationService.getDocuments(application);
    var notes = applicationService.getNotes(application);
    var program = programService.findById(application.programId()).orElse(null);
    var displayStatus = switch (application.status()) {
      case ENROLLED -> program.endDate().isBefore(LocalDate.now()) ? "COMPLETED" : "ENROLLED";
      default -> application.status().toString();
    };
    return students.filter(student -> student.username().equals(application.student()))
      .map(student -> new Applicant(
        student.username(),
        student.displayName(),
        student.email(),
        application.major(),
        application.gpa(),
        application.dateOfBirth(),
        application.status(),
        application.id(),
        documents,
        notes.size(),
        notes.stream().max(Comparator.comparing(Note::timestamp)),
        displayStatus
      ));
  }

  public enum Filter {
    APPLIED, ENROLLED, CANCELLED, WITHDRAWN, NONE, COMPLETED, ELIGIBLE, APPROVED
  }

  public enum Column {
    USERNAME, DISPLAY_NAME, EMAIL, MAJOR, GPA, DOB, STATUS, NONE, NOTE_COUNT, LATEST_NOTE, MEDICAL_HISTORY,
    CODE_OF_CONDUCT, ASSUMPTION_OF_RISK, HOUSING
  }
  public enum Sort {
    ASCENDING, DESCENDING
  }

  public sealed interface DeleteProgram permits
    ProgramNotFound,
    UserNotFound,
    UserNotAdmin,
    Success {

    record Success() implements DeleteProgram {

    }

    record ProgramNotFound() implements DeleteProgram {

    }

    record UserNotFound() implements DeleteProgram {

    }

    record UserNotAdmin() implements DeleteProgram {

    }
  }

  public record Applicant(String username, String displayName, String email, String major,
                          Double gpa, LocalDate dob, Status status,
                          String applicationId,
                          Documents documents,
                          Integer noteCount,
                          Optional<Note> latestNote,
                          String displayStatus) {
  }

}
