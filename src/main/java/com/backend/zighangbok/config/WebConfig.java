package com.backend.zighangbok.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://zighangbok-frontend.vercel.app", "http://13.125.57.51")  // 프론트 주소
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
