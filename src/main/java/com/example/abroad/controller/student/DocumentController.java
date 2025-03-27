package com.example.abroad.controller.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
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
import org.springframework.ui.Model;
import org.springframework.http.ContentDisposition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/applications/{applicationId}/documents")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final UserService userService;
    private final ProgramService programService;
    private final ApplicationService applicationService;

    public DocumentController(DocumentService documentService, UserService userService, ProgramService programService, ApplicationService applicationService) {
        this.documentService = documentService;
        this.userService = userService;
        this.programService = programService;
        this.applicationService = applicationService;
    }

    @PostMapping
    public String uploadDocument(
            @PathVariable String applicationId,
            @RequestParam Document.Type type,
            @RequestParam MultipartFile file,
            HttpSession session
    ) {
        var user = this.userService.findUserFromSession(session).orElse(null);
        logger.info("Attempting to upload document for application {} type {}", applicationId, type);
        if (userService.findUserFromSession(session).isEmpty()) {
            return "redirect:/login?error=Not logged in";
        }
        boolean isUpdate = documentService.getDocument(Integer.valueOf(applicationId), user.username(), type).isPresent();
        try {
            documentService.uploadDocument(Integer.valueOf(applicationId), user.username(), type, file);
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
    public Object viewDocument(
            @PathVariable Integer applicationId,
            @PathVariable Document.Type type,
            HttpSession session,
            Model model
    ) {
        // First check if user is logged in
        var userOpt = this.userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=Not logged in";
        }

        // Get the current logged-in user
        var user = userOpt.get();

        logger.info("User {} attempting to view document for application {} type {}",
                user.username(), applicationId, type);

        // Check permission and get the student username if permission granted
        var studentUsernameOpt = checkDocumentAccessPermission(user, applicationId, type);

        if (studentUsernameOpt.isEmpty()) {
            logger.warn("User {} attempted unauthorized access to document for application {}",
                    user.username(), applicationId);
            model.addAttribute("message", "You do not have permission to access this document");
            return "permission-error";
        }

        String studentUsername = studentUsernameOpt.get();

        // Now proceed with getting the document
        var document = applicationService.getDocument(applicationId, studentUsername, type);
        if (document.isEmpty()) {
            logger.warn("Document not found");
            model.addAttribute("message", "Document not found");
            return "permission-error";
        }

        try {
            logger.info("Document found, attempting to read blob");
            Blob blob = document.get().file();
            if (blob == null) {
                logger.error("Blob is null for document");
                model.addAttribute("message", "Document content is missing");
                return "permission-error";
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
            model.addAttribute("message", "Failed to read document: " + e.getMessage());
            return "permission-error";
        }
    }


    /**
     * Checks if a user has permission to view documents for an application
     * @param user The user attempting to access the document
     * @param programId The ID of the program
     * @param documentType The type of document being accessed
     * @return Optional containing the username of the student if permission is granted, empty otherwise
     */
    private Optional<String> checkDocumentAccessPermission(User user, Integer programId, Document.Type documentType) {
        // 1. If the user is the student themselves, they can only view their own documents
        // First check for the student's own application
        Optional<Application> studentOwnApplication = applicationService.findByProgramIdAndStudentUsername(
                programId, user.username());

        if (studentOwnApplication.isPresent()) {
            // The user is the student of this application
            return Optional.of(user.username());
        }

        // 2, 3, 4. For faculty leads, admins, or reviewers, we need to find the application first
        // to determine the student username

        // Check if the user has a role that allows them to view other students' documents
        boolean hasPermissionRole = userService.isAdmin(user) ||
                userService.isReviewer(user);

        // Check if the user is a faculty lead for this program
        boolean isFacultyForProgram = false;
        var programOpt = programService.findById(programId);
        if (programOpt.isPresent()) {
            isFacultyForProgram = programService.isFacultyLead(programOpt.get(), user);
        }

        // If the user has the necessary role/permission
        if (hasPermissionRole || isFacultyForProgram) {
            // Find all applications for this program
            List<Application> programApplications = applicationService.findByProgramId(programId);

            // Look for an application with the requested document type
            for (Application app : programApplications) {
                Optional<Document> document = applicationService.getDocument(app, documentType);
                if (document.isPresent()) {
                    // Found an application with the document - return the student username
                    return Optional.of(app.student());
                }
            }
        }

        // No permission or no matching document found
        return Optional.empty();
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