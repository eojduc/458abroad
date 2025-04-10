package com.example.abroad.controller.admin;

import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ThemeService;
import com.example.abroad.service.UserService;
import com.example.abroad.view.Alerts;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public record PreviewPageController(
    UserService userService,
    FormatService formatter,
    ThemeService themeService) {

  @GetMapping("/preview")
  public String defaultPreview(
      Model model,
      HttpSession session,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {

    return previewSpecificPage("admin", model, session, error, success, warning, info);
  }

  @GetMapping("/preview/{page}")
  public String previewSpecificPage(
      @PathVariable String page,
      Model model,
      HttpSession session,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {

    model.addAttribute("formatter", formatter);
    model.addAttribute("user", userService.previewUser());
    model.addAttribute("alerts", new Alerts(error, success, warning, info));

    // Use session-stored themeConfig or default from themeService
    RebrandConfig sessionConfig = (RebrandConfig) session.getAttribute("previewConfig");
    if (sessionConfig == null) {
      sessionConfig = themeService.getConfig();  // Fall back to default if not in session
    }
    model.addAttribute("themeConfig", sessionConfig);
    session.setAttribute("previewConfig", sessionConfig);

    return switch (page) {
      case "admin" -> "admin/admin-dashboard-preview :: page";
      case "student" -> "student/student-dashboard-preview :: page";
      case "home" -> "home-page-preview :: page";
      default -> "admin/admin-dashboard-preview :: page";
    };
  }

  @PostMapping("/preview/reset")
  public ResponseEntity<Void> resetPreview(
      HttpSession session,
      Model model) {
    RebrandConfig config = themeService.getConfig();
    model.addAttribute("themeConfig", config);
    session.setAttribute("previewConfig", config);
    return ResponseEntity
        .status(HttpStatus.OK)
        .header("HX-Redirect", "/preview")
        .build();
  }

  @PostMapping("/preview/update/{field}")
  public ResponseEntity<Void> updatePreviewField(
      HttpSession session,
      @RequestParam String headName,
      @RequestParam(required = false) MultipartFile logoSvg,
      @RequestParam String homeTitle,
      @RequestParam String homeSubtitle,
      @RequestParam String homeCardContent,
      @RequestParam String adminContent,
      @RequestParam String studentContent,
      @RequestParam String footerText,
      @RequestParam String primaryColor,
      @RequestParam String base100,
      @RequestParam String base200,
      @RequestParam String base300,
      @RequestParam("currentPage") String currentPage,
      Model model) {

    RebrandConfig config = themeService.getConfig();

    config.setHeadName(headName);
    config.setHomeTitle(homeTitle);
    config.setHomeSubtitle(homeSubtitle);
    config.setHomeCardContent(homeCardContent);
    config.setAdminContent(adminContent);
    config.setStudentContent(studentContent);
    config.setFooterText(footerText);
    config.setPrimaryColor(primaryColor);
    config.setBase100(base100);
    config.setBase200(base200);
    config.setBase300(base300);

    // Handle the logoSvg file
    if (logoSvg != null && !logoSvg.isEmpty()) {
      try {
        String logoSvgContent = new String(logoSvg.getBytes());
        config.setLogoSvg(logoSvgContent);
      } catch (Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("HX-Redirect", "/preview/" + currentPage + "?error=Failed to process logo SVG")
            .build();
      }
    }

    model.addAttribute("themeConfig", config);
    session.setAttribute("previewConfig", config);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header("HX-Redirect", "/preview/" + currentPage)
        .build();
  }
}
