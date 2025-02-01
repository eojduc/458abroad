package com.example.abroad.controller;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.IncorrectPasswordException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        System.out.println("get mapping /login geting called");
        return "auth/login";
    }



    @GetMapping("/register")
    public String showRegistrationForm() {
        System.out.println("get mapping /register geting called");
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        System.out.println("post mapping /register geting called");
        try {
            userService.registerStudent(username, email, password);  // Note: changed to registerStudent
            return "redirect:/login?registered=true";
        } catch (UsernameAlreadyInUseException e) {
            model.addAttribute("error", "Username is already taken");
            return "auth/register";
        } catch (EmailAlreadyInUseException e) {
            model.addAttribute("error", "Email is already registered");
            return "auth/register";
        }
    }

}