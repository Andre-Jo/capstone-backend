package com.muje.capstone.service;

import com.muje.capstone.domain.Comment;
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

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    public Comment addComment(Long postId, AddCommentRequest addCommentRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(addCommentRequest.getContent())
                .build();
        post.incrementCommentCount();
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        validateCommentOwner(comment);
        Post post = comment.getPost();
        post.decrementCommentCount();
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