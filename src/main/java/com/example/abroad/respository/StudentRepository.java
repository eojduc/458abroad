package com.example.abroad.respository;

import com.example.abroad.database.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<Student, String> {
}
