package com.example.project.service.impl;

import com.example.project.entity.req.TagRequest;
import com.example.project.entity.res.TagResponse;
import com.example.project.entity.Tag;
import com.example.project.repository.TagRepository;
import com.example.project.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<TagResponse> getAllTags() {
        return tagRepository.findByStatus(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findByIdAndStatus(id, true)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return mapToResponse(tag);
    }

    @Override
    public TagResponse createTag(TagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setStatus(true);
        tag.setCreatedAt(LocalDateTime.now());
        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findByIdAndStatus(id, true)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(request.getName());
        tag.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findByIdAndStatus(id, true)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setStatus(false);
        tag.setUpdatedAt(LocalDateTime.now());
        tagRepository.save(tag);
    }

    @Override
    public void incrementQuestionCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setQuestionCount(tag.getQuestionCount() + 1);
        tag.setUpdatedAt(LocalDateTime.now());
        tagRepository.save(tag);
    }

    @Override
    public void decrementQuestionCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        if (tag.getQuestionCount() > 0) {
            tag.setQuestionCount(tag.getQuestionCount() - 1);
        }
        tag.setUpdatedAt(LocalDateTime.now());
        tagRepository.save(tag);
    }

    @Override
    public void updateQuestionCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        // Gerçek soru sayısını hesapla
        int actualCount = tag.getQuestions() != null ? 
                (int) tag.getQuestions().stream()
                        .filter(q -> q.isStatus())
                        .count() : 0;
        
        tag.setQuestionCount(actualCount);
        tag.setUpdatedAt(LocalDateTime.now());
        tagRepository.save(tag);
    }

    @Override
    public List<TagResponse> getPopularTags() {
        return getPopularTags(10); // Default 10 tag
    }

    @Override
    public List<TagResponse> getPopularTags(int limit) {
        return tagRepository.findByStatusOrderByQuestionCountDesc(true)
                .stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TagResponse mapToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setQuestionCount(tag.getQuestionCount());
        response.setStatus(tag.isStatus());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        return response;
    }
}
