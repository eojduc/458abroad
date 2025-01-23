package com.example.abroad.respository;

import com.example.abroad.model.Program;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

}
