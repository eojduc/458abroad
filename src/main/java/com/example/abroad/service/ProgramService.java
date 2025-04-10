package com.example.abroad.service;


import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.QuestionRepository;

import com.example.abroad.respository.ResponseRepository;
import com.example.abroad.service.ProgramService.SaveProgram.DatabaseError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
/**
 * Service class for Program. wraps the ProgramRepository and provides additional functionality.
 */
@Service
public record ProgramService(
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  FacultyLeadRepository facultyLeadRepository,
  QuestionRepository questionRepository,
  ResponseRepository responseRepository,
  UserService userService
) {

  private static final Logger logger = LoggerFactory.getLogger(ProgramService.class);



  public SaveProgram saveProgram(Program program) {
    if (program.title().isBlank()) {
      return new SaveProgram.InvalidProgramInfo("Title cannot be blank");
    }
    if (program.title().length() > 80) {
      return new SaveProgram.InvalidProgramInfo("Title cannot be longer than 80 characters");
    }
    if (program.description().isBlank()) {
      return new SaveProgram.InvalidProgramInfo("Description cannot be blank");
    }
    if (program.description().length() > 10000) {
      return new SaveProgram.InvalidProgramInfo("Description cannot be longer than 10000 characters");
    }
    if (program.applicationOpen().isAfter(program.applicationClose())) {
      return new SaveProgram.InvalidProgramInfo("Application open is after application close");
    }
    if (program.applicationClose().isAfter(program.documentDeadline())) {
      return new SaveProgram.InvalidProgramInfo("Application close is after document deadline");
    }
    if (program.documentDeadline().isAfter(program.startDate())) {
      return new SaveProgram.InvalidProgramInfo("Document deadline is after start date");
    }
    if (program.startDate().isAfter(program.endDate())) {
      return new SaveProgram.InvalidProgramInfo("Start date is after end date");
    }
    try {
      var savedProgram = programRepository.save(program);
      return new SaveProgram.Success(savedProgram);
    } catch (Exception e) {
      return new SaveProgram.DatabaseError(e.getMessage());
    }
  }
  public SaveProgram addProgram(Program program, List<? extends User> facultyLeads, List<String> questions, List<Integer> removedQuestions) {
    // Validate faculty leads
    if (facultyLeads == null || facultyLeads.isEmpty()) {
      return new SaveProgram.InvalidProgramInfo("Program must have at least one faculty lead");
    }

    // Validate questions
    if (questions == null || questions.isEmpty()) {
      return new SaveProgram.InvalidProgramInfo("Program must have at least one question");
    }

    if (questions.stream().anyMatch(String::isBlank)) {
      return new SaveProgram.InvalidProgramInfo("Questions cannot be blank");
    }

    SaveProgram saveResult = saveProgram(program);

    return switch (saveResult) {
      case SaveProgram.Success success -> {
        Program savedProgram = success.program();
        try {
          setFacultyLeads(savedProgram, facultyLeads);
          setQuestions(savedProgram.id(), questions);
          if (!removedQuestions.isEmpty()) {
            updateResponses(savedProgram, removedQuestions);
          }
          yield success;
        } catch (Exception e) {
          try {
            deleteProgram(savedProgram);
          } catch (Exception deleteError) {
          }
          yield new DatabaseError(e.getMessage());
        }
      }
      case SaveProgram.InvalidProgramInfo invalidInfo -> invalidInfo;
      case DatabaseError databaseError -> databaseError;
    };
  }

  public Boolean isFacultyLead(Program program, User user) {
    return findFacultyLeads(program).stream().map(User::username).anyMatch(user.username()::equals);
  }


  public sealed interface SaveProgram {
    record Success(Program program) implements SaveProgram {}
    record InvalidProgramInfo(String message) implements SaveProgram {}
    record DatabaseError(String message) implements SaveProgram {}
  }

  public List<Program> findAll() {
    return programRepository.findAll();
  }


  public Optional<Program> findById(Integer id) {
    return programRepository.findById(id);
  }

  public List<? extends User> findFacultyLeads(Program program) {
    var facultyLeadUsernames = facultyLeadRepository.findById_ProgramId(program.id())
      .stream().map(FacultyLead::username).toList();

    // if the program does not have a faculty lead, include the head admin
    if (facultyLeadUsernames.isEmpty()) {
      return userService.findAll()
        .stream()
        .filter(userService::isHeadAdmin)
        .toList();
    }
    return userService.findAll()
      .stream().filter(u -> facultyLeadUsernames.contains(u.username())).toList();
  }

  public List<Program> findProgramsWithoutFaculty() {
    var facultyLeadProgramIds = facultyLeadRepository.findAll()
      .stream()
      .map(FacultyLead::programId)
      .toList();
    return programRepository.findAll()
      .stream()
      .filter(program -> !facultyLeadProgramIds.contains(program.id()))
      .toList();
  }

  public List<Program> findFacultyPrograms(User user) {
    List<Integer> facultyLeadProgramIds = new ArrayList<>(
        facultyLeadRepository.findById_Username(user.username())
            .stream()
            .map(FacultyLead::programId)
            .toList());

    // If the user is the head admin, also include programs without a faculty lead
    if (userService.isHeadAdmin(user)) {
      List<Program> programsWithoutFaculty = findProgramsWithoutFaculty();
      facultyLeadProgramIds.addAll(
          programsWithoutFaculty.stream()
              .map(Program::id)
              .toList()
      );
    }
    return programRepository.findAllById(facultyLeadProgramIds);
  }

  public List<? extends User> findAllFacultyLeads() {
    var facultyLeadUsernames = new ArrayList<>(facultyLeadRepository.findAll()
      .stream()
      .map(FacultyLead::username)
      .toList());

    // If there are programs without faculty leads, include the head admin
    List<Program> programsWithoutFaculty = findProgramsWithoutFaculty();
    if (!programsWithoutFaculty.isEmpty()) {
      facultyLeadUsernames.add("admin");
    }

    return userService.findAll()
      .stream()
      .filter(u -> facultyLeadUsernames.contains(u.username()))
      .toList();
  }

  public void deleteProgram(Program program) {
    programRepository.delete(program);
  }

  public void removeFacultyLead(Program program, User user) {
    var facultyLeads = facultyLeadRepository.findById_ProgramId(program.id())
      .stream()
      .filter(lead -> !lead.username().equals(user.username()))
      .flatMap(lead -> userService.findByUsername(lead.username()).stream())
      .toList();
    setFacultyLeads(program, facultyLeads);

  }

  public Optional<Question> findQuestion(Program program, Integer id) {
    return questionRepository.findById_ProgramIdAndId_id(program.id(), id);
  }

  public void setFacultyLeads(Program program, List<? extends User> users) {
    try {
      // Find and delete existing faculty leads for this program
      var facultyLeads = facultyLeadRepository.findById_ProgramId(program.id());
      facultyLeadRepository.deleteAll(facultyLeads);

      // Get admin usernames
      var adminUsernames = userService.findAll()
          .stream()
          .filter(userService::isAdmin)
          .map(User::username)
          .toList();

      // Create new faculty leads
      var newFacultyLeads = users.stream()
          .map(User::username)
          .filter(adminUsernames::contains)
          .map(username -> new FacultyLead(program.id(), username))
          .toList();

      // If no admin faculty leads, add default admin
      var facultyLeadsToAdd = newFacultyLeads.isEmpty() ?
          List.of(new FacultyLead(program.id(), "admin"))
          : newFacultyLeads;

      // Save new faculty leads
      facultyLeadRepository.saveAll(facultyLeadsToAdd);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set faculty leads: " + e.getMessage());
    }
  }



  public void updateResponses(Program program, List<Integer> removedQuestions) {
    try {
      List<Response> programResponses = responseRepository.findById_ProgramId(program.id());

      // Remove responses to removed questions
      List<Response> responsesToRemove = programResponses.stream()
          .filter(response -> removedQuestions.contains(response.question()))
          .toList();

      responseRepository.deleteAll(responsesToRemove);
    } catch (Exception e) {
      throw new RuntimeException("Failed to remove responses: " + e.getMessage());
    }

    try {
      List<Response> programResponses = responseRepository.findById_ProgramId(program.id());

      List<Integer> sortedRemovedQuestions = new ArrayList<>(removedQuestions);
      Collections.sort(sortedRemovedQuestions);

      List<Response> updatedResponses = new ArrayList<>();

      for (Response response : programResponses) {
        int oldQuestionId = response.question();

        // Count how many removed questions have a smaller ID
        long shiftAmount = sortedRemovedQuestions.stream()
            .filter(removedId -> removedId < oldQuestionId)
            .count();

        int newQuestionId = oldQuestionId - (int) shiftAmount;

        if (newQuestionId != oldQuestionId) {
          // Create a new Response object with the updated ID
          Response newResponse = new Response(response.programId(), response.student(), newQuestionId, response.response());
          updatedResponses.add(newResponse);
        } else {
          updatedResponses.add(response);
        }
      }

      // Remove the old responses and insert the updated ones
      responseRepository.deleteAll(programResponses);
      responseRepository.saveAll(updatedResponses);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update responses: " + e.getMessage());
    }
  }


  public void setQuestions(Integer programId, List<String> questions) {
    try {
      // Find and delete existing questions for this program
      var existingQuestions = questionRepository.findById_ProgramId(programId);
      questionRepository.deleteAll(existingQuestions);

      List<Question> newQuestions = IntStream.range(0, questions.size())
          .mapToObj(i -> new Question(i, questions.get(i), programId))
          .toList();

      // Save new questions
      questionRepository.saveAll(newQuestions);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set questions: " + e.getMessage());
    }
  }

  public List<Question> getQuestions(Program program) {
    return questionRepository.findById_ProgramId(program.id())
        .stream()
        .sorted(Comparator.comparing(Question::id))
        .toList();
  }

  public List<Application> getApplications(Program program) {
    return applicationRepository.findById_ProgramId(program.id());
  }
}
