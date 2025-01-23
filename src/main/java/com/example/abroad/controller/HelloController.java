package com.example.abroad.controller;

import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This file is a sample controller. really logic should be in service layer. there's a sample both
 * with pure ssr and with htmx
 */
@Controller
public record HelloController(StudentRepository studentRepository, UserService userService, AdminRepository adminRepository) {


  //sessions are storage for each user, stored server side.
  @GetMapping("/")
  public String helloWorld(HttpServletRequest request, Model model) {
    var students = studentRepository.findAll();
    var admins = adminRepository.findAll();
    // here until auth is set up, we'll just set the first student as the user
  students.stream().findFirst().ifPresent(student -> userService.setUser(request, student));
//    admins.stream().findFirst().ifPresent(admin -> userService.setUser(request, admin));
    var name = Optional.ofNullable(request.getSession().getAttribute("name"))
      .filter(obj -> obj instanceof String)
      .map(obj -> (String) obj)
      .orElse("world");
    model.addAttribute("name", name);
    return "hello :: page";
  }


  @PostMapping("/")
  public String helloWorldPost(@RequestParam String name, Model model, HttpServletRequest request) {
    request.getSession().setAttribute("name", name);
    model.addAttribute("name", name);
    return "redirect:/";
  }

  // these are the ones that use htmx
  @GetMapping("/hello")
  public String helloWorld2(HttpServletRequest request, Model model) {
    var name = Optional.ofNullable(request.getSession().getAttribute("name"))
      .filter(obj -> obj instanceof String)
      .map(obj -> (String) obj)
      .orElse("world");
    model.addAttribute("name", name);
    return "hello :: htmx-page";
  }


  @PostMapping("/hello")
  public String helloWorldPost2(HttpServletRequest request, @RequestParam String name,
    Model model) {
    request.getSession().setAttribute("name", name);
    model.addAttribute("name", name);
    return "hello :: htmx-hello-card";
  }

}
