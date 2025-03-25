package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.NoteRepository;
import com.example.abroad.respository.QuestionRepository;
import com.example.abroad.respository.ResponseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    return noteRepository.findByAuthor(author.username());
  }

  public List<Note> getNotes(Application application) {
    return noteRepository.findByProgramIdAndStudent(application.programId(), application.student());
  }

  public List<Application> findByProgram(Program program) {
    return applicationRepository.findById_ProgramId(program.id());
  }

  public Optional<Application> findByProgramAndStudent(Program program, User student) {
    return applicationRepository.findById_ProgramIdAndId_Student(program.id(), student.username());
  }

  public Optional<Application> findByProgramIdAndStudentUsername(Integer programId, String studentUsername) {
    return applicationRepository.findById_ProgramIdAndId_Student(programId, studentUsername);
  }
  public void updateStatus(Application application, Application.Status status) {
    applicationRepository.save(application.withStatus(status));
  }

  public void save(Application application) {
    applicationRepository.save(application);
  }

  public Documents getDocuments(Application application) {
    return new Documents(
      documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), Type.MEDICAL_HISTORY),
      documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), Type.CODE_OF_CONDUCT),
      documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), Type.HOUSING),
      documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), Type.ASSUMPTION_OF_RISK)
    );
  }



  public Optional<Document> getDocument(Application application, Type type) {
    return documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), type);
  }

  public record Documents(
    Optional<Document> medicalHistory,
    Optional<Document> codeOfConduct,
    Optional<Document> housing,
    Optional<Document> assumptionOfRisk
  ) {

  }
  public List<Response> getResponses(Application application) {
    return responseRepository.findById_ProgramIdAndId_Student(application.programId(), application.student())
      .stream()
      .sorted(Comparator.comparing(Response::question))
      .toList();
  }

  public void saveResponse(Application application, Integer question, String answer) {
    responseRepository.save(new Response(application.programId(), application.student(), question, answer));
  }

  public List<Application> findByStudent(User user) {
    return applicationRepository.findById_Student(user.username());
  }



    public void saveNote(Note note) {
      noteRepository.save(note);
    }
}
