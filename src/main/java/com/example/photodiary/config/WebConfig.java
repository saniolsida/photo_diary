package com.example.photodiary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /images/ 로 시작하는 주소를 요청하면
        // 컨테이너 내부의 /app/uploads/ 폴더를 바라보게 합니다.
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}