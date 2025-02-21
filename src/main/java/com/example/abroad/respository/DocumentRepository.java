package com.example.abroad.respository;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Application.Document, Document.ID> {

  List<Document> findById_ApplicationId(String applicationId);

  Optional<Document> findById_ApplicationIdAndId_Type(String applicationId, Document.Type type);

}
