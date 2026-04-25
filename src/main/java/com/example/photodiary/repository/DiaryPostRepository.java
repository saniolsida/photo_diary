package com.example.photodiary.repository;

import com.example.photodiary.model.DiaryPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryPostRepository extends JpaRepository<DiaryPost, Long> {
    @Query("SELECT p FROM DiaryPost p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<DiaryPost> findByIdWithImages(@Param("id") Long id);

    List<DiaryPost> findAllByTagOrderByCreatedAtDesc(String tag);
    List<DiaryPost> findAllByOrderByCreatedAtDesc();
}
