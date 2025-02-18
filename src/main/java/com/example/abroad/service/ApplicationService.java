package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.model.Application.Note;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.NoteRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public record ApplicationService(ApplicationRepository applicationRepository, NoteRepository noteRepository,
  DocumentRepository documentRepository) {

  public List<Note> getNotes(String applicationId) {
    return noteRepository.findByApplicationId(applicationId);
  }

  public List<Application> findByProgramId(Integer programId) {
    return applicationRepository.findByProgramId(programId);
  }

  public void updateStatus(String applicationId, Application.Status status) {
    applicationRepository.updateStatus(applicationId, status);
  }

  public Optional<Application> findById(String applicationId) {
    return applicationRepository.findById(applicationId);
  }

  public List<Application.Document> getDocuments(String applicationId) {
    return documentRepository.findByApplicationId(applicationId);
  }

  public Map<Type, Optional<Document>> getLatestDocuments(String applicationId) {
    return documentRepository.findByApplicationId(applicationId)
      .stream()
      .collect(Collectors.groupingBy(
        Document::type,
        Collectors.maxBy(Comparator.comparing(Document::timestamp))
      ));
  }

  public Note saveNote(Note note) {
    return noteRepository.save(note);
  }
}
