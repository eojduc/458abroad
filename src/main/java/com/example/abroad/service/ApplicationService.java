package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Response;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.NoteRepository;
import com.example.abroad.respository.ResponseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public record ApplicationService(
  ApplicationRepository applicationRepository,
  NoteRepository noteRepository,
  DocumentRepository documentRepository,
  ResponseRepository responseRepository
) {

  public List<Note> getNotes(String applicationId) {
    return noteRepository.findByApplicationId(applicationId);
  }

  public List<Application> findByProgramId(Integer programId) {
    return applicationRepository.findByProgramId(programId);
  }

  public Optional<Application> findByProgramIdAndStudent(Integer programId, String student) {
    return applicationRepository.findByProgramIdAndStudent(programId, student);
  }
  public void updateStatus(String applicationId, Application.Status status) {
    var application = applicationRepository.findById(applicationId).orElse(null);
    if (application != null) {
      applicationRepository.save(application.withStatus(status));;
    }
  }

  public Optional<Application> findById(String applicationId) {
    return applicationRepository.findById(applicationId);
  }

  public void save(Application application) {
    applicationRepository.save(application);
  }

  public Documents getLatestDocuments(String applicationId) {
    return new Documents(
      documentRepository.findById_ApplicationIdAndId_Type(applicationId, Type.MEDICAL_HISTORY),
      documentRepository.findById_ApplicationIdAndId_Type(applicationId, Type.CODE_OF_CONDUCT),
      documentRepository.findById_ApplicationIdAndId_Type(applicationId, Type.HOUSING),
      documentRepository.findById_ApplicationIdAndId_Type(applicationId, Type.ASSUMPTION_OF_RISK)
    );
  }

  public record Documents(
    Optional<Document> medicalHistory,
    Optional<Document> codeOfConduct,
    Optional<Document> housing,
    Optional<Document> assumptionOfRisk
  ) {

  }
  public List<Response> getResponses(String applicationId) {
    return responseRepository.findById_ApplicationId(applicationId)
      .stream()
      .sorted(Comparator.comparing(Response::question))
      .toList();
  }

  public void saveResponse(String applicationId, Response.Question question, String answer) {
    responseRepository.save(new Response(applicationId, question, answer));
  }



    public Note saveNote(Note note) {
    return noteRepository.save(note);
  }
}
