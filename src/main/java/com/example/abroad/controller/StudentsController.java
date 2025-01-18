package com.example.abroad.controller;

import com.example.abroad.model.Student;
import com.example.abroad.respository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentsController {

  private final StudentRepository studentRepository;

  @Autowired
  public StudentsController(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  @GetMapping("/students")
  public String getStudents(Model model) {
    studentRepository.save(
      new Student("johnny123", "secure", "johnny@gmail.com", "John Smith"));
    studentRepository.save(new Student("jane123", "password", "jane@gmail.com", "Jane Doe"));
    var students = studentRepository.findAll();
    model.addAttribute("students", students);
    return "students";
  }

}
