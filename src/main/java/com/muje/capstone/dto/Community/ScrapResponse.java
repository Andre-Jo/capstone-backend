package com.muje.capstone.dto.Community;

import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.Community.Scrap;
import com.muje.capstone.dto.User.UserInfo.SafeUserInfoResponse;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

@Getter
@NoArgsConstructor
public class ScrapResponse {
    private Long scrapId;
    private Long postId;
    private String postTitle;
    private String postType; // 게시글의 실제 타입 (예: "DISCUSSION", "GRADUATE_REVIEW")
    private String postCategory; // Post의 하위 타입에 따른 카테고리 (예: Discussion의 "QUESTION_TO_STUDENT")
    private String scrappedAt;
    private SafeUserInfoResponse postWriterInfo;

    public ScrapResponse(Scrap scrap, UserInfoResponse originalPostWriterInfo) {
        this.scrapId = scrap.getId();
        this.postId = scrap.getPost().getId();
        this.postTitle = scrap.getPost().getTitle();
        this.scrappedAt = scrap.getScrappedAt().toString(); // LocalDateTime을 String으로 변환

        Post post = scrap.getPost();

        // 1) 실제 엔티티 클래스를 확인
        Class<?> realClass = Hibernate.getClass(post);

        // 2) 프록시 언래핑
        Object unproxied = post;
        if (post instanceof HibernateProxy) {
            unproxied = ((HibernateProxy) post)
                    .getHibernateLazyInitializer()
                    .getImplementation();
        }

        // 3) 분기 및 캐스트
        if (GraduateReview.class.equals(realClass)) {
            this.postType     = "GRADUATE_REVIEW";
            this.postCategory = null;
        }
        else if (Discussion.class.equals(realClass)) {
            this.postType     = "DISCUSSION";
            this.postCategory = ((Discussion) unproxied)
                    .getDiscussionCategory()
                    .name();
        }
        else {
            this.postType     = "UNKNOWN";
            this.postCategory = null;
        }


        // 게시글 작성자 정보 설정
        SafeUserInfoResponse safePostWriterInfo = new SafeUserInfoResponse(
                originalPostWriterInfo.getEmail(),
                originalPostWriterInfo.getNickname(),
                originalPostWriterInfo.getSchool(),
                originalPostWriterInfo.getDepartment(),
                originalPostWriterInfo.getStudentYear(),
                originalPostWriterInfo.getUserType(),
                originalPostWriterInfo.getProfileImage(),
                originalPostWriterInfo.getIsSchoolVerified(),
                null, null, null, null, null, null // 민감 정보는 null
        );
        this.postWriterInfo = safePostWriterInfo;
    }
}