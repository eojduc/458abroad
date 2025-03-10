package com.example.abroad.service;

import com.example.abroad.model.Application.Document.Type;
import com.example.abroad.respository.ApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import com.example.abroad.model.Application;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.ProgramRepository;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ProgramRepository programRepository;
    private final FormatService formatService;
    private final ApplicationRepository applicationRepository;
    private static final long MAX_PDF_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_MAGIC_NUMBER = "%PDF-";
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final UserService userService;

    public DocumentService(
            DocumentRepository documentRepository,
            ProgramRepository programRepository,
            ApplicationRepository applicationRepository,
            FormatService formatService, UserService userService) {
        this.documentRepository = documentRepository;
        this.programRepository = programRepository;
        this.formatService = formatService;
        this.applicationRepository = applicationRepository;
        this.userService = userService;
    }

    public record DocumentStatus(
            Application.Document.Type type,
            boolean submitted,
            LocalDate deadline,
            Integer programId,
            String student,
            Instant submittedAt,
            String formattedTimestamp
    ) {}

    public sealed interface Validation {
        record Valid() implements Validation {}
        record Invalid(String errorMessage) implements Validation {}
    }

    public List<DocumentStatus> getDocumentStatuses(HttpSession session, Integer programId) {
        var user = userService.findUserFromSession(session).orElseThrow();
        var documents = this.documentRepository.findById_ProgramIdAndId_Student(programId, user.username());
        var program = this.programRepository.findById(programId).orElseThrow();

        LocalDate deadline = program.documentDeadline();

        return Arrays.stream(Application.Document.Type.values())
                .map(type -> {
                    var doc = documents.stream()
                            .filter(d -> d.type() == type)
                            .findFirst();

                    Instant timestamp = doc.map(Application.Document::timestamp).orElse(null);
                    String formattedTime = timestamp != null ?
                            formatService.formatInstant(timestamp) :
                            null;

                    return new DocumentStatus(
                            type,
                            doc.isPresent(),
                            deadline,
                            programId,
                            user.username(),
                            timestamp,
                            formattedTime
                    );
                })
                .toList();
    }


    public long getMissingDocumentsCount(HttpSession session, Integer programId) {
        var user = userService.findUserFromSession(session).orElseThrow();
        var documents = this.documentRepository.findById_ProgramIdAndId_Student(programId, user.username());
        return Application.Document.Type.values().length - documents.size();
    }

    @Transactional
    public void uploadDocument(Integer programId, String student, Type type, MultipartFile file) {
        var validation = validatePDF(file);
        if (validation instanceof Validation.Invalid invalid) {
            throw new IllegalArgumentException(invalid.errorMessage());
        }

        try {
            logger.info("Uploading document of size: {}", file.getSize());

            Optional<Application.Document> existingDoc = documentRepository.findById(
                    new Application.Document.ID(type, programId, student)
            );

            // Create new document with current timestamp
            Application.Document document = new Application.Document(
                    type,
                    Instant.now(),
                    BlobProxy.generateProxy(file.getInputStream(), file.getSize()),
                    programId,
                student
            );

            // Save will either create new or update existing
            this.documentRepository.save(document);

            if (existingDoc.isPresent()) {
                logger.info("Updated existing document for type: {} with new timestamp", type);
            } else {
                logger.info("Created new document for type: {}", type);
            }

        } catch (IOException e) {
            logger.error("Failed to store document", e);
            throw new IllegalArgumentException("Failed to process the file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error storing document", e);
            throw new IllegalArgumentException("Failed to save document: " + e.getMessage());
        }
    }

    private Validation validatePDF(MultipartFile file) {
        if (file.isEmpty()) {
            return new Validation.Invalid("File is empty");
        }

        if (file.getSize() > MAX_PDF_SIZE) {
            return new Validation.Invalid("File is too large");
        }

        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            return new Validation.Invalid("File must be a PDF");
        }

        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 5) {
                return new Validation.Invalid("Invalid PDF file format");
            }
            String magicNumber = new String(bytes, 0, 5, StandardCharsets.UTF_8);
            if (!magicNumber.equals(PDF_MAGIC_NUMBER)) {
                return new Validation.Invalid("Invalid PDF file format");
            }
        } catch (IOException e) {
            return new Validation.Invalid("Failed to read file");
        }

        return new Validation.Valid();
    }

    @Transactional
    public Optional<Application.Document> getDocument(Integer programId, String student, Application.Document.Type type) {
        return this.documentRepository.findById(new Application.Document.ID(type, programId, student));
    }

    public static String getBlankFormPath(Application.Document.Type type) {
        return switch (type) {
            case ASSUMPTION_OF_RISK -> "/forms/assumption-of-risk.pdf";
            case CODE_OF_CONDUCT -> "/forms/code-of-conduct.pdf";
            case MEDICAL_HISTORY -> "/forms/medical-history.pdf";
            case HOUSING -> "/forms/housing-form.pdf";
        };
    }

    public Optional<Application> getApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId);
    }
}