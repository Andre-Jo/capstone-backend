package com.muje.capstone.service.Community;

import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.domain.User.Graduate;
import com.muje.capstone.dto.Community.GraduateReview.AddGraduateReviewRequest;
import com.muje.capstone.dto.Community.GraduateReview.UpdateGraduateReviewRequest;
import com.muje.capstone.repository.Community.GraduateReviewRepository;
import com.muje.capstone.service.User.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GraduateReviewService {

    private final GraduateReviewRepository graduateReviewRepository;
    private final UserService userService; // 유저(Graduate) 정보를 가져오기 위한 서비스

    @Transactional // 저장 메서드에 트랜잭션 추가
    public Long save(AddGraduateReviewRequest request, String email) {
        Graduate graduateUser = userService.getGraduateByEmail(email); // Graduate 타입 User 객체 가져오기

        GraduateReview review = GraduateReview.builder()
                .isAnonymous(request.getIsAnonymous())
                .title(request.getTitle())
                .content(request.getContent())
                .user(graduateUser)
                .build();

        return graduateReviewRepository.save(review).getId();
    }

    @Transactional(readOnly = true)
    public List<GraduateReview> findAll() {
        return graduateReviewRepository.findAll();
    }

    @Transactional
    public GraduateReview findById(Long id) {
        GraduateReview graduateReview = graduateReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("GraduateReview not found with id: " + id));
        graduateReview.incrementViewCount();
        return graduateReview;
    }

    @Transactional
    public void delete(long id) {
        GraduateReview graduateReview = graduateReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
        validateReviewOwner(graduateReview);
        graduateReviewRepository.delete(graduateReview);
    }

    @Transactional
    public GraduateReview update(long id, UpdateGraduateReviewRequest request) {
        GraduateReview graduateReview = graduateReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        validateReviewOwner(graduateReview);
        graduateReview.updateReview(request.getTitle(), request.getContent(), request.getIsAnonymous());

        return graduateReview;
    }

    private void validateReviewOwner(GraduateReview graduateReview) {
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (graduateReview.getUser() == null ||
                !graduateReview.getUser().getEmail().equals(authenticatedUserEmail)) {
            throw new IllegalArgumentException("Not authorized to delete/update this review");
        }
    }
}