package com.muje.capstone.service.Community;

import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Community.Discussion.AddDiscussionRequest;
import com.muje.capstone.dto.Community.Discussion.UpdateDiscussionRequest;
import com.muje.capstone.repository.Community.DiscussionRepository;
import com.muje.capstone.service.User.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserService userService;
    private final ScrapService scrapService;    // ScrapService 주입

    @Transactional
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

    @Transactional
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

    @Transactional
    public Discussion update(long id, UpdateDiscussionRequest request) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found: " + id));
        validatePostOwner(discussion);
        discussion.updateDiscussion(request.getTitle(), request.getContent(), request.getDiscussionCategory());
        return discussion;
    }

    private void validatePostOwner(Discussion discussion) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (discussion.getUser() == null || !discussion.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Not authorized to modify this Discussion");
        }
    }
}