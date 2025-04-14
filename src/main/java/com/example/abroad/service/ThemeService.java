package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.model.User.Theme;
import com.example.abroad.respository.ThemeConfigRepository;
import com.example.abroad.service.ProgramService.SaveProgram;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

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
                case "base100" -> config.setBase100(entry.getValue());
                case "base200" -> config.setBase200(entry.getValue());
                case "base300" -> config.setBase300(entry.getValue());
                case "neutralColor" -> config.setNeutralColor(entry.getValue());
                case "logoSvg" -> config.setLogoSvg(entry.getSvgData());
            }
        }

        return config;
    }

    public SaveThemeConfig saveThemeConfig(Map<String, String> formData, MultipartFile logoSvg) {
        try {
            List<ThemeConfig> configs = new java.util.ArrayList<>(formData.entrySet().stream()
                .map(entry -> new ThemeConfig(entry.getKey(), entry.getValue()))
                .toList());

            // Handle the logoSvg file
            if (logoSvg != null && !logoSvg.isEmpty()) {
                String logoSvgContent = new String(logoSvg.getBytes());
                ThemeConfig logoConfig = new ThemeConfig("logoSvg", "logo");
                logoConfig.setSvgData(logoSvgContent);
                configs.add(logoConfig);
            }

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
