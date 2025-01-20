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

  public GetProgramInfoOutput getProgramInfo(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetProgramInfoOutput.UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfoOutput.ProgramNotFound(user);
    }
    var students = applicationRepository.countByProgramId(programId);
    var applicationStatus = applicationRepository.findByProgramIdAndStudent(programId, user.username())
      .map(application -> application.status().name())
      .orElse("NOT_APPLIED");
    return new GetProgramInfoOutput.Success(program, students, applicationStatus, user);
  }

  public sealed interface GetProgramInfoOutput permits
    GetProgramInfoOutput.Success, GetProgramInfoOutput.ProgramNotFound, GetProgramInfoOutput.UserNotFound {
    record Success(Program program, Integer studentsEnrolled, String applicationStatus, User user)
      implements GetProgramInfoOutput {
    }
    record ProgramNotFound(User user) implements GetProgramInfoOutput {
    }
    record UserNotFound() implements GetProgramInfoOutput {
    }
  }

}
