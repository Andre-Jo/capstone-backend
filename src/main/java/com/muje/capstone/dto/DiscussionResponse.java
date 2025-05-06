package com.muje.capstone.dto;

import com.muje.capstone.domain.Discussion;
import lombok.Getter;

@Getter
public class DiscussionResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final int viewCount;
    private final int likeCount;
    private final int commentCount;
    private final String createdAt;
    private final String updatedAt;
    private final Discussion.DiscussionCategory discussionCategory;
    private final SafeUserInfoResponse userInfo;  // 안전한 유저 정보 DTO

    public DiscussionResponse(Discussion discussion, UserInfoResponse originalUserInfo) {
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
                null,
                null,
                null,
                null,
                null
        );

        this.userInfo = safeUserInfo;
        this.id = discussion.getId();
        this.title = discussion.getTitle();
        this.content = discussion.getContent();
        this.viewCount = discussion.getViewCount();
        this.likeCount = discussion.getLikeCount();
        this.commentCount = discussion.getCommentCount();
        this.createdAt = discussion.getCreatedAt().toString();
        this.updatedAt = discussion.getUpdatedAt().toString();
        this.discussionCategory = discussion.getDiscussionCategory();
    }

}