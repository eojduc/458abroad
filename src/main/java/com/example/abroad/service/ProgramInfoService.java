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

  public GetProgramInfo getProgramInfo(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetProgramInfo.UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfo.ProgramNotFound();
    }
    var students = applicationRepository.countByProgramId(programId);
    var applicationStatus = applicationRepository.findByProgramIdAndStudent(programId, user.username())
      .map(application -> application.status().name())
      .orElse("NOT_APPLIED");
    return new GetProgramInfo.Success(program, students, applicationStatus, user);
  }

  public sealed interface GetProgramInfo permits
    GetProgramInfo.Success, GetProgramInfo.ProgramNotFound, GetProgramInfo.UserNotFound {
    record Success(Program program, Integer studentsEnrolled, String applicationStatus, User user)
      implements GetProgramInfo {
    }
    record ProgramNotFound() implements GetProgramInfo {
    }
    record UserNotFound() implements GetProgramInfo {
    }
  }

}
