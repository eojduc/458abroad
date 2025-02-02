package com.example.abroad.controller;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Alerts;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
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
@RequestMapping("/profile")
public class AccountController {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(AdminRepository adminRepository, StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }
    Alerts alerts = new Alerts(
            Optional.empty(),  // error
            Optional.empty(),  // success
            Optional.empty(),  // warning
            Optional.empty()   // info
    );


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

        // Create an Alerts object with no messages

        model.addAttribute("alerts", alerts);

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
        Alerts alerts = new Alerts(
                Optional.empty(),
                Optional.of("Profile updated successfully"),
                Optional.empty(),
                Optional.empty()
        );
        model.addAttribute("alerts", alerts);

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

        // Verify current password using BCrypt
        if (!passwordEncoder.matches(currentPassword, user.password())) {
            Alerts alerts = new Alerts(
                    Optional.of("Current password is incorrect"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            model.addAttribute("alerts", alerts);
            model.addAttribute("user", user);
            return "profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            Alerts alerts = new Alerts(
                    Optional.of("New passwords do not match"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            model.addAttribute("alerts", alerts);
            model.addAttribute("user", user);
            return "profile";
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
            model.addAttribute("user", updatedAdmin);
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
            model.addAttribute("user", updatedStudent);
            session.setAttribute("user", updatedStudent);
        }

        Alerts alerts = new Alerts(
                Optional.empty(),
                Optional.of("Password updated successfully"),
                Optional.empty(),
                Optional.empty()
        );
        model.addAttribute("alerts", alerts);
        return "profile";
    }
}