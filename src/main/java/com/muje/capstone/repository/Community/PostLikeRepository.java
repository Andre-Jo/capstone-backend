package com.muje.capstone.repository.Community;

import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.Community.PostLike;
import com.muje.capstone.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long>{
    Optional<PostLike> findByPostAndUser(Post post, User user);
    @Query("SELECT pl.user FROM PostLike pl WHERE pl.post.id = :postId")
    List<User> findUsersByPostId(@Param("postId") Long postId);
}