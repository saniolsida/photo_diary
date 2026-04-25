package com.example.photodiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    private String mood; // 기분 이모티콘 저장

    private String tag;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    // 편의 메서드
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public String getFirstImageUrl() {
        if (this.images != null && !this.images.isEmpty()) {
            return this.images.get(0).getImageUrl();
        }
        return null; // 사진이 없을 경우 null 반환
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt; //작성일


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
