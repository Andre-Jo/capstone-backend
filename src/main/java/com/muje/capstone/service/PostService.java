package com.muje.capstone.service;

import com.muje.capstone.domain.Post;
import com.muje.capstone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<Post> getPostsByUser(String userEmail) {
        return postRepository.findByUser_Email(userEmail);
    }
}
