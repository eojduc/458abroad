package com.example.abroad.controller.admin;

import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.AdminProgramInfoService;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminProgramsController(StudentRepository studentRepository, UserService userService, AdminRepository adminRepository, AdminProgramInfoService service, FormatService formatter) {
  @GetMapping("/admin/programs")
  public String getApplicationInfo(HttpServletRequest request, Model model) {
    var students = studentRepository.findAll();
    var admins = adminRepository.findAll();
    // here until auth is set up, we'll just set the first student as the user
//  students.stream().findFirst().ifPresent(student -> userService.setUser(request, student));
    admins.stream().findFirst().ifPresent(admin -> userService.setUser(request, admin));
    var name = Optional.ofNullable(request.getSession().getAttribute("name"))
        .filter(obj -> obj instanceof String)
        .map(obj -> (String) obj)
        .orElse("world");
    model.addAttribute("name", name);
    return "admin/programs :: page";
  }
}
