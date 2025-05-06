package com.muje.capstone.service;

import com.muje.capstone.domain.Discussion;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.AddDiscussionRequest;
import com.muje.capstone.dto.UpdateDiscussionRequest;
import com.muje.capstone.repository.DiscussionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserService userService;

    @Transactional // 저장 메서드에 트랜잭션 추가
    public Long save(AddDiscussionRequest request, String email) {

        User user = userService.findByEmail(email);

        Discussion discussion = Discussion.builder()
                .discussionCategory(request.getDiscussionCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return discussionRepository.save(discussion).getId();
    }

    @Transactional(readOnly = true)
    public List<Discussion> findAll() {
        return discussionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Discussion findById(Long id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found with id: " + id));
        discussion.incrementViewCount();
        return discussion;
    }

    @Transactional
    public void delete(long id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found: " + id));
        validatePostOwner(discussion);
        discussionRepository.delete(discussion);
    }

    @Transactional // 업데이트 메서드에 트랜잭션 추가
    public Discussion update(long id, UpdateDiscussionRequest request) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        validatePostOwner(discussion);
        discussion.updateDiscussion(request.getTitle(), request.getContent(), request.getDiscussionCategory());

        return discussion;
    }

    // 게시글을 작성한 유저인지 확인 (Graduate 엔티티의 이메일로 비교)
    private void validatePostOwner(Discussion discussion) { // 파라미터 이름 수정

        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (discussion.getUser() == null ||
                !discussion.getUser().getEmail().equals(authenticatedUserEmail)) {
            throw new IllegalArgumentException("Not authorized to delete/update this Discussion");
        }
    }

}