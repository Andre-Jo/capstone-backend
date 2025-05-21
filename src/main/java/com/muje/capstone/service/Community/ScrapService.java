package com.muje.capstone.service.Community;

import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.Community.Scrap;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Community.ScrapResponse;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.repository.Community.PostRepository;
import com.muje.capstone.repository.Community.ScrapRepository;
import com.muje.capstone.repository.User.UserRepository;
import com.muje.capstone.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    @Transactional
    public boolean toggleScrap(Long postId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId) // 변경: discussionRepository.findById 대신 postRepository.findById
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId)); // 변경: Discussion 대신 Post

        Optional<Scrap> existingScrap = scrapRepository.findByUserAndPost(user, post);

        if (existingScrap.isPresent()) {
            scrapRepository.delete(existingScrap.get());
            return false;
        } else {
            Scrap scrap = Scrap.builder()
                    .user(user)
                    .post(post)
                    .build();
            scrapRepository.save(scrap);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<ScrapResponse> getMyScraps() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        List<Scrap> scraps = scrapRepository.findByUserOrderByScrappedAtDesc(user);

        return scraps.stream()
                .map(scrap -> {
                    UserInfoResponse postWriterInfo = userService.getUserInfoByEmail(scrap.getPost().getUser().getEmail());
                    return new ScrapResponse(scrap, postWriterInfo);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시글이 현재 로그인한 유저에 의해 스크랩되었는지 확인
     * @param postId 확인할 게시글 ID (변경: discussionId 대신 postId)
     * @return true: 스크랩됨, false: 스크랩되지 않음
     */
    @Transactional(readOnly = true)
    public boolean isPostScrappedByUser(Long postId) { // 변경: discussionId 대신 postId
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId) // 변경: discussionRepository.findById 대신 postRepository.findById
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId)); // 변경: Discussion 대신 Post

        return scrapRepository.existsByUserAndPost(user, post);
    }

    @Transactional(readOnly = true)
    public Set<Long> findScrappedPostIds(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        return scrapRepository.findByUser(user).stream()
                .map(scrap -> scrap.getPost().getId())
                .collect(Collectors.toSet());
    }
}