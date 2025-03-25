package com.example.abroad.controller;

import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;

    @Autowired
    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addUserAttributes(Model model, HttpSession session) {
        userService.findUserFromSession(session).ifPresent(user -> {
            model.addAttribute("user", user);
            model.addAttribute("isAdmin", userService.isAdmin(user));
        });
    }
}