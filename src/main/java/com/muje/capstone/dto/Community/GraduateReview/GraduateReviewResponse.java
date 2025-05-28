package com.muje.capstone.dto.Community.GraduateReview;

import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.User.UserInfo.SafeUserInfoResponse;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
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
    
    private final int q1;
    private final int q2;
    private final int q3;
    private final int q4;
    private final int q5;
    private final double averageScore;
    private final String colorIcon;
    
    private final SafeUserInfoResponse userInfo;  // 안전한 유저 정보 DTO

    public GraduateReviewResponse(GraduateReview review, UserInfoResponse originalUserInfo) {
        SafeUserInfoResponse.SafeUserInfoResponseBuilder builder = SafeUserInfoResponse.builder()
                .email(originalUserInfo.getEmail())
                .nickname(originalUserInfo.getNickname())
                .school(originalUserInfo.getSchool())
                .department(originalUserInfo.getDepartment())
                .studentYear(originalUserInfo.getStudentYear())
                .userType(originalUserInfo.getUserType())
                .profileImage(originalUserInfo.getProfileImage())
                .isSchoolVerified(originalUserInfo.getIsSchoolVerified());

        if (originalUserInfo.getUserType() == User.UserType.STUDENT) {
            builder.isSubscribed(originalUserInfo.getIsSubscribed());
        } else if (originalUserInfo.getUserType() == User.UserType.GRADUATE) {
            builder
                    .currentCompany(originalUserInfo.getCurrentCompany())
                    .currentSalary(originalUserInfo.getCurrentSalary())
                    .occupation(originalUserInfo.getOccupation())
                    .skills(originalUserInfo.getSkills())
                    .isCompanyVerified(originalUserInfo.getIsCompanyVerified());
        }

        SafeUserInfoResponse safeUserInfo = builder.build();

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

        this.q1 = review.getQ1();
        this.q2 = review.getQ2();
        this.q3 = review.getQ3();
        this.q4 = review.getQ4();
        this.q5 = review.getQ5();
        this.averageScore = review.getAverageScore();
        this.colorIcon = review.getColorIcon();
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
