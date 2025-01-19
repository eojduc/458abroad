package com.example.abroad.controller;


import com.example.abroad.model.Program;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.ProgramInfoService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProgramInfoController {


  private final ProgramInfoService service;


  public ProgramInfoController(ProgramInfoService service) {
    this.service = service;
  }



  @GetMapping("/program-info/{programId}")
  public String getProgramInfo(
    @PathVariable("programId") String programId,
    Model model
  ) {
    var data = service.getProgramInfo(programId).orElse(null);
    if (data == null) {
      return "program-not-found";
    }
    model.addAttribute("data", data);
    return "program-info";
  }

}
