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

  public Documents getLatestDocuments(String applicationId) {
    var latestDocuments = documentRepository.findByApplicationId(applicationId)
      .stream()
      .collect(Collectors.groupingBy(
        Document::type,
        Collectors.maxBy(Comparator.comparing(Document::timestamp))
      ));
    return new Documents(
      latestDocuments.getOrDefault(Type.MEDICAL_HISTORY, Optional.empty()),
      latestDocuments.getOrDefault(Type.CODE_OF_CONDUCT, Optional.empty()),
      latestDocuments.getOrDefault(Type.HOUSING, Optional.empty()),
      latestDocuments.getOrDefault(Type.ASSUMPTION_OF_RISK, Optional.empty())
    );
  }

  public record Documents(
    Optional<Document> medicalHistory,
    Optional<Document> codeOfConduct,
    Optional<Document> housing,
    Optional<Document> assumptionOfRisk
  ) {

  }



    public Note saveNote(Note note) {
    return noteRepository.save(note);
  }
}
