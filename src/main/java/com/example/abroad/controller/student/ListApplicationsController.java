package com.example.abroad.controller.student;

import com.example.abroad.service.FormatService;
import com.example.abroad.service.ListApplicationsService;
import com.example.abroad.service.ListApplicationsService.GetApplicationsResult;
import com.example.abroad.service.UserService;

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
      HttpServletRequest request,
      Model model,
      @RequestParam(value = "sort", required = false, defaultValue = "title") String sort) {

    GetApplicationsResult result = listApplicationsService.getApplications(session, sort);

    return switch (result) {
      case GetApplicationsResult.Success(var apps, var programs, var user) -> {
        model.addAllAttributes(Map.of(
            "apps", apps,
            "programs", programs,
            "user", user,
            "sort", sort,
            "formatter", formatter,
            "theme", userService.getTheme(session)));

        if (request.getHeader("HX-Request") != null) {
          yield "student/list-application :: programTable";
        } else {
          yield "student/list-application :: page";
        }
      }
      case GetApplicationsResult.UserNotFound() ->
        "redirect:/login?error=Not logged in";
    };
  }
}