package com.example.abroad.controller.admin;

import com.example.abroad.service.admin.AdminProgramsService;
import com.example.abroad.service.admin.AdminProgramsService.GetAllProgramsInfo;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminProgramsController(AdminProgramsService service) {

  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsController.class);
  private static final Random random = new Random();

  @GetMapping("/admin/programs")
  public String getProgramsInfo(HttpSession session, Model model,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String nameFilter,
      @RequestParam(required = false) String timeFilter
      ) {
    logger.info("Optional Sort/Filter Parameters: sort={}, nameFilter={}, timeFilter={}", sort, nameFilter, timeFilter);
    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, timeFilter);
    int randomActive = random.nextInt(101);
    int randomStatus = random.nextInt(101);
    boolean noSortOrFilter = sort == null && nameFilter == null && timeFilter == null;

    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
            Map.of(
                "name", user.displayName(),
                "programs", programs,
                "programActive", randomActive,
                "programStatus", randomStatus,
                "sort", Objects.toString(session.getAttribute("lastSort"), "")
            )
        );
        yield "admin/programs :: " + ( noSortOrFilter ? "page" : "programTable");
      }
    };
  }

}
