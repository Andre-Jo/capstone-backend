package com.muje.capstone.repository;

import com.muje.capstone.domain.Comment;
import com.muje.capstone.domain.CommentLike;
import com.muje.capstone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    @Query("select cl.user from CommentLike cl where cl.comment.id = :commentId")
    List<User> findUsersByCommentId(@Param("commentId") Long commentId);
}