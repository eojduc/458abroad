package com.example.abroad.respository;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Application.Document, Integer> {

  List<Document> findByApplicationId(String applicationId);

}
