package com.example.abroad.controller;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AccountController {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    public AccountController(AdminRepository adminRepository, StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/getProfile")
    public String getProfile(HttpSession session, Model model) {
        System.out.println("DEBUG: Hitting getProfile endpoint");
        System.out.println("DEBUG: Session ID: " + session.getId());
        User user = (User) session.getAttribute("user");
        System.out.println("DEBUG: User from session: " + (user != null ? user.username() : "null"));
        System.out.println("calling /getProfle");
        if (user == null) {
            System.out.println("user is null");
            return "redirect:/login";
        }
        System.out.println("User found: " + user.username());
        model.addAttribute("user", user);
        return "profile";
    }
    @PostMapping("/updateProfile")
    public String updateProfile(
            @RequestParam String displayName,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            Admin updatedAdmin = new Admin(
                    admin.username(),    // username stays the same as it's the primary key
                    admin.password(),
                    displayName,
                    email
            );
            adminRepository.save(updatedAdmin);
            session.setAttribute("user", updatedAdmin);
        } else {
            Student student = (Student) user;
            Student updatedStudent = new Student(
                    student.username(),  // username stays the same as it's the primary key
                    student.password(),
                    displayName,
                    email
            );
            studentRepository.save(updatedStudent);
            session.setAttribute("user", updatedStudent);
        }

        model.addAttribute("success", "Profile updated successfully");
        return "profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (!user.password().equals(currentPassword)) {
            model.addAttribute("error", "Current password is incorrect");
            return "profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "profile";
        }

        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            Admin updatedAdmin = new Admin(
                    admin.username(),    // username stays the same
                    newPassword,
                    admin.displayName(),
                    admin.email()
            );
            adminRepository.save(updatedAdmin);
            session.setAttribute("user", updatedAdmin);
        } else {
            Student student = (Student) user;
            Student updatedStudent = new Student(
                    student.username(),  // username stays the same
                    newPassword,
                    student.displayName(),
                    student.email()
            );
            studentRepository.save(updatedStudent);
            session.setAttribute("user", updatedStudent);
        }

        model.addAttribute("success", "Password updated successfully");
        return "profile";
    }
}