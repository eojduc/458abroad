package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
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

  public void deleteNote(Integer noteId) {
    noteRepository.deleteById(noteId);
  }

  public List<Note> findNotesByAuthor(User author) {
    return noteRepository.findByUsername(author.username());
  }

  public List<Note> getNotes(Application application) {
    return noteRepository.findByApplicationId(application.id());
  }

  public List<Application> findByProgram(Program program) {
    return applicationRepository.findByProgramId(program.id());
  }

  public Optional<Application> findByProgramAndStudent(Program program, User student) {
    return applicationRepository.findByProgramIdAndStudent(program.id(), student.username());
  }
  public void updateStatus(Application application, Application.Status status) {
    applicationRepository.save(application.withStatus(status));
  }

  public Optional<Application> findById(String applicationId) {
    return applicationRepository.findById(applicationId);
  }

  public void save(Application application) {
    applicationRepository.save(application);
  }

  public Documents getLatestDocuments(Application application) {
    return new Documents(
      documentRepository.findById_ApplicationIdAndId_Type(application.id(), Type.MEDICAL_HISTORY),
      documentRepository.findById_ApplicationIdAndId_Type(application.id(), Type.CODE_OF_CONDUCT),
      documentRepository.findById_ApplicationIdAndId_Type(application.id(), Type.HOUSING),
      documentRepository.findById_ApplicationIdAndId_Type(application.id(), Type.ASSUMPTION_OF_RISK)
    );
  }

  public record Documents(
    Optional<Document> medicalHistory,
    Optional<Document> codeOfConduct,
    Optional<Document> housing,
    Optional<Document> assumptionOfRisk
  ) {

  }
  public List<Response> getResponses(Application application) {
    return responseRepository.findById_ApplicationId(application.id())
      .stream()
      .sorted(Comparator.comparing(Response::question))
      .toList();
  }

  public void saveResponse(Application application, Response.Question question, String answer) {
    responseRepository.save(new Response(application.id(), question, answer));
  }

  public List<Application> findByStudent(User user) {
    return applicationRepository.findByStudent(user.username());
  }



    public void saveNote(Note note) {
      noteRepository.save(note);
    }
}
