package com.example.abroad.respository;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Application.Document, Document.ID> {

  Optional<Document> findById_ProgramIdAndId_StudentAndId_Type(Integer programId, String student, Document.Type type);

  List<Document> findById_ProgramIdAndId_Student(Integer programId, String student);
}
