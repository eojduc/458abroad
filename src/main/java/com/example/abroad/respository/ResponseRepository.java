package com.example.abroad.respository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Application.Response;

public interface ResponseRepository extends JpaRepository<Response, Response.ID> {

  Optional<Response> findById_ProgramIdAndId_StudentAndId_Question(Integer programId, String student, Integer questionId);
  List<Response> findById_ProgramIdAndId_Student(Integer programId, String student);

}
