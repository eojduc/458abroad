package com.example.abroad.controller.student;

import com.example.abroad.service.FormatService;
import com.example.abroad.service.page.ListApplicationsService;
import com.example.abroad.service.page.ListApplicationsService.GetApplicationsResult;
import com.example.abroad.service.UserService;

import com.example.abroad.service.page.ListApplicationsService.Sort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public record ListApplicationsController(ListApplicationsService listApplicationsService, FormatService formatter,
    UserService userService) {

  @GetMapping("/applications")
  public String listApplications(
      HttpSession session,
      Model model) {

    GetApplicationsResult result = listApplicationsService.getApplications(session, Sort.TITLE, true);

    return switch (result) {
      case GetApplicationsResult.Success(var pairs, var user) -> {
        model.addAllAttributes(Map.of(
            "pairs", pairs,
            "user", user,
            "sort", Sort.TITLE,
            "formatter", formatter,
            "theme", userService.getTheme(session)));
        yield "student/list-application :: page";
      }
      case GetApplicationsResult.UserNotFound() ->
        "redirect:/login?error=Not logged in";
    };
  }


  @GetMapping("/applications/sort")
  public String sortApplications(   HttpSession session,
    Model model,
    @RequestParam Sort sort,
    @RequestParam Boolean ascending) {

    GetApplicationsResult result = listApplicationsService.getApplications(session, sort, ascending);

    return switch (result) {
      case GetApplicationsResult.Success(var pairs, var user) -> {
        model.addAllAttributes(Map.of(
          "pairs", pairs,
          "user", user,
          "sort", sort,
          "formatter", formatter,
          "theme", userService.getTheme(session),
          "ascending", ascending
        ));

        yield "student/list-application :: programTable";
      }
      case GetApplicationsResult.UserNotFound() ->
        "redirect:/login?error=Not logged in";
    };
  }
}