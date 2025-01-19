package com.example.abroad.controller;

import com.example.abroad.model.Student;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentsController {

  private final StudentRepository studentRepository;
  private final UserService userService;

  public StudentsController(StudentRepository studentRepository, UserService userService) {
    this.studentRepository = studentRepository;
    this.userService = userService;
  }
  @GetMapping("/students")
  public String getStudents(Model model, HttpServletRequest request) {
    var students = studentRepository.findAll();
    var student = students.stream().findFirst().orElse(null);
    if (student != null) {
      userService.setUser(request, student);
    }
    model.addAttribute("students", students);
    return "students";
  }

}
