package com.muje.capstone.repository;

import com.muje.capstone.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    // 최상위 댓글만 조회
    List<Comment> findByPostIdAndParentCommentIsNull(Long postId);
    // 특정 댓글의 대댓글만 조회
    List<Comment> findByParentCommentId(Long parentCommentId);
    List<Comment> findByUserEmail(String email);
}