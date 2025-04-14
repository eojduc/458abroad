package com.example.abroad.controller;

import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.service.ThemeService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;
    private final ThemeService themeService;

    @Autowired
    public GlobalControllerAdvice(UserService userService, ThemeService themeService) {
        this.userService = userService;
        this.themeService = themeService;
    }

    @ModelAttribute
    public void addUserAttributes(Model model, HttpSession session) {
        userService.findUserFromSession(session).ifPresent(user -> {
            model.addAttribute("user", user);
            model.addAttribute("isAdmin", userService.isAdmin(user));
        });
    }
    @ModelAttribute("themeConfig")
    public RebrandConfig addThemeConfig() {
        return themeService.getConfig();
    }


}