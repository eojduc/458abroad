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

  public StudentsController(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }
  @GetMapping("/students")
  public String getStudents(Model model, HttpServletRequest request) {
    var students = studentRepository.findAll();
    // here until auth is set up, we'll just set the first student as the user
    students.stream().findFirst().ifPresent(student -> UserService.setUser(request, student));
    model.addAttribute("students", students);
    return "students :: page";
  }

}
