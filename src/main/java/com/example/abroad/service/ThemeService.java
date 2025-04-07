package com.example.abroad.service;

import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.respository.ThemeConfigRepository;
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
                case "headerTitle" -> config.setHeaderTitle(entry.getValue());
                case "logoUrl" -> config.setLogoUrl(entry.getValue());
                case "highlightColor" -> config.setHighlightColor(entry.getValue());
            }
        }

        return config;
    }
}
