package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public record ProgramInfoService(
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  UserService userService
) {

  public GetProgramInfoResponse getProgramInfo(String programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound(user);
    }
    var students = applicationRepository.countByProgramId(programId);
    var applicationStatus = applicationRepository.findByProgramIdAndStudent(programId,
        user.getUsername())
      .map(application -> application.getStatus().name())
      .orElse("NOT_APPLIED");
    return new ProgramInfo(program, students, applicationStatus, user);
  }

  public sealed interface GetProgramInfoResponse permits ProgramInfo, ProgramNotFound,
    UserNotFound {

  }

  public record ProgramInfo(Program program, Integer studentsEnrolled, String applicationStatus,
                            User user) implements
    GetProgramInfoResponse {

  }

  public record ProgramNotFound(User user) implements GetProgramInfoResponse {

  }

  public record UserNotFound() implements GetProgramInfoResponse {

  }

}
