package com.example.abroad.respository;

import com.example.abroad.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Application.Document, Integer> {

}
