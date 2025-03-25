package com.example.abroad.respository;

import com.example.abroad.model.Application.LetterOfRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LetterOfRecommendationRepository extends
  JpaRepository<LetterOfRecommendation, LetterOfRecommendation.ID> {


  List<LetterOfRecommendation> findById_ProgramIdAndId_Student(Integer programId, String studentId);

}
