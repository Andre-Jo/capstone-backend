package com.muje.capstone.repository.Community;

import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.Community.Scrap;
import com.muje.capstone.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<Scrap> findByUserAndPost(User user, Post post);
    List<Scrap> findByUserOrderByScrappedAtDesc(User user);
    List<Scrap> findByUser(User user);
}