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

    private TagResponse mapToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setStatus(tag.isStatus());
        return response;
    }
}
