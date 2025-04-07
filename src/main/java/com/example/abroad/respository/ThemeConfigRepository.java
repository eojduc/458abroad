package com.example.abroad.respository;

import com.example.abroad.model.ThemeConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Note;

public interface ThemeConfigRepository extends JpaRepository<ThemeConfig, Integer> {

}
