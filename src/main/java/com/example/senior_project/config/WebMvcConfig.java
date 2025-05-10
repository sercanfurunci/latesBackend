package com.example.senior_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Profil fotoğrafları için resource handler
        registry.addResourceHandler("/profiles/**")
                .addResourceLocations("file:src/profiles/");

        // Ürün fotoğrafları için resource handler
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}