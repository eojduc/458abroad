package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.respository.ThemeConfigRepository;
import com.example.abroad.service.ProgramService.SaveProgram;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public record ThemeService(ThemeConfigRepository themeConfigRepository) {

    public RebrandConfig getConfig() {
        List<ThemeConfig> configs = themeConfigRepository.findAll();
        var config = new RebrandConfig();

        for (ThemeConfig entry : configs) {
            switch (entry.getKey()) {
                case "footerText" -> config.setFooterText(entry.getValue());
                case "headName" -> config.setHeadName(entry.getValue());
                case "homeTitle" -> config.setHomeTitle(entry.getValue());
                case "homeSubtitle" -> config.setHomeSubtitle(entry.getValue());
                case "homeCardContent" -> config.setHomeCardContent(entry.getValue());
                case "adminContent" -> config.setAdminContent(entry.getValue());
                case "studentContent" -> config.setStudentContent(entry.getValue());
                case "primaryColor" -> config.setPrimaryColor(entry.getValue());
                case "secondaryColor" -> config.setSecondaryColor(entry.getValue());
                case "accentColor" -> config.setAccentColor(entry.getValue());
                case "base100" -> config.setBase100(entry.getValue());
            }
        }

        return config;
    }

    public SaveThemeConfig saveThemeConfig(Map<String, String> formData) {
        try {
            List<ThemeConfig> configs = formData.entrySet().stream()
                .map(entry -> new ThemeConfig(entry.getKey(), entry.getValue()))
                .toList();

            themeConfigRepository.saveAll(configs);

            return new SaveThemeConfig.Success(getConfig());
        } catch (Exception e) {
            return new SaveThemeConfig.DatabaseError(e.getMessage());
        }
    }


    public sealed interface SaveThemeConfig {
        record Success(RebrandConfig themeConfig) implements ThemeService.SaveThemeConfig {}
        record InvalidThemeInfo(String message) implements ThemeService.SaveThemeConfig {}
        record DatabaseError(String message) implements ThemeService.SaveThemeConfig {}
    }

}
