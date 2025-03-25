package com.example.abroad.service;

import com.example.abroad.model.Application;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Blob;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public record LetterOfRecommendationService(
  UserService userService,
  ProgramService programService,
  ApplicationService applicationService,
  EmailService emailService
) {


  public sealed interface RequestRecommendation {
    record Success() implements RequestRecommendation { }
    record UserNotFound() implements RequestRecommendation { }
    record ProgramNotFound() implements RequestRecommendation { }
    record StudentAlreadyAsked() implements RequestRecommendation { }
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
    var code = ThreadLocalRandom.current().nextInt(100000, 1000000);
    var recRequest = new Application.RecommendationRequest(programId, user.username(), email, name, code);
    applicationService.saveRecommendationRequest(recRequest);
    emailService.sendRecommendationRequestEmail(email, name, user, program, code);
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
    applicationService.deleteRecommendationRequest(request);
    return new DeleteRecommendationRequest.Success();
  }
  public sealed interface UploadLetter {
    record Success() implements UploadLetter { }
    record UserNotFound() implements UploadLetter { }
    record ProgramNotFound() implements UploadLetter { }
    record StudentNotAsked() implements UploadLetter { }
    record FileSaveError() implements UploadLetter { }
  }

  public UploadLetter uploadLetter(Integer programId, String student, String email, MultipartFile file) {
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new UploadLetter.ProgramNotFound();
    }
    var request = applicationService.findRecommendationRequest(programId, student, email).orElse(null);
    if (request == null) {
      return new UploadLetter.StudentNotAsked();
    }
    try {
      var letter = new Application.LetterOfRecommendation(programId, student, email, toBlob(file), Instant.now(), request.name());
      applicationService.saveLetterOfRecommendation(letter);
      return new UploadLetter.Success();
    } catch (Exception e) {
      return new UploadLetter.FileSaveError();
    }
  }

  public Blob toBlob(MultipartFile file) throws IOException {
    return BlobProxy.generateProxy(file.getInputStream(), file.getSize());
  }

}
