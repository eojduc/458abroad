package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.respository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public record DocumentService(
        DocumentRepository documentRepository,
        ProgramRepository programRepository  // for getting program dates
) {
    public record DocumentStatus(
            Application.Document.Type type,
            boolean submitted,
            LocalDate deadline,
            String applicationId,
            Instant submittedAt
    ) {}

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

    //comment here

    public long getMissingDocumentsCount(String applicationId) {
        var documents = documentRepository.findById_ApplicationId(applicationId);
        return Application.Document.Type.values().length - documents.size();
    }
}