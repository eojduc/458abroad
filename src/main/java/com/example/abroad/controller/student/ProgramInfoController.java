package com.example.abroad.controller.student;


import com.example.abroad.controller.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramInfoService;
import com.example.abroad.service.ProgramInfoService.GetProgramInfoOutput;
import jakarta.servlet.http.HttpServletRequest;
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
      case GetProgramInfoOutput.Success(var program, var studentsEnrolled, var applicationStatus, var user) -> {
        model.addAttribute("program", program);
        model.addAttribute("studentsEnrolled", studentsEnrolled);
        model.addAttribute("applicationStatus", applicationStatus);
        model.addAttribute("user", user);
        model.addAttribute("formatter", formatter);
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        return "program-info :: page";
      }
      case GetProgramInfoOutput.ProgramNotFound(var user) -> {
        model.addAttribute("user", user);
        return "program-info :: not-found";
      }
      case GetProgramInfoOutput.UserNotFound() -> {
        return "redirect:/login";
      }
    }
  }

}
