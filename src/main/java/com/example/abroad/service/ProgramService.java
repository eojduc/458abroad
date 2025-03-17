package com.example.abroad.service;


import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.ProgramRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
/**
 * Service class for Program. wraps the ProgramRepository and provides additional functionality.
 */
@Service
public record ProgramService(
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  FacultyLeadRepository facultyLeadRepository,
  UserService userService
) {


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
    return userService.findAll()
      .stream().filter(u -> facultyLeadUsernames.contains(u.username())).toList();
  }

  public List<Program> findFacultyPrograms(User user) {
    var facultyLeadProgramIds = facultyLeadRepository.findById_Username(user.username())
      .stream()
      .map(FacultyLead::programId)
      .toList();
    return programRepository.findAllById(facultyLeadProgramIds);
  }

  public List<? extends User> findAllFacultyLeads() {
    var facultyLeadUsernames = facultyLeadRepository.findAll()
      .stream()
      .map(FacultyLead::username)
      .toList();
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

  public void setFacultyLeads(Program program, List<? extends User> users) {
    var facultyLeads = facultyLeadRepository.findById_ProgramId(program.id());
    facultyLeadRepository.deleteAll(facultyLeads);
    var adminUsernames = userService.findAll()
      .stream()
      .filter(userService::isAdmin)
      .map(User::username)
      .toList();
    var newFacultyLeads = users.stream()
      .map(User::username)
      .filter(adminUsernames::contains)
      .map(username -> new FacultyLead(program.id(), username))
      .toList();
    var facultyLeadsToAdd = newFacultyLeads.isEmpty() ?
      List.of(new FacultyLead(program.id(), "admin"))
      : newFacultyLeads;
    facultyLeadRepository.saveAll(facultyLeadsToAdd);
  }

}
