package com.example.abroad.respository;

import com.example.abroad.model.Application;
import java.util.List;
import java.util.Optional;
  import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, String> {

  Integer countByProgramId(Integer programId);

  Optional<Application> findByProgramIdAndStudent(Integer programId, String studentId);

  List<Application> findByProgramId(Integer programId);

}
