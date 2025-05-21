package com.muje.capstone.service.Community;

import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.repository.Community.PostRepository;
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
