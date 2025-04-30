package com.muje.capstone.service;

import com.muje.capstone.domain.GraduateReview;
import com.muje.capstone.domain.Graduate;
import com.muje.capstone.dto.AddGraduateReviewRequest;
import com.muje.capstone.dto.UpdateGraduateReviewRequest;
import com.muje.capstone.repository.GraduateReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GraduateReviewService {

    private final GraduateReviewRepository graduateReviewRepository;
    private final UserService userService; // 유저(Graduate) 정보를 가져오기 위한 서비스

    public Long save(AddGraduateReviewRequest request, String email) {
        // UserService를 통해 로그인된 Graduate 정보를 조회합니다.
        Graduate graduate = userService.getGraduateByEmail(email);
        GraduateReview review = GraduateReview.builder()
                .isAnonymous(request.getIsAnonymous())
                .title(request.getTitle())
                .content(request.getContent())
                .graduate(graduate)
                .build();
        return graduateReviewRepository.save(review).getId();
    }

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

    // 게시글을 작성한 유저인지 확인 (Graduate 엔티티의 이메일로 비교)
    private static void validateReviewOwner(GraduateReview graduateReview) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        // Graduate 엔티티에 getEmail() 메서드가 있다고 가정합니다.
        if (graduateReview.getGraduate() == null ||
                !graduateReview.getGraduate().getEmail().equals(userName)) {
            throw new IllegalArgumentException("Not authorized to delete this review");
        }
    }
}