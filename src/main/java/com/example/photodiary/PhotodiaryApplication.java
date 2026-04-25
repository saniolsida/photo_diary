package com.example.photodiary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@SpringBootApplication
public class PhotodiaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotodiaryApplication.class, args);
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // LocalDateTime 변환을 위해 필요
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
