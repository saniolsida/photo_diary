package com.example.photodiary.controller;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.repository.DiaryPostRepository;
import com.example.photodiary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DiaryService diaryService; // Repository 대신 Service를 사용합니다.

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", diaryService.getAllPosts());
        return "index";
    }

    @GetMapping("/post/detail/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        model.addAttribute("post", diaryService.getPostById(id));
        return "view-detail";
    }

    @PostMapping("/post/create")
    public String createPost(DiaryPost post, @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        diaryService.savePost(post, imageFile);
        return "redirect:/";
    }

    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", diaryService.getPostById(id));
        return "edit/edit-post";
    }

    @PostMapping("/post/update")
    public String updatePost(DiaryPost post, @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        diaryService.savePost(post, imageFile);
        return "redirect:/post/detail/" + post.getId();
    }

    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        diaryService.deletePost(id);
        return "redirect:/";
    }

    @GetMapping("/post/new")
    public String newPostForm() {
        return "new/new-post";
    }
}
