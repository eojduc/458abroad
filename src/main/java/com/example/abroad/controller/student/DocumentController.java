package com.example.abroad.controller.student;

import com.example.abroad.model.Application.Document;
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
@RequestMapping("/applications/{applicationId}/documents")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping
    public String uploadDocument(
            @PathVariable String applicationId,
            @RequestParam Document.Type type,
            @RequestParam MultipartFile file,
            HttpSession session
    ) {
        logger.info("Attempting to upload document for application {} type {}", applicationId, type);
        if (userService.findUserFromSession(session).isEmpty()) {
            return "redirect:/login?error=Not logged in";
        }
        boolean isUpdate = documentService.getDocument(applicationId, type).isPresent();
        try {
            documentService.uploadDocument(applicationId, type, file);
            String message = isUpdate ? "Document updated successfully" : "Document uploaded successfully";
            return "redirect:/applications/" + applicationId + "?success=" + message + "#documents";
        } catch (IllegalArgumentException e) {
            // This correctly handles validation errors
            return "redirect:/applications/" + applicationId + "?error=" + e.getMessage() + "#documents";
        } catch (RuntimeException e) {
            // This is a generic error handler
            logger.error("Error uploading document", e);
            String errorMessage = isUpdate ? "Failed to update document: " : "Failed to upload document: ";
            errorMessage += e.getMessage();
            return "redirect:/applications/" + applicationId + "?error=" + errorMessage + "#documents";
        }
    }

    @GetMapping("/{type}/view")
    @Transactional
    public ResponseEntity<?> viewDocument(
            @PathVariable String applicationId,
            @PathVariable Document.Type type,
            HttpSession session
    ) {
        // First check if user is logged in
        var userOpt = this.userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=Not logged in"))
                    .build();
        }

        // Get the current logged-in user
        var user = userOpt.get();

        logger.info("User {} attempting to view document for application {} type {}",
                user.username(), applicationId, type);

        // Get the application to check ownership
        var applicationOpt = documentService.getApplicationById(applicationId);
        if (applicationOpt.isEmpty()) {
            logger.warn("Application not found: {}", applicationId);
            return ResponseEntity.notFound().build();
        }

        // Check if the current user is the owner of the application
        var application = applicationOpt.get();
        if (!application.student().equals(user.username())  && !user.isAdmin()) {
            logger.warn("User {} attempted unauthorized access to application {} owned by {}",
                    user.username(), applicationId, application.student());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to access this document");
        }

        // Now proceed with getting the document
        var document = this.documentService.getDocument(applicationId, type);
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

    @GetMapping("/{type}/blank")
    public ResponseEntity<?> downloadBlankForm(
            @PathVariable Document.Type type,
            HttpSession session
    ) {
        if (this.userService.findUserFromSession(session).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=Not logged in"))
                    .build();
        }

        try {
            String formPath = DocumentService.getBlankFormPath(type);
            Resource resource = new ClassPathResource("static" + formPath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = FileCopyUtils.copyToByteArray(resource.getInputStream());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + type.text() + "-blank.pdf\"")
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read blank form");
        }
    }
}