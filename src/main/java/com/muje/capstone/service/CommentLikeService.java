package com.muje.capstone.service;

import com.muje.capstone.domain.Comment;
import com.muje.capstone.domain.CommentLike;
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

    /**
     * 댓글 좋아요 토글
     * @return true=좋아요 추가, false=좋아요 취소
     */
    @Transactional
    public boolean toggleLike(Long postId, Long commentId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decrementLikeCount();
            return false;
        } else {
            commentLikeRepository.save(new CommentLike(comment, user));
            comment.incrementLikeCount();
            return true;
        }
    }

    /** 해당 댓글에 좋아요 누른 사용자 조회 */
    @Transactional(readOnly = true)
    public List<CommentLikeResponse> getLikedUsers(Long commentId) {
        // (postId는 검증용으로, 실제로는 commentId만으로도 충분)
        commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        List<User> users = commentLikeRepository.findUsersByCommentId(commentId);
        return users.stream()
                .map(user -> new CommentLikeResponse(user.getId(), user.getEmail(), user.getNickname(), user.getSchool(), user.getDepartment(), user.getUserType(), user.getProfileImage()))
                .toList();
    }
}
