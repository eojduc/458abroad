package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.service.LetterOfRecommendationService.GetRequestPage.RequestNotFound;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Blob;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public record LetterOfRecommendationService(
  UserService userService,
  ProgramService programService,
  ApplicationService applicationService,
  EmailService emailService
) {


  private static final Logger log = LoggerFactory.getLogger(LetterOfRecommendationService.class);

  public sealed interface RequestRecommendation {
    record Success() implements RequestRecommendation { }
    record UserNotFound() implements RequestRecommendation { }
    record ProgramNotFound() implements RequestRecommendation { }
    record StudentAlreadyAsked() implements RequestRecommendation { }
    record EmailError() implements RequestRecommendation { }
  }
  public sealed interface GetRequestPage {
    record Success(String name, String email, Boolean submitted, String studentName, String studentEmail, String programTitle) implements GetRequestPage { }
    record RequestNotFound() implements GetRequestPage { }
  }
  public GetRequestPage getRequestPage(String code) {
    System.out.println("Code: " + code);
    var requests = applicationService.getRecRequestsByCode(code);
    if (requests.isEmpty()) {
      return new RequestNotFound();
    }
    var request = requests.get(0);
    var letter = applicationService.findLetterOfRecommendation(request.programId(), request.student(), request.email());
    var submitted = letter.isPresent();
    var studentUser = userService.findByUsername(request.student()).orElse(null);
    if (studentUser == null) {
      return new RequestNotFound();
    }
    var program = programService.findById(request.programId()).orElse(null);
    if (program == null) {
      return new RequestNotFound();
    }
    return new GetRequestPage.Success(request.name(), request.email(), submitted, studentUser.displayName(), studentUser.email(), program.title());
  }

  public RequestRecommendation requestRecommendation(Integer programId, HttpSession session, String email, String name) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new RequestRecommendation.UserNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new RequestRecommendation.ProgramNotFound();
    }
    if (applicationService.findRecommendationRequest(programId, user.username(), email).isPresent()) {
      return new RequestRecommendation.StudentAlreadyAsked();
    }
    var code = UUID.randomUUID().toString();
    var recRequest = new Application.RecommendationRequest(programId, user.username(), email, name, code);
    applicationService.saveRecommendationRequest(recRequest);
    try {
      emailService.sendRequestEmail(email, name, program, user, code);
    } catch (Exception e) {
      log.error("Error sending email", e);
      applicationService.deleteRecommendationRequest(recRequest);
      return new RequestRecommendation.EmailError();
    }
    return new RequestRecommendation.Success();
  }
  public sealed interface DeleteRecommendationRequest {
    record Success() implements DeleteRecommendationRequest { }
    record UserNotFound() implements DeleteRecommendationRequest { }
  }

  public DeleteRecommendationRequest deleteRecommendationRequest(Integer programId, HttpSession session, String email) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new DeleteRecommendationRequest.UserNotFound();
    }
    var request = applicationService.findRecommendationRequest(programId, user.username(), email).orElse(null);
    if (request == null) {
      return new DeleteRecommendationRequest.Success();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new DeleteRecommendationRequest.Success();
    }
    emailService.sendCancelRequestEmail(email, request.name(), program, user);
    applicationService.deleteRecommendationRequest(request);
    applicationService.findLetterOfRecommendation(programId, user.username(), email)
      .ifPresent(applicationService::deleteLetterOfRecommendation);
    return new DeleteRecommendationRequest.Success();
  }
  public sealed interface UploadLetter {
    record Success() implements UploadLetter { }
    record RequestNotFound() implements UploadLetter { }
    record FileSaveError() implements UploadLetter { }
    record FileTooBig() implements UploadLetter { }
    record FileEmpty() implements UploadLetter { }
  }

  public UploadLetter uploadLetter(String code, MultipartFile file) {
    if (file.isEmpty()) {
      return new UploadLetter.FileEmpty();
    }
    if (file.getSize() > 10 * 1024 * 1024) {
      return new UploadLetter.FileTooBig();
    }
    var requests = applicationService.getRecRequestsByCode(code);
    if (requests.isEmpty()) {
      return new UploadLetter.RequestNotFound();
    }
    var request = requests.getFirst();
    try {
      var letter = new Application.LetterOfRecommendation(request.programId(), request.student(), request.email(), toBlob(file), Instant.now(), request.name());
      applicationService.saveLetterOfRecommendation(letter);
      return new UploadLetter.Success();
    } catch (Exception e) {
      return new UploadLetter.FileSaveError();
    }
  }

  public Blob toBlob(MultipartFile file) throws IOException {
    return BlobProxy.generateProxy(file.getInputStream(), file.getSize());
  }
  public sealed interface GetLetterFile {
    record Success(Blob file) implements GetLetterFile { }
    record LetterNotFound() implements GetLetterFile { }
  }

  public GetLetterFile getLetterFile(String code) {
    var requests = applicationService.getRecRequestsByCode(code);
    if (requests.isEmpty()) {
      return new GetLetterFile.LetterNotFound();
    }
    var request = requests.getFirst();
    var letter = applicationService.findLetterOfRecommendation(request.programId(), request.student(), request.email());
    if (letter.isEmpty()) {
      return new GetLetterFile.LetterNotFound();
    }
    return new GetLetterFile.Success(letter.get().file());
  }

}
