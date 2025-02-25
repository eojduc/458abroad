package com.example.abroad.respository;

import com.example.abroad.model.Application;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, String> {

  Optional<Application> findByProgramIdAndStudent(Integer programId, String studentId);

  List<Application> findByProgramId(Integer programId);

  List<Application> findByStudent(String studentId);

}
