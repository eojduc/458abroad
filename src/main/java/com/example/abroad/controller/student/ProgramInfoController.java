package com.example.abroad.controller.student;


import com.example.abroad.model.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramInfoService;
import com.example.abroad.service.ProgramInfoService.GetProgramInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public record ProgramInfoController(ProgramInfoService service, FormatService formatter) {

  @GetMapping("/programs/{programId}")
  public String getProgramInfo(
    @PathVariable("programId") Integer programId,
    Model model,
    HttpServletRequest request,
    Optional<String> error,
    Optional<String> success,
    Optional<String> warning,
    Optional<String> info
  ) {
    switch (service.getProgramInfo(programId, request)) {
      case GetProgramInfo.Success(var program, var studentsEnrolled, var applicationStatus, var user) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "studentsEnrolled", studentsEnrolled,
          "applicationStatus", applicationStatus,
          "user", user,
          "formatter", formatter,
          "alerts", new Alerts(error, success, warning, info)
        ));
        return "program-info :: page";
      }
      case GetProgramInfo.ProgramNotFound() -> {
        model.addAllAttributes(Map.of(
          "title", "Program not found",
          "message", "The program you are looking for does not exist."
        ));
        return "error :: custom-page";
      }
      case GetProgramInfo.UserNotFound() -> {
        return "redirect:/login";
      }
    }
  }

}
