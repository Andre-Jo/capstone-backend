package com.muje.capstone.repository.Community;

import com.muje.capstone.domain.Community.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser_Email(String email);
}