package com.muje.capstone.dto.Community.Discussion;

import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.dto.Community.Comment.CommentResponse;
import com.muje.capstone.dto.User.UserInfo.SafeUserInfoResponse;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    private final Long adoptedCommentId; // 채택된 댓글의 ID만 저장 (null이면 채택 안 됨)
    private CommentResponse adoptedComment; // 채택된 댓글 정보
    private boolean scrapped;

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
        this.adoptedCommentId = discussion.getAdoptedCommentId();
    }

    public DiscussionResponse(Discussion discussion, UserInfoResponse originalUserInfo, CommentResponse adoptedComment) {
        this(discussion, originalUserInfo); // 기존 생성자 호출
        this.adoptedComment = adoptedComment; // 채택된 댓글 정보 추가
    }
}