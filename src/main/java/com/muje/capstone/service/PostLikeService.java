package com.muje.capstone.service;

import com.muje.capstone.domain.Notification;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 게시글 좋아요 토글
     * @return true=좋아요 추가, false=좋아요 취소
     */
    @Transactional
    public boolean toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        boolean result;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decrementLikeCount();
            result = false;
        } else {
            postLikeRepository.save(new PostLike(post, user));
            post.incrementLikeCount();
            result = true;
            // 알림: 게시글 작성자에게 좋아요 알림
            String postOwnerEmail = userRepository.findEmailById(post.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Post owner not found"));
            notificationService.createAndSend(
                    postOwnerEmail,
                    Notification.NotificationType.LIKE,
                    user.getNickname() + "님이 회원님의 게시글을 좋아합니다.",
                    "/posts/" + postId
            );
        }
        postRepository.save(post);
        return result;
    }

    @Transactional(readOnly = true)
    public List<PostLikeResponse> getLikedUsers(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        List<User> users = postLikeRepository.findUsersByPostId(postId);
        return users.stream()
                .map(u -> new PostLikeResponse(u.getId(), u.getEmail(), u.getNickname(), u.getSchool(), u.getDepartment(), u.getUserType(), u.getProfileImage()))
                .collect(Collectors.toList());
    }
}