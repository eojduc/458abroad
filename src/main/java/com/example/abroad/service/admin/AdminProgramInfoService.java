package com.example.abroad.service.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramInfoService(
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  UserService userService,
  StudentRepository studentRepository,
  AdminRepository adminRepository
) {

  public DeleteProgram deleteProgram(Integer programId, HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new DeleteProgram.UserNotFound();
    }
    if (adminRepository.findByUsername(user.username()).isEmpty()) {
      return new DeleteProgram.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new DeleteProgram.ProgramNotFound();
    }
    programRepository.deleteById(programId);
    return new DeleteProgram.Success();
  }

  public GetProgramInfo getProgramInfo(Integer programId, HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetProgramInfo.UserNotFound();
    }
    if (!user.isAdmin()) {
      return new GetProgramInfo.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfo.ProgramNotFound();
    }
    var students = studentRepository.findAll();

    var applicants = applicationRepository.findByProgramId(programId).stream()
      .flatMap(application -> applicants(students.stream(), application))
      .toList();
    return new GetProgramInfo.Success(program, applicants, user);
  }

  public SortApplicantTable sortApplicantTable(Optional<Column> column,
    Optional<Filter> filter,
    Integer programId, HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new SortApplicantTable.Failure();
    }
    if (adminRepository.findByUsername(user.username()).isEmpty()) {
      return new SortApplicantTable.Failure();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new SortApplicantTable.Failure();
    }
    var students = studentRepository.findAll();

    var sorter = switch (column.orElse(Column.NONE)) {
      case USERNAME, NONE -> Comparator.comparing(Applicant::username);
      case DISPLAY_NAME -> Comparator.comparing(Applicant::displayName);
      case EMAIL -> Comparator.comparing(Applicant::email);
      case MAJOR -> Comparator.comparing(Applicant::major);
      case GPA -> Comparator.comparing(Applicant::gpa);
      case DOB -> Comparator.comparing(Applicant::dob);
      case STATUS -> Comparator.comparing(Applicant::status);
    };
    Predicate<Applicant> filterer = switch (filter.orElse(Filter.NONE)) {
      case APPLIED -> applicant -> applicant.status() == Status.APPLIED;
      case ENROLLED -> applicant -> applicant.status() == Status.ENROLLED;
      case CANCELLED -> applicant -> applicant.status() == Status.CANCELLED;
      case WITHDRAWN -> applicant -> applicant.status() == Status.WITHDRAWN;
      case NONE -> applicant -> true;
    };
    var applicants = applicationRepository.findByProgramId(programId).stream()
      .flatMap(application -> applicants(students.stream(), application))
      .sorted(sorter)
      .filter(filterer)
      .toList();
    return new SortApplicantTable.Success(applicants, program);
  }

  private Stream<Applicant> applicants(Stream<Student> students, Application application) {
    return students.filter(student -> student.username().equals(application.student()))
      .map(student -> new Applicant(
        student.username(),
        student.displayName(),
        student.email(),
        application.major(),
        application.gpa(),
        application.dateOfBirth(),
        application.status(),
        application.id()
      ));
  }

  public enum Filter {
    APPLIED, ENROLLED, CANCELLED, WITHDRAWN, NONE
  }

  public enum Column {
    USERNAME, DISPLAY_NAME, EMAIL, MAJOR, GPA, DOB, STATUS, NONE
  }

  public sealed interface DeleteProgram permits
    DeleteProgram.ProgramNotFound,
    DeleteProgram.UserNotFound,
    DeleteProgram.UserNotAdmin,
    DeleteProgram.Success {

    record Success() implements DeleteProgram {

    }

    record ProgramNotFound() implements DeleteProgram {

    }

    record UserNotFound() implements DeleteProgram {

    }

    record UserNotAdmin() implements DeleteProgram {

    }
  }

  public sealed interface GetProgramInfo permits
    GetProgramInfo.Success, GetProgramInfo.ProgramNotFound,
    GetProgramInfo.UserNotFound, GetProgramInfo.UserNotAdmin {

    record Success(Program program, List<Applicant> applicants, User user) implements
      GetProgramInfo {

    }

    record ProgramNotFound() implements GetProgramInfo {

    }

    record UserNotFound() implements GetProgramInfo {

    }

    record UserNotAdmin() implements GetProgramInfo {

    }
  }

  public sealed interface SortApplicantTable permits SortApplicantTable.Success,
    SortApplicantTable.Failure {

    record Success(List<Applicant> applicants, Program program) implements SortApplicantTable {

    }

    record Failure() implements SortApplicantTable {

    }

  }

  public record Applicant(String username, String displayName, String email, String major,
                          Double gpa, LocalDate dob, Application.Status status,
                          String applicationId) {

  }

}
