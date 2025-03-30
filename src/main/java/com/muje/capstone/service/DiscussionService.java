package com.muje.capstone.service;

import com.muje.capstone.domain.Discussion;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.AddDiscussionRequest;
import com.muje.capstone.dto.UpdateDiscussionRequest;
import com.muje.capstone.repository.DiscussionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserService userService;

    public Long save(AddDiscussionRequest request, String email) {
        // UserService를 통해 로그인된 Graduate 정보를 조회합니다.
        User user = userService.findByEmail(email);
        Discussion discussion = Discussion.builder()
                .discussionCategory(request.getDiscussionCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();
        return discussionRepository.save(discussion).getId();
    }

    public List<Discussion> findAll() {
        return discussionRepository.findAll();
    }

    public Discussion findById(Long id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found with id: " + id));
    }

    public void delete(long id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found: " + id));
        validatePostOwner(discussion);
        discussionRepository.delete(discussion);
    }

    @Transactional
    public Discussion update(long id, UpdateDiscussionRequest request) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        validatePostOwner(discussion);
        discussion.updateDiscussion(request.getTitle(), request.getContent(), request.getDiscussionCategory());

        return discussion;
    }

    // 게시글을 작성한 유저인지 확인 (Graduate 엔티티의 이메일로 비교)
    private static void validatePostOwner(Discussion Discussion) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        // Graduate 엔티티에 getEmail() 메서드가 있다고 가정합니다.
        if (Discussion.getUser() == null ||
                !Discussion.getUser().getEmail().equals(userName)) {
            throw new IllegalArgumentException("Not authorized to delete this Discussion");
        }
    }
}