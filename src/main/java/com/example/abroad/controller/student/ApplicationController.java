package com.example.abroad.controller.student;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {
  @GetMapping("/applications/{applicationId}")
  public String getApplication() {
    return "application";
  }
}
