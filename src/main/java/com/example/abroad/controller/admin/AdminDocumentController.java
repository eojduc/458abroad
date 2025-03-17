package com.example.abroad.controller.admin;


import com.example.abroad.model.Application.Document;
import com.example.abroad.service.ApplicationService;
import org.springframework.transaction.annotation.Transactional;
import com.example.abroad.service.UserService;
import com.example.abroad.service.DocumentService;
import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.sql.Blob;
import java.sql.SQLException;

@Controller
@RequestMapping("/admin/applications/{programId}/{username}/documents")
public class AdminDocumentController {
  private static final Logger logger = LoggerFactory.getLogger(
    com.example.abroad.controller.student.DocumentController.class);

  private final DocumentService documentService;
  private final UserService userService;
  private final ApplicationService applicationService;

  public AdminDocumentController(DocumentService documentService, UserService userService,
    ApplicationService applicationService) {
    this.documentService = documentService;
    this.userService = userService;
    this.applicationService = applicationService;
  }

  @GetMapping("/{type}/view")
  @Transactional
  public ResponseEntity<?> viewDocument(
    @PathVariable Integer programId,
    @PathVariable String username,
    @PathVariable Document.Type type,
    HttpSession session
  ) {
    // First check if user is logged in
    var user = this.userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create("/login?error=Not logged in"))
        .build();
    }
    var application = applicationService.findByProgramIdAndStudentUsername(programId, username).orElse(null);
    if (application == null) {
//            logger.warn("Application not found: {}", applicationId);
      return ResponseEntity.notFound().build();
    }

    if (!application.student().equals(user.username())  && !userService.isAdmin(user)) {
//            logger.warn("User {} attempted unauthorized access to application {} owned by {}",
//                    user.username(), applicationId, application.student());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body("You do not have permission to access this document");
    }

    // Now proceed with getting the document
    var document = this.documentService.getDocument(programId, username, type);
    if (document.isEmpty()) {
      logger.warn("Document not found");
      return ResponseEntity.notFound().build();
    }

    try {
      logger.info("Document found, attempting to read blob");
      Blob blob = document.get().file();
      if (blob == null) {
        logger.error("Blob is null for document");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Document content is missing");
      }

      byte[] content = blob.getBinaryStream().readAllBytes();
      logger.info("Successfully read {} bytes from blob", content.length);

      return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
          "inline; filename=\"" + type.text() + ".pdf\"")
        .body(content);
    } catch (IOException | SQLException e) {
      logger.error("Failed to read document blob", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Failed to read document: " + e.getMessage());
    }
  }
}