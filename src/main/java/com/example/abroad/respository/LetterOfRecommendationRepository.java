package com.example.abroad.respository;

import com.example.abroad.model.Application.LetterOfRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LetterOfRecommendationRepository extends
  JpaRepository<LetterOfRecommendation, LetterOfRecommendation.ID> {

}
