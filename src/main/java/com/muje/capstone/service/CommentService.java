package com.muje.capstone.service;

import com.muje.capstone.domain.Comment;
import com.muje.capstone.domain.Notification;
import com.muje.capstone.domain.Post;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.AddCommentRequest;
import com.muje.capstone.dto.CommentResponse;
import com.muje.capstone.repository.CommentRepository;
import com.muje.capstone.repository.PostRepository;
import com.muje.capstone.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 유지
    public List<CommentResponse> getCommentsByUser(String userEmail) {
        List<Comment> comments = commentRepository.findByUserEmail(userEmail);
        for (Comment comment : comments) {
            comment.getReplies().size();
            if (comment.getParentComment() != null) {
                comment.getParentComment().getId();
            }
        }
        return comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    // 1) 포스트의 최상위 댓글 + 각 대댓글을 함께 가져오기
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        // 최상위 댓글만
        List<Comment> roots = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        // DTO로 변환하면서 대댓글을 채워줌
        return roots.stream()
                .map(root -> {
                    CommentResponse dto = new CommentResponse(root);
                    List<Comment> replies = commentRepository.findByParentCommentId(root.getId());
                    dto.getReplies().addAll(
                            replies.stream()
                                    .map(CommentResponse::new)
                                    .collect(Collectors.toList())
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 2) 댓글 작성 (parentCommentId가 있으면 대댓글)
    @Transactional
    public Comment addComment(Long postId, AddCommentRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        Comment parent = null;
        if (req.getParentCommentId() != null) {
            parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            parent.incrementCommentCount();
            commentRepository.save(parent);
        }

        post.incrementCommentCount();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(req.getContent())
                .parentComment(parent)
                .build();
        Comment saved = commentRepository.save(comment);

        // 알림: 게시글 작성자에게 댓글 알림 발송
        String postOwnerEmail = userRepository.findEmailById(post.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Post owner not found"));
        notificationService.createAndSend(
                postOwnerEmail,
                Notification.NotificationType.COMMENT,
                user.getNickname() + "님이 댓글을 달았습니다: " + req.getContent(),
                "/posts/" + postId
        );

        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        validateCommentOwner(comment); // 작성자 검증

        Post post = comment.getPost();

        Comment parent = comment.getParentComment();
        if (parent != null) {
            parent.decrementCommentCount();
            post.decrementCommentCount();
            commentRepository.save(parent);
            postRepository.save(post);

        } else {
            int numberOfRepliesToDelete = comment.getReplies().size();
            post.decrementCommentCount(); // 최상위 댓글 1개 감소
            for (int i = 0; i < numberOfRepliesToDelete; i++) {
                post.decrementCommentCount(); // 대댓글 수 만큼 추가 감소
            }
            postRepository.save(post);
        }
        commentRepository.delete(comment);
    }

    // 댓글을 작성한 유저인지 확인 (User 엔티티의 이메일로 비교)
    private static void validateCommentOwner(Comment comment) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (comment.getUser() == null || !comment.getUser().getEmail().equals(userName)) {
            throw new IllegalArgumentException("Not authorized to delete this comment");
        }
    }

}