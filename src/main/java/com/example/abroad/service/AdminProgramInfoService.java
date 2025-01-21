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
      return new GetProgramInfo.ProgramNotFound(user);
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

    record ProgramNotFound(User user) implements GetProgramInfo {

    }

    record UserNotFound() implements GetProgramInfo {

    }

    record UserNotAdmin() implements GetProgramInfo {

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
