package com.example.abroad.respository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Program.Question;

public interface QuestionRepository extends JpaRepository<Question, Question.ID> {
  
    List<Question> findById_ProgramId(Integer programId);

    Optional<Question> findById_ProgramIdAndId_id(Integer programId, Integer id);
}