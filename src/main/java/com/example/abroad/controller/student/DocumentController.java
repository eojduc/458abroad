package com.example.abroad.controller.student;

import com.example.abroad.model.Application.Document;
import org.springframework.transaction.annotation.Transactional;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.DocumentService;
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
import java.util.Optional;

@Controller
@RequestMapping("/applications/{applicationId}/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping
    public String uploadDocument(
            @PathVariable String applicationId,
            @RequestParam("type") Document.Type type,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (this.userService.findUserFromSession(session).isEmpty()) {
            return "redirect:/login?error=Not logged in";
        }
        boolean isUpdate = this.documentService.getDocument(applicationId, type).isPresent();
        try {
            this.documentService.uploadDocument(applicationId, type, file);
            String message = isUpdate ? "Document updated successfully" : "Document uploaded successfully";
            redirectAttributes.addFlashAttribute("message", message);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            String errorMessage = isUpdate ? "Failed to update document" : "Failed to upload document";
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }
        return "redirect:/applications";
    }

    @GetMapping("/{type}/view")
    @Transactional
    public ResponseEntity<?> viewDocument(
            @PathVariable String applicationId,
            @PathVariable Document.Type type,
            HttpSession session
    ) {
        if (this.userService.findUserFromSession(session).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=Not logged in"))
                    .build();
        }

        logger.info("Attempting to view document for application {} type {}", applicationId, type);

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
            String formPath = this.documentService.getBlankFormPath(type);
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