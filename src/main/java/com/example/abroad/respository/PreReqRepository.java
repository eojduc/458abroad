package com.example.abroad.respository;

import com.example.abroad.model.Program.PreReq;
import com.example.abroad.model.Program.PreReq.ID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreReqRepository extends JpaRepository<PreReq, ID> {
  List<PreReq> findById_ProgramId(Integer programId);
}
