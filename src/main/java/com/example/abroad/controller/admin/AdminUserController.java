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
    public String redirectToPage(
            @RequestParam Optional<String> error,
            @RequestParam Optional<String> success,
            @RequestParam Optional<String> warning,
            @RequestParam Optional<String> info
    ) {
        return String.format("redirect:/admin/users/page?%s%s%s%s",
                error.map(e -> "error=" + e + "&").orElse(""),
                success.map(s -> "success=" + s + "&").orElse(""),
                warning.map(w -> "warning=" + w + "&").orElse(""),
                info.map(i -> "info=" + i).orElse(""));
    }

    @GetMapping("/page")
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
                                entry("theme", userService.getTheme(session)),
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
                                entry("theme", userService.getTheme(session)),
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
}