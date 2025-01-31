package com.example.abroad.controller.admin;

import com.example.abroad.service.AdminProgramInfoService;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public record AdminProgramsController(AdminProgramInfoService service, FormatService formatter) {

  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsController.class);

  @GetMapping("/admin/programs")
  public String getApplicationInfo(HttpSession session,
      Model model) {
    var user = service.userService().getUser(session).orElse(null);
    var programs = service.programRepository().findAll();
    logger.info("Getting programs for user: {}", user.displayName());
    logger.info("{}", programs);
    model.addAttribute("name", user.displayName());
    model.addAttribute("programs", programs);

    return "admin/programs :: page";
  }

}
