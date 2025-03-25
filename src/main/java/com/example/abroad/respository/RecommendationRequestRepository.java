package com.example.abroad.respository;

import com.example.abroad.model.Application.RecommendationRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRequestRepository extends JpaRepository<RecommendationRequest, RecommendationRequest.ID> {
  List<RecommendationRequest> findById_ProgramIdAndId_Student(Integer programId, String studentId);
}
