package com.example.abroad.respository;

import com.example.abroad.model.Application;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, String> {

  Integer countByProgramId(Integer programId);

  Optional<Application> findByProgramIdAndStudent(Integer programId, String studentId);

  List<Application> findByProgramId(Integer programId);

  // This method will return a list of Optionals, where each Optional corresponds to a student's application for a program
  @Query("SELECT a FROM Application a WHERE a.student = :studentId")
  List<Optional<Application>> findProgramApplicationsByStudent(String studentId);

  @Modifying
  @Transactional
  @Query("update Application a set a.status = ?2 where a.id = ?1")
  void updateStatus(String applicationId, Application.Status status);

}
