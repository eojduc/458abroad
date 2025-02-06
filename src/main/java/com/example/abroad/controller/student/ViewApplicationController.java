package com.example.abroad.controller.student;

import com.example.abroad.model.Application;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.ViewApplicationService;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public record ViewApplicationController(
    ViewApplicationService applicationService,
    FormatService formatter,
    UserService userService) {

  @GetMapping("/applications/{applicationId}")
  public String viewApplication(@PathVariable String applicationId, HttpSession session, Model model) {

    var result = applicationService.getApplication(applicationId, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        model.addAllAttributes(Map.of(
            "app", success.application(),
            "prog", success.program(),
            "user", success.user(),
            "editable", success.editable(),
            "formatter", formatter,
            "theme", userService.getTheme(session)));
        yield "student/view-application :: page";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
        "redirect:/?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() -> "redirect:/?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
        "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
        "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
        "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{id}/update")
  public String updateApplication(
      @PathVariable("id") String id,
      @RequestParam("answer1") String answer1,
      @RequestParam("answer2") String answer2,
      @RequestParam("answer3") String answer3,
      @RequestParam("answer4") String answer4,
      @RequestParam("answer5") String answer5,
      HttpSession session,
      Model model) {

    var result = applicationService.updateResponses(id, answer1, answer2, answer3, answer4, answer5, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        model.addAllAttributes(Map.of(
            "app", success.application(),
            "prog", success.program(),
            "user", success.user(),
            "editable", success.editable(),
            "formatter", formatter,
            "theme", userService.getTheme(session)));
        yield "student/view-application :: applicationContent";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
        "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() -> "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
        "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
        "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
        "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{id}/withdraw")
  public String withdrawApplication(@PathVariable("id") String id,
      HttpSession session,
      Model model) {

    var result = applicationService.changeStatus(id, Application.Status.WITHDRAWN, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        model.addAllAttributes(Map.of(
            "app", success.application(),
            "prog", success.program(),
            "user", success.user(),
            "editable", success.editable(),
            "formatter", formatter,
            "theme", userService.getTheme(session)));
        yield "student/view-application :: applicationContent";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
        "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() -> "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
        "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
        "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
        "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{id}/reactivate")
  public String reactivateApplication(@PathVariable("id") String id,
      HttpSession session,
      Model model) {

    var result = applicationService.changeStatus(id, Application.Status.APPLIED, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        model.addAllAttributes(Map.of(
            "app", success.application(),
            "prog", success.program(),
            "user", success.user(),
            "editable", success.editable(),
            "formatter", formatter,
            "theme", userService.getTheme(session)));
        yield "student/view-application :: applicationContent";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
        "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() -> "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
        "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
        "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
        "redirect:/dashboard?error=Illegal Status Change";
    };
  }
}
