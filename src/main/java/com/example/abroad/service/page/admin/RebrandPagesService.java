package com.example.abroad.service.page.admin;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ProgramService.SaveProgram;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AddProgramService.GetAddProgramInfo;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public record RebrandPagesService(UserService userService, ProgramService programService,
                                AuditService auditService) {

  public GetRebrandPageInfo getAddProgramInfo(HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetRebrandPageInfo.NotLoggedIn();
    }
    if (!userService.isHeadAdmin(user)) {
      return new GetRebrandPageInfo.UserNotAdmin();
    }
    return new GetRebrandPageInfo.Success(user);
  }

  public sealed interface GetRebrandPageInfo {

    record Success(User admin) implements GetRebrandPageInfo {

    }

    record UserNotAdmin() implements GetRebrandPageInfo {

    }

    record NotLoggedIn() implements GetRebrandPageInfo {

    }
  }

  public sealed interface EditRebrandPageInfo {

    record Success(Integer programId) implements EditRebrandPageInfo {

    }

    record UserNotAdmin() implements EditRebrandPageInfo {

    }

    record NotLoggedIn() implements EditRebrandPageInfo {

    }

    record InvalidProgramInfo(String message) implements EditRebrandPageInfo {

    }
  }

}
