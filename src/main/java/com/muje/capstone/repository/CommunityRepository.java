package com.muje.capstone.repository;

import com.muje.capstone.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Post, Long> {

}
