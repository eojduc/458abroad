package com.example.abroad.respository;

import com.example.abroad.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Application.Note, Integer> {


}
