package com.example.project.service;

import com.example.project.entity.req.TagRequest;
import com.example.project.entity.res.TagResponse;

import java.util.List;

public interface TagService {

    List<TagResponse> getAllTags();

    TagResponse getTagById(Long id);

    TagResponse createTag(TagRequest request);

    TagResponse updateTag(Long id, TagRequest request);

    void deleteTag(Long id);
}
