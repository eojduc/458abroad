package com.example.abroad.controller.admin;

import static java.util.Map.entry;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminUserService;
import com.example.abroad.service.page.admin.AdminUserService.GetAllUsersInfo;
import com.example.abroad.service.page.admin.AdminUserService.Sort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public record AdminUserController(
  AdminUserService adminUserService,
  FormatService formatter,
  UserService userService
) {
  // Add a mapping for the root URL that redirects to /page
  @GetMapping
  public String getUsers(
    HttpSession session,
    Model model,
    @RequestParam Optional<String> error,
    @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning,
    @RequestParam Optional<String> info
  ) {
    GetAllUsersInfo usersInfo = adminUserService.getUsersInfo(session, Sort.NAME, "", true);
    return switch (usersInfo) {
      case GetAllUsersInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllUsersInfo.UserNotAdmin() -> "redirect:/home?error=You are not an admin";
      case GetAllUsersInfo.Success(var users, var adminUser) -> {
        model.addAllAttributes(
          Map.ofEntries(
            entry("name", adminUser.displayName()),
            entry("sort", "NAME"),
            entry("alerts", new Alerts(error, success, warning, info)),
            entry("formatter", formatter),
            entry("searchFilter", ""),
            entry("user", adminUser),
            entry("ascending", true),
            entry("users", users)
          )
        );
        yield "admin/users :: page";
      }
    };
  }

  @GetMapping("/table")
  public String getUsersTable(
    HttpSession session,
    Model model,
    @RequestParam Sort sort,
    @RequestParam String searchFilter,
    @RequestParam Boolean ascending
  ) {
    GetAllUsersInfo usersInfo = adminUserService.getUsersInfo(session, sort, searchFilter, ascending);
    return switch (usersInfo) {
      case AdminUserService.GetAllUsersInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllUsersInfo.UserNotAdmin() -> "redirect:/home?error=You are not an admin";
      case GetAllUsersInfo.Success(var users, var adminUser) -> {
        model.addAllAttributes(
          Map.ofEntries(
            entry("name", adminUser.displayName()),
            entry("sort", sort.name()),
            entry("searchFilter", searchFilter),
            entry("formatter", formatter),
            entry("ascending", ascending),
            entry("users", users)
          )
        );
        yield "admin/users :: userTable";
      }
    };
  }

  @PostMapping("/{username}/admin-status")
  public String modifyAdminStatus(
    HttpSession session,
    @PathVariable String username,
    @RequestParam boolean grantAdmin,
    @RequestParam(required = false) boolean confirmed,
    Model model,
    HttpServletRequest request
  ) {
    var result = adminUserService.modifyUserAdminStatus(session, username, grantAdmin, confirmed);

    return switch (result) {
      case AdminUserService.ModifyUserResult.UserNotFound() ->
        "redirect:/login?error=User not found";
      case AdminUserService.ModifyUserResult.UserNotAdmin() ->
        "redirect:/home?error=You are not an admin";
      case AdminUserService.ModifyUserResult.CannotModifySuperAdmin() ->
        "redirect:/admin/users?error=Cannot modify super admin account";
      case AdminUserService.ModifyUserResult.RequiresConfirmation(var targetUser, var programs) -> {
        model.addAttribute("username", targetUser);
        model.addAttribute("programs", programs);
        model.addAttribute("formatter", formatter);
        // Return just the dialog fragment for HTMX requests
        if (isHtmxRequest(request)) {
          yield "admin/users :: confirmationDialog";
        }
        // Redirect to main page for regular form submissions
        yield "redirect:/admin/users";
      }
      case AdminUserService.ModifyUserResult.Success(var user) ->
        "redirect:/admin/users?success=User admin status updated successfully";
    };
  }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return request.getHeader("HX-Request") != null;
    }

    // Add these methods to your AdminUserController class

    @GetMapping("/{username}/show-reset-password")
    public String showPasswordResetForm(
            HttpSession session,
            @PathVariable String username,
            Model model,
            HttpServletRequest request
    ) {
        var result = adminUserService.validatePasswordReset(session, username);

        return switch (result) {
            case AdminUserService.PasswordResetValidationResult.UserNotFound() ->
                    "redirect:/login?error=User not found";
            case AdminUserService.PasswordResetValidationResult.UserNotAdmin() ->
                    "redirect:/home?error=You are not an admin";
            case AdminUserService.PasswordResetValidationResult.CannotResetSSOUser() ->
                    "redirect:/admin/users?error=Cannot reset password for SSO user";
            case AdminUserService.PasswordResetValidationResult.CannotResetSuperAdmin() ->
                    "redirect:/admin/users?error=Cannot reset password for super admin";
            case AdminUserService.PasswordResetValidationResult.Valid() -> {
                model.addAttribute("username", username);
                yield "admin/users :: passwordResetDialog";
            }
        };
    }

    @PostMapping("/{username}/reset-password")
    public String resetPassword(
            HttpSession session,
            @PathVariable String username,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword
    ) {
        var result = adminUserService.resetUserPassword(session, username, newPassword, confirmPassword);

        return switch (result) {
            case AdminUserService.PasswordResetResult.UserNotFound() ->
                    "redirect:/login?error=User not found";
            case AdminUserService.PasswordResetResult.UserNotAdmin() ->
                    "redirect:/home?error=You are not an admin";
            case AdminUserService.PasswordResetResult.CannotResetSSOUser() ->
                    "redirect:/admin/users?error=Cannot reset password for SSO user";
            case AdminUserService.PasswordResetResult.CannotResetSuperAdmin() ->
                    "redirect:/admin/users?error=Cannot reset password for super admin";
            case AdminUserService.PasswordResetResult.PasswordsDoNotMatch() ->
                    "redirect:/admin/users?error=Passwords do not match";
            case AdminUserService.PasswordResetResult.PasswordTooShort() ->
                    "redirect:/admin/users?error=Password must be at least 8 characters";
            case AdminUserService.PasswordResetResult.Success() ->
                    "redirect:/admin/users?success=Password reset successfully";
        };
    }

    @PostMapping("/{username}/delete-user")
    public String deleteUser(
            HttpSession session,
            @PathVariable String username
    ) {
        var result = adminUserService.deleteUser(session, username);

        return switch (result) {
            case AdminUserService.DeleteUserResult.UserNotFound() ->
                    "redirect:/login?error=User not found";
            case AdminUserService.DeleteUserResult.UserNotAdmin() ->
                    "redirect:/home?error=You are not an admin";
            case AdminUserService.DeleteUserResult.CannotDeleteSSOUser() ->
                    "redirect:/admin/users?error=Cannot delete SSO user";
            case AdminUserService.DeleteUserResult.CannotDeleteSuperAdmin() ->
                    "redirect:/admin/users?error=Cannot delete super admin";
            case AdminUserService.DeleteUserResult.CannotDeleteSelf() ->
                    "redirect:/admin/users?error=You cannot delete your own account";
            case AdminUserService.DeleteUserResult.Success() ->
                    "redirect:/admin/users?success=User deleted successfully";
        };
    }

}