package com.example.abroad.controller.admin;

import com.example.abroad.model.Program;
import com.example.abroad.service.AdminProgramInfoService;
import com.example.abroad.service.AdminProgramsService;
import com.example.abroad.service.AdminProgramsService.GetAllProgramsInfo;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Random;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminProgramsController(AdminProgramsService service) {

  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsController.class);
  private static final Random random = new Random();

  @GetMapping("/admin/programs")
  public String getProgramsInfo(HttpSession session, Model model, @RequestParam(required = false) String sort
  ) {
    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, Optional.ofNullable(sort));
    int randomActive = random.nextInt(101);
    int randomStatus = random.nextInt(101);

    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
            Map.of(
                "name", user.displayName(),
                "programs", programs,
                "programActive", randomActive,
                "programStatus", randomStatus
            )
        );
        yield "admin/programs :: " + (sort != null ? "programTable" : "page");
      }
    };
  }

}
