package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.model.Application.LetterOfRecommendation;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.RecommendationRequest;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.LetterOfRecommendationRepository;
import com.example.abroad.respository.NoteRepository;
import com.example.abroad.respository.QuestionRepository;
import com.example.abroad.respository.RecommendationRequestRepository;
import com.example.abroad.respository.ResponseRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.example.abroad.service.UserService;

@Service
public record ApplicationService(
  ApplicationRepository applicationRepository,
  NoteRepository noteRepository,
  DocumentRepository documentRepository,
  ResponseRepository responseRepository,
  LetterOfRecommendationRepository letterOfRecommendationRepository,
  RecommendationRequestRepository recommendationRequestRepository,
  UserService userService
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

  public void saveRecommendationRequest(RecommendationRequest recommendationRequest) {
    recommendationRequestRepository.save(recommendationRequest);
  }
  public void saveLetterOfRecommendation(LetterOfRecommendation letterOfRecommendation) {
    letterOfRecommendationRepository.save(letterOfRecommendation);
  }
  public void deleteRecommendationRequest(RecommendationRequest recommendationRequest) {
    recommendationRequestRepository.delete(recommendationRequest);
  }
  public void deleteLetterOfRecommendation(LetterOfRecommendation letterOfRecommendation) {
    letterOfRecommendationRepository.delete(letterOfRecommendation);
  }

  public Optional<RecommendationRequest> findRecommendationRequest(Integer programId, String student, String recommender) {
    return recommendationRequestRepository.findById(
      new RecommendationRequest.ID(programId, student, recommender));
  }
  public Optional<LetterOfRecommendation> findLetterOfRecommendation(Integer programId, String student, String recommender) {
    return letterOfRecommendationRepository.findById(
      new LetterOfRecommendation.ID(programId, student, recommender));
  }
  public List<LetterOfRecommendation> getRecommendations(Program program, User student) {
    return letterOfRecommendationRepository.findById_ProgramIdAndId_Student(program.id(), student.username());
  }
  public List<RecommendationRequest> getRecommendationRequests(Program program, User student) {
    return recommendationRequestRepository.findById_ProgramIdAndId_Student(program.id(), student.username());
  }
  public Optional<Document> getDocument(Application application, Type type) {
    return documentRepository.findById_ProgramIdAndId_StudentAndId_Type(application.programId(), application.student(), type);
  }

  public List<RecommendationRequest> getRecRequestsByCode(String code) {
    return recommendationRequestRepository.findByCode(code);
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

  public Optional<Response> getResponse(Application application, Integer question) {
    return responseRepository.findById_ProgramIdAndId_StudentAndId_Question(application.programId(), application.student(), question);
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

  /**
   * Finds an application by its program ID (without needing a student username)
   * @param programId The program ID to search for
   * @return A list of applications for the given program
   */
  public List<Application> findByProgramId(Integer programId) {
    return applicationRepository.findById_ProgramId(programId);
  }

  /**
   * Gets a document by program ID, student username, and document type
   * @param programId The program ID
   * @param student The student username
   * @param type The document type
   * @return The document if found
   */
  public Optional<Document> getDocument(Integer programId, String student, Type type) {
    return documentRepository.findById_ProgramIdAndId_StudentAndId_Type(programId, student, type);
  }

  public void updatePaymentStatus(Application application, Application.PaymentStatus paymentStatus) {
    applicationRepository.save(application.withPaymentStatus(paymentStatus));
  }

  // Add these methods to ApplicationService
  public List<ApplicantDTO> getApprovedAndEnrolledApplicants(Integer programId) {
    // Find applications with "APPROVED" or "ENROLLED" status for this program
    List<String> statuses = Arrays.asList("APPROVED", "ENROLLED");
    List<Application> applications = applicationRepository.findById_ProgramId(programId)
            .stream()
            .filter(app -> statuses.contains(app.status().toString()))
            .collect(Collectors.toList());

    // Convert to DTOs with user information
    return applications.stream()
            .map(app -> {
              Optional<? extends User> userOpt = userService.findByUsername(app.student());
              if (userOpt.isEmpty()) {
                return null;
              }
              User user = userOpt.get();

              return new ApplicantDTO(
                      user.displayName(),
                      user.username(),
                      user.email(),
                      app.status().toString(),
                      app.paymentStatus() != null ? app.paymentStatus().toString() : "UNPAID"
              );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  public long countApprovedAndEnrolled(Integer programId) {
    List<Application.Status> statuses = Arrays.asList(Application.Status.APPROVED, Application.Status.ENROLLED);
    return applicationRepository.countByProgramIdAndStatuses(programId, statuses);
  }

  public long countFullyPaid(Integer programId) {
    List<Application.Status> statuses = Arrays.asList(Application.Status.APPROVED, Application.Status.ENROLLED);
    Application.PaymentStatus paymentStatus = Application.PaymentStatus.FULLY_PAID;
    return applicationRepository.countByProgramIdAndStatusesAndPaymentStatus(programId, statuses, paymentStatus);
  }

  public boolean updatePaymentStatus(Integer programId, String username, String paymentStatus) {
    Optional<Application> appOpt = applicationRepository.findById_ProgramIdAndId_Student(programId, username);
    if (appOpt.isEmpty()) {
      return false;
    }

    Application app = appOpt.get();
    Application.PaymentStatus status;
    try {
      status = Application.PaymentStatus.valueOf(paymentStatus);
    } catch (IllegalArgumentException e) {
      return false;
    }

    // Update payment status
    Application updatedApp = app.withPaymentStatus(status);
    applicationRepository.save(updatedApp);
    return true;
  }

  // DTO for displaying applicant information
  public record ApplicantDTO(
          String displayName,
          String username,
          String email,
          String status,
          String paymentStatus
  ) {}
}
