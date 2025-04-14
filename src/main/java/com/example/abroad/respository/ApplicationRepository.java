package com.example.abroad.respository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.abroad.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, String> {

  Optional<Application> findById_ProgramIdAndId_Student(Integer programId, String studentId);

  List<Application> findById_ProgramId(Integer programId);

  List<Application> findById_Student(String studentId);

  @Query("SELECT COUNT(a) FROM Application a WHERE a.id.programId = :programId AND a.status IN :statuses")
  long countByProgramIdAndStatuses(@Param("programId") Integer programId,
                                   @Param("statuses") List<Application.Status> statuses);

  @Query("SELECT COUNT(a) FROM Application a WHERE a.id.programId = :programId AND a.status IN :statuses AND a.paymentStatus = :paymentStatus")
  long countByProgramIdAndStatusesAndPaymentStatus(@Param("programId") Integer programId,
                                                   @Param("statuses") List<Application.Status> statuses,
                                                   @Param("paymentStatus") Application.PaymentStatus paymentStatus);
}
