package com.example.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirige la raíz y otras rutas comunes a /api/hello
        registry.addRedirectViewController("/", "/api/hello");
        registry.addRedirectViewController("/index", "/api/hello");
        registry.addRedirectViewController("/home", "/api/hello");
    }
}
