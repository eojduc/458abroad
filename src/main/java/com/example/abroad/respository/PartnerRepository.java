package com.example.abroad.respository;

import com.example.abroad.model.Program.Partner;
import com.example.abroad.model.Program.Partner.ID;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PartnerRepository extends JpaRepository<Partner, ID> {

  List<Partner> findById_ProgramId(Integer programId);

  List<Partner> findById_Username(String username);
}