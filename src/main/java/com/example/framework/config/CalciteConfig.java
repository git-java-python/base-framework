package com.example.framework.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.PropertyPlaceholderHelper;

@Configuration
public class CalciteConfig {

    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}", ":", true);

    @Bean
    public DataSource calciteDataSource(Environment environment) throws IOException {
        String modelContent = loadModelTemplate();
        String resolvedModel = resolvePlaceholders(modelContent, environment);
        Path tempModelPath = Files.createTempFile("calcite-model", ".json");
        Files.writeString(tempModelPath, resolvedModel, StandardCharsets.UTF_8);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.apache.calcite.jdbc.Driver");
        dataSource.setUrl("jdbc:calcite:model=" + tempModelPath.toAbsolutePath());

        Properties properties = new Properties();
        properties.setProperty("caseSensitive", "false");
        dataSource.setConnectionProperties(properties);
        return dataSource;
    }

    private String loadModelTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("calcite-model.json");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String resolvePlaceholders(String template, Environment environment) {
        return PLACEHOLDER_HELPER.replacePlaceholders(template, environment::getProperty);
    }
}
