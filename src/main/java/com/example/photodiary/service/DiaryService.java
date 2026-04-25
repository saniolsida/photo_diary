package com.example.photodiary.service;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.model.PostImage;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryPostRepository diaryPostRepository;

    // 1. 허용할 이미지 확장자 리스트 (화이트리스트 방식)
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    // 2. 허용할 MIME 타입 리스트
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Transactional(readOnly = true)
    public List<DiaryPost> getAllPosts(String tag) {
        if (tag != null && !tag.isEmpty()) {
            return diaryPostRepository.findAllByTagOrderByCreatedAtDesc(tag);
        }
        return diaryPostRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public DiaryPost getPostById(Long id) {
        // 기존 findById 대신 새로 만든 메서드 호출
        return diaryPostRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void savePost(DiaryPost post, List<MultipartFile> imageFiles) throws IOException {
        // 1. 수정 모드일 때 데이터 복사
        if (post.getId() != null) {
            diaryPostRepository.findById(post.getId()).ifPresent(existingPost -> {
                existingPost.setTitle(post.getTitle());
                existingPost.setContent(post.getContent());
                existingPost.setAuthor(post.getAuthor()); // 여기서 author가 null이면 위험!
                existingPost.setMood(post.getMood());
                existingPost.setTag(post.getTag());
                // 핵심: 새 파일이 실제로 있을 때만 기존 사진을 지운다!
                boolean hasNewFiles = imageFiles != null && imageFiles.stream().anyMatch(f -> !f.isEmpty());

                if (hasNewFiles) {
                    System.out.println("🧹 [DEBUG] 새 파일이 감지되어 기존 이미지 정리");
                    for (PostImage oldImg : existingPost.getImages()) {
                        deletePhysicalFile(oldImg.getImageUrl());
                    }
                    existingPost.getImages().clear();
                    // 새 이미지 처리 (기존 로직과 동일하게 진행하되 existingPost에 추가)
                    try {
                        processImages(imageFiles, existingPost);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // 새 파일이 없으면 existingPost의 images 리스트를 건드리지 않음 (보존됨)

                diaryPostRepository.save(existingPost);
            });
        } else {
            // 신규 등록 로직
            processImages(imageFiles, post);
            diaryPostRepository.save(post);
        }
    }

    // 이미지 처리 로직을 별도 메서드로 분리하면 깔끔합니다
    private void processImages(List<MultipartFile> imageFiles, DiaryPost targetPost) throws IOException {
        if (imageFiles == null) return;

        File rootFolder = new File(uploadDir);
        if (!rootFolder.exists()) rootFolder.mkdirs();

        for (MultipartFile file : imageFiles) {
            if (file.isEmpty()) continue;

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetPath = new File(rootFolder, fileName).toPath();

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                PostImage postImage = new PostImage();
                postImage.setImageUrl("/images/" + fileName);
                targetPost.addImage(postImage);
            }
        }
    }

    @Transactional
    public void deletePost(Long id) {
        diaryPostRepository.findById(id).ifPresent(post -> {
            // 해당 포스트에 달린 모든 이미지 파일을 물리적으로 삭제
            for (PostImage img : post.getImages()) {
                deletePhysicalFile(img.getImageUrl());
            }
        });
        // DB 데이터 삭제 (연관된 PostImage 레코드도 자동 삭제됨)
        diaryPostRepository.deleteById(id);
    }

    private void deletePhysicalFile(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/images/")) {
            String fileName = imageUrl.replace("/images/", "");
            File fileToDelete = new File("/app/uploads/" + fileName);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
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
