package com.example.abroad.respository;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FacultyLeadRepository extends JpaRepository<Program.FacultyLead, Program.FacultyLead.AppUser> {

  // Example of custom query methods, if needed:
  // Optional<FacultyLead> findById_ApplicationIdAndId_Username(String applicationId, String username);

  List<FacultyLead> findById_ApplicationId(String applicationId);

  List<FacultyLead> findById_Username(String username);

}