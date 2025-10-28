package com.example.project.repository;

import com.example.project.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByStatus(boolean status);
    Optional<Tag> findByName(String name);
    Optional<Tag> findByIdAndStatus(Long id, boolean status);
    List<Tag> findByStatusOrderByQuestionCountDesc(boolean status);
}
