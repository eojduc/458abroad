package com.example.abroad.controller.student;


import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramInfoService;
import com.example.abroad.service.ProgramInfoService.ProgramNotFound;
import com.example.abroad.service.ProgramInfoService.ProgramInfo;
import com.example.abroad.service.ProgramInfoService.UserNotFound;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProgramInfoController {


  private final ProgramInfoService service;
  private final FormatService formatter;


  public ProgramInfoController(ProgramInfoService service, FormatService formatter) {
    this.service = service;
    this.formatter = formatter;
  }


  @GetMapping("/programs/{programId}")
  public String getProgramInfo(
    @PathVariable("programId") String programId,
    Model model,
    HttpServletRequest request
  ) {
    switch (service.getProgramInfo(programId, request)) {
      case ProgramInfo(var program, var studentsEnrolled, var applicationStatus, var user) -> {
        model.addAttribute("program", program);
        model.addAttribute("studentsEnrolled", studentsEnrolled);
        model.addAttribute("applicationStatus", applicationStatus);
        model.addAttribute("user", user);
        model.addAttribute("formatter", formatter);
        return "program-info :: page";
      }
      case ProgramNotFound(var user) -> {
        model.addAttribute("user", user);
        return "program-info :: not-found";
      }
      case UserNotFound() -> {
        return "redirect:/login";
      }
    }
  }

}
