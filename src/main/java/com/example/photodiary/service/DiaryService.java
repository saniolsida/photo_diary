package com.example.photodiary.service;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.repository.DiaryPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryPostRepository diaryPostRepository;

    @Transactional(readOnly = true)
    public List<DiaryPost> getAllPosts() {
        return diaryPostRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DiaryPost getPostById(Long id) {
        return diaryPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    @Transactional
    public void savePost(DiaryPost post) {
        diaryPostRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        diaryPostRepository.deleteById(id);
    }
}
