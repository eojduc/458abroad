package com.example.abroad.controller.student;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.page.ListApplicationsService;
import com.example.abroad.service.page.ListApplicationsService.GetApplicationsResult;
import com.example.abroad.service.UserService;

import com.example.abroad.service.page.ListApplicationsService.Sort;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.HashMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record ListApplicationsController(
        ListApplicationsService listApplicationsService,
        FormatService formatter,
        UserService userService
) {
  @GetMapping("/applications")
  public String listApplications(HttpSession session, Model model, @RequestParam Optional<String> error,
                                 @RequestParam Optional<String> success, @RequestParam Optional<String> warning,
                                 @RequestParam Optional<String> info) {
    GetApplicationsResult result = listApplicationsService.getApplications(session, Sort.TITLE, true);
    return switch (result) {
      case GetApplicationsResult.Success(var pairs, var user) -> {
        var attributes = new HashMap<String, Object>();
        attributes.put("pairs", pairs);
        attributes.put("alerts", new Alerts(error, success, warning, info));
        attributes.put("user", user);
        attributes.put("sort", Sort.TITLE);
        attributes.put("ascending", true);  // Always include ascending
        attributes.put("formatter", formatter);

        model.addAllAttributes(attributes);
        yield "student/list-application :: page";
      }
      case GetApplicationsResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
    };
  }

  @GetMapping("/applications/sort")
  public String sortApplications(
          HttpSession session,
          Model model,
          @RequestParam Sort sort,
          @RequestParam Boolean ascending
  ) {
    GetApplicationsResult result = listApplicationsService.getApplications(session, sort, ascending);
    return switch (result) {
      case GetApplicationsResult.Success(var pairs, var user) -> {
        var attributes = new HashMap<String, Object>();
        attributes.put("pairs", pairs);
        attributes.put("user", user);
        attributes.put("sort", sort);
        attributes.put("formatter", formatter);
        attributes.put("ascending", ascending);

        model.addAllAttributes(attributes);
        yield "student/list-application :: programTable";
      }
      case GetApplicationsResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
    };
  }
}