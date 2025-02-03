package com.example.abroad.controller.student;

import com.example.abroad.service.FormatService;
import com.example.abroad.service.ListApplicationsService;
import com.example.abroad.service.ListApplicationsService.GetApplicationsResult;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@Controller
public record ListApplicationsController(ListApplicationsService listApplicationsService, FormatService formatter,
                                         UserService userService) {

  @GetMapping("/applications")
  public String listApplications(HttpSession session, Model model) {
    GetApplicationsResult result = listApplicationsService.getApplications(session);
    return switch(result) {
      case GetApplicationsResult.Success(var apps, var programs, var user) -> {
        model.addAllAttributes(Map.of(
          "apps", apps,
          "programs", programs,
          "user", user,
          "formatter", formatter,
          "theme", userService.getTheme(session)
        ));
        yield "student/list-application :: page";
      }
      case GetApplicationsResult.UserNotFound() ->
        "redirect:/login?error=Not logged in";
    };
  }
}