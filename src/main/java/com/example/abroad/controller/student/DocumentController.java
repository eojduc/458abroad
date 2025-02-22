package com.example.abroad.controller.student;

import com.example.abroad.model.Application.Document;
import com.example.abroad.respository.DocumentRepository;
import com.example.abroad.service.page.DocumentService;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Blob;
import java.time.Instant;

@Controller
@RequestMapping("/applications/{applicationId}/documents")
public record DocumentController(DocumentService documentService, DocumentRepository documentRepository) {

    @PostMapping
    public String uploadDocument(
            @PathVariable String applicationId,
            @RequestParam("type") Document.Type type,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Convert MultipartFile to Blob
            Blob blob = new SerialBlob(file.getBytes());

            // Create new document
            Document document = new Document(
                    type,
                    Instant.now(),
                    blob,
                    applicationId
            );

            documentRepository.save(document);
            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + e.getMessage());
        }

        return "redirect:/applications";
    }
}