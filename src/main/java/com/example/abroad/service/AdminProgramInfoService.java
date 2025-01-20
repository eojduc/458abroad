package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
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

  public DeleteProgramOutput deleteProgram(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new DeleteProgramOutput.UserNotFound();
    }
    if (adminRepository.findByUsername(user.username()).isEmpty()) {
      return new DeleteProgramOutput.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new DeleteProgramOutput.ProgramNotFound();
    }
    programRepository.deleteById(programId);
    return new DeleteProgramOutput.Success();
  }

  public GetProgramInfoOutput getProgramInfo(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetProgramInfoOutput.UserNotFound();
    }
    if (adminRepository.findByUsername(user.username()).isEmpty()) {
      return new GetProgramInfoOutput.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfoOutput.ProgramNotFound(user);
    }
    var studentsByUsername = studentRepository.findAll().stream()
      .collect(Collectors.toMap(Student::username, Function.identity()));

    var applicants = applicationRepository.findByProgramId(programId).stream()
      .flatMap(application ->
        Stream.ofNullable(studentsByUsername.get(application.student()))
          .map(student -> applicant(student, application)))
      .toList();
    return new GetProgramInfoOutput.Success(program, applicants, user);
  }

  public sealed interface DeleteProgramOutput permits
    DeleteProgramOutput.ProgramNotFound,
    DeleteProgramOutput.UserNotFound,
    DeleteProgramOutput.UserNotAdmin,
    DeleteProgramOutput.Success {

    record Success() implements DeleteProgramOutput {

    }

    record ProgramNotFound() implements DeleteProgramOutput {

    }

    record UserNotFound() implements DeleteProgramOutput {

    }

    record UserNotAdmin() implements DeleteProgramOutput {

    }
  }

  public sealed interface GetProgramInfoOutput permits
    GetProgramInfoOutput.Success, GetProgramInfoOutput.ProgramNotFound,
    GetProgramInfoOutput.UserNotFound, GetProgramInfoOutput.UserNotAdmin {

    record Success(Program program, List<Applicant> applicants, User user) implements
      GetProgramInfoOutput {

    }

    record ProgramNotFound(User user) implements GetProgramInfoOutput {

    }

    record UserNotFound() implements GetProgramInfoOutput {

    }

    record UserNotAdmin() implements GetProgramInfoOutput {

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

  public record Applicant(String username, String displayName, String email, String major,
                          Double gpa, LocalDate dob, Application.Status status) {
  }

}
