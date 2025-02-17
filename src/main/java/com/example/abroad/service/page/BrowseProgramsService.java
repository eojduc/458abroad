package com.example.abroad.service.page;

import com.example.abroad.model.Program;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import java.util.List;

public record BrowseProgramsService(UserService userService, ProgramService programService) {

  public interface BrowsePrograms {
    record NotLoggedIn() implements BrowsePrograms {}
    record Success(List<ProgramAndStatus> programs) implements BrowsePrograms {}
    record ProgramAndStatus(Program program, ProgramStatus status) {}
    enum ProgramStatus {
      APPLIED,
      ENROLLED,
      CANCELLED,
      WITHDRAWN,
      ELIGIBLE,
      APPROVED,
      COMPLETED,
      UPCOMING,
      CLOSED,
      OPEN,
    }
  }

  enum Sort {
    ASCENDING,
    DESCENDING
  }
  enum Column {
    TITLE,
    APP_OPENS,
    APP_CLOSES,
    DOC_DEADLINE,
    START_DATE,
    END_DATE,
    STATUS
  }

}
