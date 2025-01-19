package com.example.abroad.controller.student;


import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramInfoService;
import com.example.abroad.service.ProgramInfoService.ProgramNotFound;
import com.example.abroad.service.ProgramInfoService.ProgramInfo;
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
    Model model
  ) {
    switch (service.getProgramInfo(programId)) {
      case ProgramInfo(var program, var studentsEnrolled) -> {
        model.addAttribute("program", program);
        model.addAttribute("studentsEnrolled", studentsEnrolled);
        model.addAttribute("formatter", formatter);
        return "program-info";
      }
      case ProgramNotFound n -> {
        return "program-not-found";
      }
    }
  }

}
