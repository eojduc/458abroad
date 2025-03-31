package com.example.abroad.controller;

import com.example.abroad.service.LetterOfRecommendationService;
import com.example.abroad.service.LetterOfRecommendationService.GetLetterFile.Success;
import com.example.abroad.view.Alerts;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public record LetterOfRecommendationController( LetterOfRecommendationService service ) {
  @GetMapping("/letter-of-rec/{code}.pdf")
  public ResponseEntity<byte[]> getPdf(@PathVariable Integer code) throws IOException, SQLException {
    return switch (service.getLetterFile(code)) {
      case Success(var pdfBytes) -> {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("letter-of-rec.pdf").build());
        yield new ResponseEntity<>(pdfBytes.getBinaryStream().readAllBytes(), headers, HttpStatus.OK);
      }
      case LetterOfRecommendationService.GetLetterFile.LetterNotFound() -> ResponseEntity.notFound().build();
    };
  }

  @PostMapping("/rec-request/{code}")
  public String uploadLetter(@PathVariable Integer code, @RequestParam MultipartFile file) {
    return switch (service.uploadLetter(code, file)) {
      case LetterOfRecommendationService.UploadLetter.Success() -> "redirect:/rec-request/" + code + "?success=Letter uploaded successfully";
      case LetterOfRecommendationService.UploadLetter.RequestNotFound() -> "redirect:/rec-request/" + code + "?requestNotFound=Request not found";
      case LetterOfRecommendationService.UploadLetter.FileSaveError() -> "redirect:/rec-request/" + code + "?error=Invalid file";
      case LetterOfRecommendationService.UploadLetter.FileTooBig() -> "redirect:/rec-request/" + code + "?error=File size exceeded";
      case LetterOfRecommendationService.UploadLetter.FileEmpty() -> "redirect:/rec-request/" + code + "?error=File must not be empty";
    };
  }

  @GetMapping("/rec-request/{code}")
  public String getRequestPage(@PathVariable Integer code, Model model,
    @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info
  ) {
    return switch (service.getRequestPage(code)) {
      case LetterOfRecommendationService.GetRequestPage.Success(var name, var email, var submitted, var studentName, var studentEmail, var programTitle) -> {
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("submitted", submitted);
        model.addAttribute("studentName", studentName);
        model.addAttribute("studentEmail", studentEmail);
        model.addAttribute("code", code);
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        model.addAttribute("programTitle", programTitle);
        yield "recommendation-request";
      }
      case LetterOfRecommendationService.GetRequestPage.RequestNotFound() -> "redirect:/";
    };
  }
}
