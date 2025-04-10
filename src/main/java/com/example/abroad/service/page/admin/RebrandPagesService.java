package com.example.abroad.service.page.admin;

import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ThemeService;
import com.example.abroad.service.ThemeService.SaveThemeConfig;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public record RebrandPagesService(UserService userService, ThemeService themeServce,
                                AuditService auditService) {


  private static final Map<String, String> PAGE_DISPLAY_NAMES = new LinkedHashMap<>() {{
    put("admin", "Admin Dashboard");
    put("student", "Student Dashboard");
    put("home", "Home Page");
  }};

  private static final List<Map<String, Object>> FORM_FIELDS = List.of(
      // Common fields for all pages
      Map.of(
          "name", "headName",
          "label", "Header Name",
          "type", "text",
          "placeholder", "e.g., Customer University",
          "id", "headNameInput",
          "pages", List.of("all")
      ),
      Map.of(
          "name", "logoSvg",
          "label", "Logo (SVG)",
          "type", "file",
          "accept", "image/svg+xml",
          "id", "svgFileInput",
          "pages", List.of("all")
      ),
      // Home page specific fields
      Map.of(
          "name", "homeTitle",
          "label", "Home Page Title",
          "type", "text",
          "placeholder", "e.g., Global Study Program",
          "id", "homeTitleInput",
          "pages", List.of("home")
      ),
      Map.of(
          "name", "homeSubtitle",
          "label", "Home Page Subtitle",
          "type", "text",
          "placeholder", "e.g., Expand your horizons",
          "id", "homeSubtitleInput",
          "pages", List.of("home")
      ),
      Map.of(
          "name", "homeCardContent",
          "label", "Home Card Content (Markdown)",
          "type", "textarea",
          "placeholder", "Enter markdown content...",
          "id", "homeCardContentInput",
          "height", "12",
          "pages", List.of("home")
      ),
      // Admin page specific fields
      Map.of(
          "name", "adminContent",
          "label", "Admin Content",
          "type", "textarea",
          "placeholder", "Enter admin content...",
          "id", "adminContentInput",
          "height", "6",
          "pages", List.of("admin")
      ),
      // Student page specific fields
      Map.of(
          "name", "studentContent",
          "label", "Student Content",
          "type", "textarea",
          "placeholder", "Enter student content...",
          "id", "studentContentInput",
          "height", "4",
          "pages", List.of("student")
      ),
      // Common footer field
      Map.of(
          "name", "footerText",
          "label", "Footer Text",
          "type", "text",
          "placeholder", "e.g., © 2025 Customer University",
          "id", "footerTextInput",
          "pages", List.of("all")
      ),
      // Primary color field
      Map.of(
          "name", "primaryColor",
          "label", "Primary Color",
          "type", "color",
          "id", "primaryColorInput",
          "pages", List.of("all")
      ),
      Map.of(
          "name", "base100",
          "label", "Base Color",
          "type", "color",
          "id", "baseColorInput",
          "pages", List.of("all")
      ),
      Map.of(
          "name", "base200",
          "label", "Base Color 2",
          "type", "color",
          "id", "baseColor2Input",
          "pages", List.of("all")
      ),
      Map.of(
          "name", "base300",
          "label", "Base Color 3",
          "type", "color",
          "id", "baseColor3Input",
          "pages", List.of("all")
      )
  );

  public GetRebrandPageInfo getRebrandPageInfo(HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetRebrandPageInfo.NotLoggedIn();
    }
    if (!userService.isHeadAdmin(user)) {
      return new GetRebrandPageInfo.UserNotAdmin();
    }
    return new GetRebrandPageInfo.Success(user, PAGE_DISPLAY_NAMES, FORM_FIELDS);
  }

  public EditRebrandPageInfo editBrandInfo(HttpSession session, Map<String, String> formData, MultipartFile logoSvg) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new EditRebrandPageInfo.NotLoggedIn();
    }
    if (!userService.isHeadAdmin(user)) {
      return new EditRebrandPageInfo.UserNotAdmin();
    }
    if (false) { // TODO: Replace with actual validation logic
      return new EditRebrandPageInfo.InvalidField("Footer must be less than 100 characters.");
    }

    return switch (themeServce.saveThemeConfig(formData, logoSvg)) {
      case SaveThemeConfig.DatabaseError(var message) -> new EditRebrandPageInfo.DatabaseError(message);
      case SaveThemeConfig.InvalidThemeInfo(var message) -> new EditRebrandPageInfo.InvalidField(message);
      case SaveThemeConfig.Success(var themeConfig) -> {
        auditService.logEvent("%s(%s): Brand information updated".formatted(user.username(), user.displayName()));
        yield new EditRebrandPageInfo.Success("Brand information updated successfully.");
      }
    };
  }

  public sealed interface GetRebrandPageInfo {

    record Success(User admin, Map<String, String> pageNames, List<Map<String, Object>> formFields) implements GetRebrandPageInfo {

    }

    record UserNotAdmin() implements GetRebrandPageInfo {

    }

    record NotLoggedIn() implements GetRebrandPageInfo {

    }
  }

  public sealed interface EditRebrandPageInfo {

    record Success(String message) implements EditRebrandPageInfo {

    }

    record NotLoggedIn() implements EditRebrandPageInfo {

    }

    record UserNotAdmin() implements EditRebrandPageInfo {

    }

    record InvalidField(String message) implements EditRebrandPageInfo {

    }

    record DatabaseError(String message) implements EditRebrandPageInfo {

    }
  }

}
