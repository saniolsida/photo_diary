package com.example.photodiary.repository;

import com.example.photodiary.model.DiaryPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryPostRepository extends JpaRepository<DiaryPost, Long> {

}
