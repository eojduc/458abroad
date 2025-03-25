package com.example.abroad.respository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, String> {

  Optional<Application> findById_ProgramIdAndId_Student(Integer programId, String studentId);

  List<Application> findById_ProgramId(Integer programId);

  List<Application> findById_Student(String studentId);

}
