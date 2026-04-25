package com.example.photodiary.controller;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.repository.DiaryPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DiaryPostRepository diaryPostRepository;

    @GetMapping("/")
    public String index(Model model) {
        // 모든 게시글 조회
        List<DiaryPost> posts = diaryPostRepository.findAll();

        // Mustache 템플릿에 데이터 전달
        model.addAttribute("posts", posts);

        return "index";
    }

    // 게시글 상세 조회
    @GetMapping("/post/detail/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        DiaryPost post = diaryPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        model.addAttribute("post", post);
        return "view-detail"; // detail-post.mustache 파일을 찾음
    }
    @GetMapping("/post/new")
    public String newPostForm() {
        return "new/new-post"; // new-post.mustache 파일을 찾음
    }

    @PostMapping("/post/create")
    public String createPost(DiaryPost post) {
        // 폼에서 전달된 데이터가 DiaryPost 객체로 매핑됨
        diaryPostRepository.save(post);
        return "redirect:/"; // 저장 후 메인 페이지로 이동
    }

    // 1. 수정 페이지로 이동
    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        DiaryPost post = diaryPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        model.addAttribute("post", post);
        return "edit/edit-post"; // edit-post.mustache 파일을 찾음
    }

    // 2. 수정 내용 반영 (JPA의 더티 체킹 또는 save 메서드 활용)
    @PostMapping("/post/update")
    public String updatePost(DiaryPost post) {
        // save()는 ID가 이미 존재하면 해당 데이터를 찾아 업데이트(Update)를 수행합니다.
        diaryPostRepository.save(post);
        return "redirect:/";
    }

    // 3. 삭제 기능
    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        diaryPostRepository.deleteById(id);
        return "redirect:/";
    }
}
