package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  public DeleteProgram deleteProgram(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
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

  public GetProgramInfo getProgramInfo(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetProgramInfo.UserNotFound();
    }
    if (adminRepository.findByUsername(user.username()).isEmpty()) {
      return new GetProgramInfo.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfo.ProgramNotFound();
    }
    var studentsByUsername = studentRepository.findAll().stream()
      .collect(Collectors.toMap(Student::username, Function.identity()));

    var applicants = applicationRepository.findByProgramId(programId).stream()
      .flatMap(application ->
        Stream.ofNullable(studentsByUsername.get(application.student()))
          .map(student -> applicant(student, application)))
      .toList();
    return new GetProgramInfo.Success(program, applicants, user);
  }

  public enum Filter {
    APPLIED, ENROLLED, CANCELLED, WITHDRAWN, NONE
  }
  public SortApplicantTable sortApplicantTable(Optional<Column> column,
    Optional<Filter> filter,
    Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
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
    var studentsByUsername = studentRepository.findAll().stream()
      .collect(Collectors.toMap(Student::username, Function.identity()));

    var applicants = applicationRepository.findByProgramId(programId).stream()
      .flatMap(application ->
        Stream.ofNullable(studentsByUsername.get(application.student()))
          .map(student -> applicant(student, application)))
      .toList();
    var sortedApplicants = switch (column.orElse(null)) {
      case USERNAME -> applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList();
      case DISPLAY_NAME -> applicants.stream().sorted(Comparator.comparing(Applicant::displayName)).toList();
      case EMAIL -> applicants.stream().sorted(Comparator.comparing(Applicant::email)).toList();
      case MAJOR -> applicants.stream().sorted(Comparator.comparing(Applicant::major)).toList();
      case GPA -> applicants.stream().sorted(Comparator.comparing(Applicant::gpa)).toList();
      case DOB -> applicants.stream().sorted(Comparator.comparing(Applicant::dob)).toList();
      case STATUS -> applicants.stream().sorted(Comparator.comparing(Applicant::status)).toList();
      case NONE -> applicants;
      case null -> applicants;
    };

    var filteredApplicants = switch (filter.orElse(null)) {
      case APPLIED -> sortedApplicants.stream().filter(applicant -> applicant.status() == Status.APPLIED).toList();
      case ENROLLED -> sortedApplicants.stream().filter(applicant -> applicant.status() == Status.ENROLLED).toList();
      case CANCELLED -> sortedApplicants.stream().filter(applicant -> applicant.status() == Status.CANCELLED).toList();
      case WITHDRAWN -> sortedApplicants.stream().filter(applicant -> applicant.status() == Status.WITHDRAWN).toList();
      case NONE -> sortedApplicants;
      case null -> sortedApplicants;
    };
    return new SortApplicantTable.Success(filteredApplicants, program);
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

  private Applicant applicant(Student student, Application application) {
    return new Applicant(
      student.username(),
      student.displayName(),
      student.email(),
      application.major(),
      application.gpa(),
      application.dateOfBirth(),
      application.status()
    );
  }

  public enum Column {
    USERNAME, DISPLAY_NAME, EMAIL, MAJOR, GPA, DOB, STATUS, NONE
  }

  public record Applicant(String username, String displayName, String email, String major,
                          Double gpa, LocalDate dob, Application.Status status) {
  }

}
