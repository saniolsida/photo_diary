package com.example.photodiary.service;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.repository.DiaryPostRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryPostRepository diaryPostRepository;

    // 1. 허용할 이미지 확장자 리스트 (화이트리스트 방식)
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    // 2. 허용할 MIME 타입 리스트
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Transactional(readOnly = true)
    public List<DiaryPost> getAllPosts() {
        return diaryPostRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DiaryPost getPostById(Long id) {
        return diaryPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void savePost(DiaryPost post, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {

            validateImage(imageFile);

            // [추가] 수정 시 기존 이미지가 있다면 먼저 삭제
            if (post.getId() != null) {
                diaryPostRepository.findById(post.getId()).ifPresent(oldPost -> {
                    deletePhysicalFile(oldPost.getImageUrl());
                });
            }
            // 1. 절대 경로를 수동으로 강제 조립 (가장 안전한 방법)
            // uploadDir이 "/app/uploads/" 라면, 시스템 루트(/)부터 시작하도록 보장
            File rootFolder = new File("/app/uploads");

            if (!rootFolder.exists()) {
                rootFolder.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            // 2. 부모 폴더와 파일명을 결합하여 절대 경로 생성
            File targetFile = new File(rootFolder, fileName);
            Path targetPath = targetFile.toPath();

            // 3. Files.copy 사용 (이미지 스트림 직접 쓰기)
            try (InputStream is = imageFile.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            post.setImageUrl("/images/" + fileName);
        }
        diaryPostRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        // 1. 삭제 전 기존 포스트 정보를 가져와 이미지 경로 파악
        diaryPostRepository.findById(id).ifPresent(post -> {
            // 2. 실제 파일 삭제
            deletePhysicalFile(post.getImageUrl());
        });
        // 3. DB 데이터 삭제
        diaryPostRepository.deleteById(id);
    }

    // 실제 물리적 파일을 삭제하는 유틸리티 메서드
    private void deletePhysicalFile(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/images/")) {
            String fileName = imageUrl.replace("/images/", "");
            File fileToDelete = new File("/app/uploads/" + fileName);
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    System.out.println("🗑️ 파일 삭제 성공: " + fileName);
                } else {
                    System.err.println("⚠️ 파일 삭제 실패: " + fileName);
                }
            }
        }
    }

    private void validateImage(MultipartFile file) {
        // A. 파일명 및 확장자 검사
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다. (허용: jpg, png, gif)");
        }

        // B. MIME 타입 검사 (내부 바이트 스트림 확인)
        String contentType = file.getContentType(); // 클라이언트가 보낸 정보

        // 이 contentType은 사용자가 조작할 수 있으므로,
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("이미지 파일 형식(MIME)이 올바르지 않습니다.");
        }
    }
}
