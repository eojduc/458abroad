package com.example.abroad.service.page.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.DocumentService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.stream.Stream;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public record ListApplicationsService(
        ApplicationService applicationService,
        ProgramService programService,
        DocumentService documentService,
        UserService userService,
        FacultyLeadRepository facultyLeadRepository
) {

  public GetApplicationsResult getApplications(HttpSession session, Sort sort, Boolean ascending) {
    // Check user authentication
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplicationsResult.UserNotFound();
    }

    // Fetch base data
    List<Application> applications = applicationService.findByStudent(user);
    List<Program> programs = programService.findAll();

    // Create sorter based on selected column
    Comparator<PairWithDocuments> sorter = switch (sort) {
      case TITLE -> Comparator.comparing(pair -> pair.prog().title());
      case YEAR_SEMESTER -> Comparator.comparing(pair ->
              pair.prog().year().toString() + pair.prog().semester().toString());
      case FACULTY -> Comparator.comparing(pair -> pair.prog().title());
      case START_DATE -> Comparator.comparing(pair -> pair.prog().startDate());
      case END_DATE -> Comparator.comparing(pair -> pair.prog().endDate());
      case APPLICATION_OPEN -> Comparator.comparing(pair -> pair.prog().applicationOpen());
      case APPLICATION_CLOSED -> Comparator.comparing(pair -> pair.prog().applicationClose());
      case STATUS -> Comparator.comparing(pair -> pair.app().status().toString());
      case PAYMENT_STATUS ->
          Comparator.comparing(pair -> {
              if (pair.prog().trackPayment() &&
                  (pair.app().status() == Application.Status.APPROVED ||
                   pair.app().status() == Application.Status.ENROLLED)) {
                  return pair.app().paymentStatus().name();
              }
              return "";
          });
      case DOCUMENT_RISK -> documentTypeComparator(ascending, Application.Document.Type.ASSUMPTION_OF_RISK);
      case DOCUMENT_CONDUCT -> documentTypeComparator(ascending, Application.Document.Type.CODE_OF_CONDUCT);
      case DOCUMENT_MEDICAL -> documentTypeComparator(ascending, Application.Document.Type.MEDICAL_HISTORY);
      case DOCUMENT_HOUSING -> documentTypeComparator(ascending, Application.Document.Type.HOUSING);
      case DOCUMENT_DEADLINE -> documentDeadlineComparator(ascending);
      case DOCUMENTS -> documentComparator(ascending); // Keep for backward compatibility
    };

    // Join and enrich data with document information
    List<PairWithDocuments> enrichedPairs = join(programs, applications)
            .map(pair -> {
              // Get document statuses if application is approved or enrolled
              var documentStatuses = documentService.getDocumentStatuses(session, pair.prog().id());

              // Create a map of document types to their status for easier lookup
              Map<String, Boolean> documentStatusMap = new HashMap<>();
              for (DocumentService.DocumentStatus status : documentStatuses) {
                documentStatusMap.put(status.type().name(), status.submitted());
              }

              // Count missing documents
              var missingCount = documentService.getMissingDocumentsCount(session, pair.prog.id());

              var facultyLeads = facultyLeadRepository.findById_ProgramId(pair.prog().id());

              // Create enriched pair with document information
              return new PairWithDocuments(
                      pair.app(),
                      pair.prog(),
                      documentStatuses,
                      missingCount,
                      facultyLeads,
                      documentStatusMap
              );
            })
            .sorted(sort == Sort.DOCUMENTS ||
                    sort == Sort.DOCUMENT_RISK ||
                    sort == Sort.DOCUMENT_CONDUCT ||
                    sort == Sort.DOCUMENT_MEDICAL ||
                    sort == Sort.DOCUMENT_HOUSING ||
                    sort == Sort.DOCUMENT_DEADLINE ? sorter : (ascending ? sorter : sorter.reversed())) // Apply sorting
            .toList();

    return new GetApplicationsResult.Success(enrichedPairs, user);
  }

  private Comparator<PairWithDocuments> documentDeadlineComparator(boolean ascending) {
    if (ascending) {
      // Ascending order: earlier deadlines first
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? 1 : -1; // Applications without doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, compare by deadline
        // Extract deadlines - if no documents, use maximum LocalDate value as default
        LocalDate deadline1 = pair1.documents != null && !pair1.documents.isEmpty()
                ? pair1.documents.get(0).deadline()
                : LocalDate.MAX;

        LocalDate deadline2 = pair2.documents != null && !pair2.documents.isEmpty()
                ? pair2.documents.get(0).deadline()
                : LocalDate.MAX;

        // Compare deadlines
        int deadlineComparison = deadline1.compareTo(deadline2);

        // If deadlines are the same, sort by title
        return deadlineComparison != 0 ? deadlineComparison : pair1.prog().title().compareTo(pair2.prog().title());
      };
    } else {
      // Descending order: later deadlines first
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? -1 : 1; // Applications with doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, compare by deadline
        // Extract deadlines - if no documents, use minimum LocalDate value as default
        LocalDate deadline1 = pair1.documents != null && !pair1.documents.isEmpty()
                ? pair1.documents.get(0).deadline()
                : LocalDate.MIN;

        LocalDate deadline2 = pair2.documents != null && !pair2.documents.isEmpty()
                ? pair2.documents.get(0).deadline()
                : LocalDate.MIN;

        // Compare deadlines (reversed for descending order)
        int deadlineComparison = deadline2.compareTo(deadline1);

        // If deadlines are the same, sort by title
        return deadlineComparison != 0 ? deadlineComparison : pair1.prog().title().compareTo(pair2.prog().title());
      };
    }
  }

  private Comparator<PairWithDocuments> documentTypeComparator(boolean ascending, Application.Document.Type docType) {
    if (ascending) {
      // Ascending order: missing documents first, then submitted
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? 1 : -1; // Applications without doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, check specific document status
        boolean pair1HasDoc = pair1.getDocumentStatus(docType.name());
        boolean pair2HasDoc = pair2.getDocumentStatus(docType.name());

        if (pair1HasDoc != pair2HasDoc) {
          return pair1HasDoc ? 1 : -1; // Missing documents come first in ascending order
        }

        // If document status is the same, sort by title
        return pair1.prog().title().compareTo(pair2.prog().title());
      };
    } else {
      // Descending order: submitted documents first, then missing
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? -1 : 1; // Applications with doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, check specific document status
        boolean pair1HasDoc = pair1.getDocumentStatus(docType.name());
        boolean pair2HasDoc = pair2.getDocumentStatus(docType.name());

        if (pair1HasDoc != pair2HasDoc) {
          return pair1HasDoc ? -1 : 1; // Submitted documents come first in descending order
        }

        // If document status is the same, sort by title
        return pair1.prog().title().compareTo(pair2.prog().title());
      };
    }
  }

  private Comparator<PairWithDocuments> documentComparator(boolean ascending) {
    // Keep existing implementation for backward compatibility
    if (ascending) {
      // Ascending order: applications without doc requirements come first,
      // then least to greatest missing docs
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? 1 : -1; // Applications without doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, compare by missing document count (ascending)
        int countComparison = Long.compare(pair1.missingDocumentsCount(), pair2.missingDocumentsCount());

        // If missing counts are the same, sort by title
        return countComparison != 0 ? countComparison : pair1.prog().title().compareTo(pair2.prog().title());
      };
    } else {
      // Descending order: applications with most missing docs come first,
      // then those without doc requirements at the bottom
      return (pair1, pair2) -> {
        // Check if applications need documents (are approved or enrolled)
        boolean pair1NeedsDocuments = needsDocuments(pair1.app());
        boolean pair2NeedsDocuments = needsDocuments(pair2.app());

        // If different needs
        if (pair1NeedsDocuments != pair2NeedsDocuments) {
          return pair1NeedsDocuments ? -1 : 1; // Applications with doc needs come first
        }

        // If neither needs documents, sort by title
        if (!pair1NeedsDocuments) {
          return pair1.prog().title().compareTo(pair2.prog().title());
        }

        // Both need documents, compare by missing document count (descending)
        int countComparison = Long.compare(pair2.missingDocumentsCount(), pair1.missingDocumentsCount());

        // If missing counts are the same, sort by title
        return countComparison != 0 ? countComparison : pair1.prog().title().compareTo(pair2.prog().title());
      };
    }
  }

  /**
   * Determines if an application needs documents based on its status
   */
  private boolean needsDocuments(Application app) {
    return app.status() == Application.Status.APPROVED || app.status() == Application.Status.ENROLLED;
  }

  public record PairWithDocuments(
          Application app,
          Program prog,
          List<DocumentService.DocumentStatus> documents,
          long missingDocumentsCount,
          List<Program.FacultyLead> facultyLeads,
          Map<String, Boolean> documentStatusMap
  ) {
    // Helper method to get document status
    public boolean getDocumentStatus(String documentType) {
      return documentStatusMap.getOrDefault(documentType, false);
    }
  }


  public enum Sort {
    TITLE,
    YEAR_SEMESTER,
    FACULTY,
    START_DATE,
    END_DATE,
    APPLICATION_OPEN,
    APPLICATION_CLOSED,
    STATUS,
    PAYMENT_STATUS,
    DOCUMENTS,               // Keep for backward compatibility
    DOCUMENT_RISK,           // Assumption of Risk document
    DOCUMENT_CONDUCT,        // Code of Conduct document
    DOCUMENT_MEDICAL,        // Medical History document
    DOCUMENT_HOUSING,        // Housing Form document
    DOCUMENT_DEADLINE        // Document deadline
  }


  public record Pair(Application app, Program prog) {
  }


  public static Stream<Pair> join(List<Program> programs, List<Application> applications) {
    return programs.stream()
            .flatMap(program -> applications.stream()
                    .filter(app -> app.programId().equals(program.id()))
                    .map(app -> new Pair(app, program)));
  }

  public sealed interface GetApplicationsResult {
    record Success(List<PairWithDocuments> data, User user) implements GetApplicationsResult {}
    record UserNotFound() implements GetApplicationsResult {}
  }
}