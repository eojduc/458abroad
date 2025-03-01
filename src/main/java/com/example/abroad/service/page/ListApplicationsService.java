package com.example.abroad.service.page;

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

import java.util.Comparator;
import java.util.List;

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
      case DOCUMENTS -> documentComparator(ascending);
    };

    // Join and enrich data with document information
    List<PairWithDocuments> enrichedPairs = join(programs, applications)
            .map(pair -> {
              // Get document statuses if application is approved or enrolled
              var documentStatuses = documentService.getDocumentStatuses(pair.app().id(), pair.prog().id());

              // Count missing documents
              var missingCount = documentService.getMissingDocumentsCount(pair.app().id());

              var facultyLeads = facultyLeadRepository.findById_ProgramId(pair.prog().id());

              // Create enriched pair with document information
              return new PairWithDocuments(
                      pair.app(),
                      pair.prog(),
                      documentStatuses,
                      missingCount,
                      facultyLeads
              );
            })
            .sorted(sort == Sort.DOCUMENTS ? sorter : (ascending ? sorter : sorter.reversed())) // Apply sorting
            .toList();

    return new GetApplicationsResult.Success(enrichedPairs, user);
  }


  private Comparator<PairWithDocuments> documentComparator(boolean ascending) {
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
          List<Program.FacultyLead> facultyLeads
  ) {}


  public enum Sort {
    TITLE,
    YEAR_SEMESTER,
    FACULTY,
    START_DATE,
    END_DATE,
    APPLICATION_OPEN,
    APPLICATION_CLOSED,
    STATUS,
    DOCUMENTS
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