package com.example.abroad.respository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Note;

public interface NoteRepository extends JpaRepository<Application.Note, Integer> {


  List<Note> findByProgramIdAndStudent(Integer programId, String student);

  List<Note> findByAuthor(String author);


}
