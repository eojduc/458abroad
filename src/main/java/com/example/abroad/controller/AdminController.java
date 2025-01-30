package com.example.abroad.controller;

import com.example.abroad.model.Admin;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/createAdmin")
    public String createAdmin(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        var user = userService.getUser(session).orElse(null);
        try {
            userService.createAdmin(username, email, password, session);
            return "redirect:/admin/dashboard";
        } catch (IllegalStateException e) {
            model.addAttribute("title", "Admin Creation Error");
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
}
