package com.example.abroad.controller;

import com.example.abroad.model.Admin;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final HttpServletRequest request;

    public AdminController(UserService userService, HttpServletRequest request) {
        this.userService = userService;
        this.request = request;
    }

    @PostMapping("/createAdmin")
    public String createAdmin(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              HttpServletRequest request,
                              Model model) {
        var user = userService.getUser(request).orElse(null);
        try {
            userService.createAdmin(username, email, password, request);
            return "redirect:/admin/dashboard";
        } catch (IllegalStateException e) {
            model.addAttribute("title", "Admin Creation Error");
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
}
