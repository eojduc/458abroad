package com.example.abroad.respository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Application.Response;

public interface ResponseRepository extends JpaRepository<Response, Response.ID> {



  List<Response> findById_ProgramIdAndId_Student(Integer programId, String student);

}
