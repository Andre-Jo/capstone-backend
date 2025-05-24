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
    Graduate graduateUser = userService.getGraduateByEmail(email);

    GraduateReview review = GraduateReview.builder()
            .user(graduateUser)
            .isAnonymous(request.getIsAnonymous())
            .title(request.getTitle())
            .content(request.getContent())
            .q1(request.getQ1())
            .q2(request.getQ2())
            .q3(request.getQ3())
            .q4(request.getQ4())
            .q5(request.getQ5())
            .averageScore(request.getAverageScore())
            .colorIcon(request.getColorIcon())
            .build();

    return graduateReviewRepository.save(review).getId();
}

    @Transactional(readOnly = true)
    public List<GraduateReview> findAll() {
        return graduateReviewRepository.findAll();
    }

    @Transactional(readOnly = true)
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

        graduateReview.updateReview(
            request.getTitle(),
            request.getContent(),
            request.getIsAnonymous(),
            request.getQ1(),
            request.getQ2(),
            request.getQ3(),
            request.getQ4(),
            request.getQ5(),
            request.getAverageScore(),
            request.getColorIcon()
        );

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