package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.ProgramRepository;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public record DocumentService(
        DocumentRepository documentRepository,
        ProgramRepository programRepository
) {
    // Constants for PDF validation
    private static final long MAX_PDF_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_MAGIC_NUMBER = "%PDF-";

    public record DocumentStatus(
            Application.Document.Type type,
            boolean submitted,
            LocalDate deadline,
            String applicationId,
            Instant submittedAt
    ) {}

    public record ValidationResult(boolean valid, String errorMessage) {}

    public List<DocumentStatus> getDocumentStatuses(String applicationId, Integer programId) {
        var documents = documentRepository.findById_ApplicationId(applicationId);
        var program = programRepository.findById(programId).orElseThrow();

        // Set deadline 30 days before program start
        LocalDate deadline = program.startDate().minusDays(30);

        return Arrays.stream(Application.Document.Type.values())
                .map(type -> {
                    var doc = documents.stream()
                            .filter(d -> d.type() == type)
                            .findFirst();

                    return new DocumentStatus(
                            type,
                            doc.isPresent(),
                            deadline,
                            applicationId,
                            doc.map(Application.Document::timestamp).orElse(null)
                    );
                })
                .toList();
    }

    public boolean hasAllRequiredDocuments(String applicationId) {
        var documents = documentRepository.findById_ApplicationId(applicationId);
        return documents.size() == Application.Document.Type.values().length;
    }

    public long getMissingDocumentsCount(String applicationId) {
        var documents = documentRepository.findById_ApplicationId(applicationId);
        return Application.Document.Type.values().length - documents.size();
    }

    public ValidationResult validatePDF(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            return new ValidationResult(false, "File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_PDF_SIZE) {
            return new ValidationResult(false, "File size exceeds 10MB limit");
        }

        // Check content type
        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            return new ValidationResult(false, "File must be a PDF");
        }

        // Check PDF magic number (file signature)
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 5) {
                return new ValidationResult(false, "Invalid PDF file");
            }
            String magicNumber = new String(bytes, 0, 5, StandardCharsets.UTF_8);
            if (!magicNumber.equals(PDF_MAGIC_NUMBER)) {
                return new ValidationResult(false, "Invalid PDF file format");
            }
        } catch (IOException e) {
            return new ValidationResult(false, "Error reading file");
        }

        return new ValidationResult(true, null);
    }

    public boolean uploadDocument(String applicationId, Application.Document.Type type, MultipartFile file) {
        ValidationResult validation = validatePDF(file);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.errorMessage());
        }

        try {
            Application.Document document = new Application.Document(
                    type,
                    Instant.now(),
                    BlobProxy.generateProxy(file.getInputStream(), file.getSize()),
                    applicationId
            );
            documentRepository.save(document);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document", e);
        }
    }

    public static String getBlankFormPath(Application.Document.Type type) {
        return switch (type) {
            case ASSUMPTION_OF_RISK -> "/forms/assumption-of-risk.pdf";
            case CODE_OF_CONDUCT -> "/forms/code-of-conduct.pdf";
            case MEDICAL_HISTORY -> "/forms/medical-history.pdf";
            case HOUSING -> "/forms/housing-form.pdf";
        };
    }
}