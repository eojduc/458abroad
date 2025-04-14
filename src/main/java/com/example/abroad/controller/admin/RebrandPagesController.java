package com.example.abroad.controller.admin;

import com.example.abroad.model.RebrandConfig;
import com.example.abroad.service.page.admin.RebrandPagesService.EditRebrandPageInfo;
import com.example.abroad.service.page.admin.RebrandPagesService.GetRebrandPageInfo;
import com.example.abroad.service.page.student.ApplyToProgramService.ApplyToProgram;
import com.example.abroad.service.page.student.ApplyToProgramService.GetApplyPageData;
import com.example.abroad.view.Alerts;
import com.example.abroad.service.page.admin.RebrandPagesService;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AddProgramService;
import com.example.abroad.service.page.admin.AddProgramService.AddProgramInfo;
import com.example.abroad.service.page.admin.AddProgramService.GetAddProgramInfo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

@Controller
public record RebrandPagesController(RebrandPagesService service, FormatService formatter,
                                     UserService userService) {


  @GetMapping("/admin/brand")
  public String addProgramPage(HttpSession session,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info,
      Model model) {

    switch (service.getRebrandPageInfo(session)) {
      case GetRebrandPageInfo.Success(var user, var pageNames, var formFields) -> {

        model.addAllAttributes(
            Map.of("user", user,
                "alerts", new Alerts(error, success, warning, info),
                "pages", pageNames,
                "formFields", formFields,
                "formatter", formatter));

        return "admin/brand-edit :: page";
      }
      case GetRebrandPageInfo.NotLoggedIn() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetRebrandPageInfo.UserNotAdmin() -> {
        return "redirect:/?error=You are not the head admin";
      }
    }
  }

  @GetMapping("/admin/brand/preview-redirect")
  public ResponseEntity<Void> previewRedirect(@RequestParam String page) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .header("HX-Redirect", "/preview/" + page)
        .build();
  }

  @PostMapping("/admin/brand/edit")
  public String editRebrandPage(
      @RequestParam Map<String, String> formData,
      @RequestParam(required = false) MultipartFile logoSvg,
      HttpSession session,
      HttpServletResponse response) {

    return switch (service.editBrandInfo(session, formData, logoSvg)) {
      case EditRebrandPageInfo.Success(String message) -> "redirect:/?success=" + message;
      case EditRebrandPageInfo.NotLoggedIn() -> "redirect:/login?error=You are not logged in";
      case EditRebrandPageInfo.UserNotAdmin() -> "redirect:/?error=You are not the head admin";
      case EditRebrandPageInfo.InvalidField(String message) -> "redirect:/admin/brand?error=" + message;
      case EditRebrandPageInfo.DatabaseError(String message) -> "redirect:/admin/brand?error=" + message;
    };

  }

}