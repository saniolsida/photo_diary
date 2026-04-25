package com.example.photodiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DiaryPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author; // 작성자

    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt; //작성일


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
