package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;

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
    UserService userService
) {

  public GetApplicationsResult getApplications(HttpSession session, Sort sort, Boolean ascending) {
    // Check user authentication
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplicationsResult.UserNotFound();
    }

    // Fetch base data
    List<Application> applications = applicationService.findByStudentUsername(user.username());
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
    };

    // Join and enrich data with document information
    List<PairWithDocuments> enrichedPairs = join(programs, applications)
            .map(pair -> {
              // Get document statuses if application is approved or enrolled
              var documentStatuses = documentService.getDocumentStatuses(pair.app().id(), pair.prog().id());

              // Count missing documents
              var missingCount = documentService.getMissingDocumentsCount(pair.app().id());

              // Create enriched pair with document information
              return new PairWithDocuments(
                      pair.app(),
                      pair.prog(),
                      documentStatuses,
                      missingCount
              );
            })
            .sorted(ascending ? sorter : sorter.reversed()) // Apply sorting
            .toList();

    return new GetApplicationsResult.Success(enrichedPairs, user);
  }
  public record PairWithDocuments(
          Application app,
          Program prog,
          List<DocumentService.DocumentStatus> documents,
          long missingDocumentsCount
  ) {}


  public enum Sort {
    TITLE,
    YEAR_SEMESTER,
    FACULTY,
    START_DATE,
    END_DATE,
    APPLICATION_OPEN,
    APPLICATION_CLOSED,
    STATUS
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