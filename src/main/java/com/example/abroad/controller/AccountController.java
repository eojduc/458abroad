package com.example.abroad.controller;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Alerts;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@Controller
public record AccountController(
        AdminRepository adminRepository,
        StudentRepository studentRepository,
        PasswordEncoder passwordEncoder,
        FormatService formatter,
        UserService userService
) {
    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model, @RequestParam Optional<String> error,
                             @RequestParam Optional<String> success, @RequestParam Optional<String> warning,
                             @RequestParam Optional<String> info) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        model.addAttribute("formatter", formatter);
        model.addAttribute("theme", userService.getTheme(session));

        return "profile :: page";
    }
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String displayName,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        model.addAttribute("formatter", formatter);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            Admin updatedAdmin = new Admin(
                    admin.username(),    // username stays the same as it's the primary key
                    admin.password(),
                    email,
                    displayName
            );

            //String username, String password, String email, String displayName
            adminRepository.save(updatedAdmin);
            model.addAttribute("user", updatedAdmin);
            session.setAttribute("user", updatedAdmin);
        } else {
            Student student = (Student) user;
            Student updatedStudent = new Student(
                    student.username(),  // username stays the same as it's the primary key
                    student.password(),
                    email,
                    displayName
            );
            studentRepository.save(updatedStudent);
            model.addAttribute("user", updatedStudent);
            session.setAttribute("user", updatedStudent);
        }
        return "redirect:/profile?success=Profile updated successfully";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Verify current password using BCrypt
        if (!passwordEncoder.matches(currentPassword, user.password())) {
            return "redirect:/profile?error=Current password is incorrect";
        }

        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/profile?error=New passwords do not match";
        }

        // Hash the new password
        String hashedPassword = passwordEncoder.encode(newPassword);

        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            Admin updatedAdmin = new Admin(
                    admin.username(),
                    hashedPassword,
                    admin.email(),
                    admin.displayName()
            );
            adminRepository.save(updatedAdmin);
            session.setAttribute("user", updatedAdmin);
        } else {
            Student student = (Student) user;
            Student updatedStudent = new Student(
                    student.username(),
                    hashedPassword,
                    student.email(),
                    student.displayName()
            );
            studentRepository.save(updatedStudent);
            session.setAttribute("user", updatedStudent);
        }
        return "redirect:/profile?success=Password updated successfully";
    }
}