package com.example.abroad.respository;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FacultyLeadRepository extends JpaRepository<Program.FacultyLead, Program.FacultyLead.AppUser> {


  List<FacultyLead> findById_ProgramId(Integer programId);

  List<FacultyLead> findById_Username(String username);

}