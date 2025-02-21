package com.example.abroad.respository;

import com.example.abroad.model.Application.Response;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<Response, Response.ID> {




  List<Response> findById_ApplicationIdAndId_Question(String applicationId, Response.Question question);

  List<Response> findById_ApplicationId(String applicationId);

}
