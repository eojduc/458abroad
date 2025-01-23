package com.example.abroad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;


@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String studentDashboard(HttpServletRequest request, Model model) {
        var username = Optional.ofNullable(request.getSession().getAttribute("username"))
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .orElse("student"); // Default fallback

        model.addAttribute("username", username);
        return "dashboard/student-dashboard :: page";  // Note the :: page suffix
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpServletRequest request, Model model) {
        var username = Optional.ofNullable(request.getSession().getAttribute("username"))
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .orElse("admin"); // Default fallback

        model.addAttribute("username", username);
        return "dashboard/admin-dashboard :: page";  // Note the :: page suffix
    }
}