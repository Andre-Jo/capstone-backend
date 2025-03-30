package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "Users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 받은 서브 클래스 id 선언 필요 없음
@SuperBuilder
@EntityListeners(AuditingEntityListener.class) // 👈 Auditing 기능 활성화
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(nullable = false)
    private String school;

    @Column(nullable = false)
    private String department; // 학과

    @Column(name = "student_year")
    private int studentYear; // 입학년도

    @Enumerated(EnumType.STRING) // Enum 값을 String 형태로 데이터베이스에 저장
    @Column(name = "userType", length = 20)
    private UserType userType;

    @Column(name = "points")
    @Builder.Default
    private int points = 0;

    @Column(name = "profileImage")
    private String profileImage;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_school_verified")
    private Boolean isSchoolVerified;

    @Column(name = "is_social_login")
    private Boolean isSocialLogin;

    @Column(name = "is_enabled") // 추가된 필드
    @Builder.Default
    private Boolean enabled = true; // 기본값으로 true 설정

    public User(String email, String password, String nickname, String school, String department, int studentYear, UserType userType, String profileImage, Boolean isSchoolVerified, Boolean isSocialLogin) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.school = school;
        this.department = department;
        this.studentYear = studentYear;
        this.userType = userType;
        this.profileImage = profileImage;
        this.isSchoolVerified = isSchoolVerified;
        this.isSocialLogin = isSocialLogin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료되지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금되지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 패스워드 기한 만료되지 않음
    }

    @Override
    public boolean isEnabled() { // 수정된 메서드
        return enabled;
    }

    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public void setEnabled(Boolean enabled) { // enabled 필드에 대한 setter 추가
        this.enabled = enabled;
    }
}