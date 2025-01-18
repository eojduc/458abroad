package com.example.abroad.respository;

import com.example.abroad.database.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends
    JpaRepository<Student, String> {
}
