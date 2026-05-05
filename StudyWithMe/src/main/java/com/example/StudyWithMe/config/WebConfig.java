package com.example.StudyWithMe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 테스트용이므로 모두 허용, 추후 프론트 주소만 지정 가능
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}