package com.example.abroad.respository;

import com.example.abroad.model.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {

  Optional<Student> findByUsername(String username);


}
