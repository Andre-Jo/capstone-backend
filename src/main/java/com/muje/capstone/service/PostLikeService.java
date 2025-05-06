package com.muje.capstone.service;

import com.muje.capstone.domain.Post;
import com.muje.capstone.domain.PostLike;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.PostLikeResponse;
import com.muje.capstone.repository.PostLikeRepository;
import com.muje.capstone.repository.PostRepository;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decrementLikeCount();
            return false; // 좋아요 삭제됨
        } else {
            postLikeRepository.save(new PostLike(post, user));
            post.incrementLikeCount();
            return true; // 좋아요 추가됨
        }
    }

    @Transactional(readOnly = true)
    public List<PostLikeResponse> getLikedUsers(Long postId) {
        List<User> users = postLikeRepository.findUsersByPostId(postId);
        return users.stream()
                .map(user -> new PostLikeResponse(user.getId(), user.getEmail(), user.getNickname(), user.getSchool(), user.getDepartment(), user.getUserType(), user.getProfileImage()))
                .toList();
    }
}
