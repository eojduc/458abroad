package com.example.abroad.controller;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.IncorrectPasswordException;
import com.example.abroad.exception.UserAlreadyExistsException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class AuthController {

    private final UserService userService; // You'll need to create this service

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpServletRequest request,
                            Model model) {
        try {
            User user = userService.authenticateUser(username, password);
            request.getSession().setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", "Username not found");
            return "auth/login";
        } catch (IncorrectPasswordException e) {
            model.addAttribute("error", "Incorrect password");
            return "auth/login";
        }
    }


    @GetMapping("/register")
    public String showRegistrationForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        try {
            userService.registerUser(username, email, password);
            return "redirect:/login?registered=true";
        } catch (UsernameAlreadyInUseException e) {
            model.addAttribute("error", "Username is already taken");
            return "auth/register";
        } catch (EmailAlreadyInUseException e) {
            model.addAttribute("error", "Email is already registered");
            return "auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login?logout=true";
    }
}