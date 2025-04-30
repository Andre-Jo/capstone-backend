package com.muje.capstone.dto;

import com.muje.capstone.domain.GraduateReview;
import lombok.Getter;

@Getter
public class GraduateReviewResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final int viewCount;
    private final int likeCount;
    private final String createdAt;
    private final String updatedAt;
    private final SafeUserInfoResponse userInfo;  // 안전한 유저 정보 DTO

    public GraduateReviewResponse(GraduateReview review, UserInfoResponse originalUserInfo) {
        // 원본 UserInfoResponse에서 필요한 정보만 SafeUserInfoResponse로 옮깁니다.
        SafeUserInfoResponse safeUserInfo = new SafeUserInfoResponse(
                originalUserInfo.getEmail(),
                originalUserInfo.getNickname(),
                originalUserInfo.getSchool(),
                originalUserInfo.getDepartment(),
                originalUserInfo.getStudentYear(),
                originalUserInfo.getUserType(),
                originalUserInfo.getProfileImage(),
                originalUserInfo.getIsSchoolVerified(),
                null,
                originalUserInfo.getCurrentCompany(),
                originalUserInfo.getCurrentSalary(),
                originalUserInfo.getSkills(),
                originalUserInfo.getIsCompanyVerified()
        );

        // 만약 익명 처리가 필요하면 민감 정보 일부를 마스킹합니다.
        if (review.getIsAnonymous() != null && review.getIsAnonymous()) {
            safeUserInfo.setEmail(maskEmail(safeUserInfo.getEmail()));
            // 필요에 따라 다른 필드들도 마스킹 처리할 수 있습니다.
        }

        this.userInfo = safeUserInfo;
        this.id = review.getId();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.viewCount = review.getViewCount();
        this.likeCount = review.getLikeCount();
        this.createdAt = review.getCreatedAt().toString();
        this.updatedAt = review.getUpdatedAt().toString();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            String regex = "(.).(?=[^@]*?@)";
            return email.replaceAll(regex, "$1*");
        }
        return email;
    }
}
