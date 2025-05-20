package com.muje.capstone.service;

import com.muje.capstone.domain.Comment;
import com.muje.capstone.domain.CommentLike;
import com.muje.capstone.domain.Notification;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.CommentLikeResponse;
import com.muje.capstone.dto.PostLikeResponse;
import com.muje.capstone.repository.CommentLikeRepository;
import com.muje.capstone.repository.CommentRepository;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 댓글 좋아요 토글
     * @return true=좋아요 추가, false=좋아요 취소
     */
    @Transactional
    public boolean toggleLike(Long commentId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);
        boolean result;
        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decrementLikeCount();
            result = false;
        } else {
            commentLikeRepository.save(new CommentLike(comment, user));
            comment.incrementLikeCount();
            result = true;
            // 알림: 댓글 작성자에게 좋아요 알림
            String commentOwnerEmail = userRepository.findEmailById(comment.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Comment owner not found"));
            notificationService.createAndSend(
                    commentOwnerEmail,
                    Notification.NotificationType.LIKE,
                    user.getNickname() + "님이 회원님의 댓글을 좋아합니다.",
                    "/posts/" + comment.getPost().getId() + "#comment-" + commentId
            );
        }
        commentRepository.save(comment);
        return result;
    }

    /** 해당 댓글에 좋아요 누른 사용자 조회 */
    @Transactional(readOnly = true)
    public List<CommentLikeResponse> getLikedUsers(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        List<User> users = commentLikeRepository.findUsersByCommentId(commentId);
        return users.stream()
                .map(u -> new CommentLikeResponse(u.getId(), u.getEmail(), u.getNickname(), u.getSchool(), u.getDepartment(), u.getUserType(), u.getProfileImage()))
                .collect(Collectors.toList());
    }
}