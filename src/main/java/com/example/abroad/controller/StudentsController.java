package com.example.abroad.controller;

import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public record StudentsController(StudentRepository studentRepository, UserService userService, AdminRepository adminRepository) {

  @GetMapping("/students")
  public String getStudents(Model model, HttpServletRequest request) {
    var students = studentRepository.findAll();
    var admins = adminRepository.findAll();
    // here until auth is set up, we'll just set the first student as the user
//    students.stream().findFirst().ifPresent(student -> userService.setUser(request, student));
  admins.stream().findFirst().ifPresent(admin -> userService.setUser(request, admin));
    model.addAttribute("students", students);
    return "students :: page";
  }

}
