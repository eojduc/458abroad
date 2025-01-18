package com.example.abroad;

import com.example.abroad.model.Student;
import com.example.abroad.respository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication
@Controller
public class AbroadApplication {
	@Autowired
	private StudentRepository studentRepository;

	@GetMapping("/")
    public String helloWorld() {
        return "Hello World";
    }
	
	@GetMapping("/students")
	public String getStudents(Model model) {
		studentRepository.save(new Student("johnny123", "secure", "johnny@gmail.com", "John Smith"));
		studentRepository.save(new Student("jane123", "password", "jane@gmail.com", "Jane Doe"));
		var students = studentRepository.findAll();
		model.addAttribute("students", students);
		return "students";
	}

	public static void main(String[] args) {
		SpringApplication.run(AbroadApplication.class, args);
	}

}
