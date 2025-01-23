package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.Question;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record ApplyToProgramService(
  ApplicationRepository applicationRepository,
  ProgramRepository programRepository,
  UserService userService
) {


  public GetApplyPageData getPageData(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetApplyPageData.UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetApplyPageData.ProgramNotFound();
    }
    var existingApplication = applicationRepository.findByProgramIdAndStudent(programId, user.username());
    if (existingApplication.isPresent()) {
      return new GetApplyPageData.StudentAlreadyApplied(existingApplication.get().id());
    }
    return new GetApplyPageData.Success(program, user, Question.QUESTIONS);
  }

  public ApplyToProgram applyToProgram(
    Integer programId,
    HttpServletRequest request, String major, Double gpa, LocalDate dob,
    String answer1, String answer2, String answer3, String answer4, String answer5
  ) {
    var user = userService.getUser(request).orElse(null);

    if (user == null) {
      return new ApplyToProgram.UserNotFound();
    }
    var application = new Application(
      null, user.username(), programId, dob, gpa, major, answer1,
      answer2, answer3, answer4, answer5, Status.APPLIED
    );
    applicationRepository.save(application);

    return new ApplyToProgram.Success();
  }

  public sealed interface GetApplyPageData permits
    GetApplyPageData.Success, GetApplyPageData.UserNotFound, GetApplyPageData.ProgramNotFound,
    GetApplyPageData.StudentAlreadyApplied {
    record Success(Program program, User user, List<Question> questions) implements
      GetApplyPageData {
    }
    record UserNotFound() implements GetApplyPageData {
    }
    record ProgramNotFound() implements GetApplyPageData {
    }
    record StudentAlreadyApplied(String applicationId) implements GetApplyPageData {
    }

  }

  public sealed interface ApplyToProgram permits ApplyToProgram.Success,
    ApplyToProgram.UserNotFound {
    record Success() implements ApplyToProgram {
    }

    record UserNotFound() implements ApplyToProgram {
    }

  }


}
