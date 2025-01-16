package com.example.abroad;

import com.example.abroad.database.Student;
import com.example.abroad.respository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class AbroadApplication {
	@Autowired
	private StudentRepository studentRepository;

	@GetMapping("/students")
	public List<Student> getStudents() {
		List<Student> students = new ArrayList<>();
		for (Student student : studentRepository.findAll()) {
			students.add(student);
		}
		return students;
	}

	public static void main(String[] args) {
		SpringApplication.run(AbroadApplication.class, args);
	}

}
