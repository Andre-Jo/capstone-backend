package com.muje.capstone.service.User;

import com.muje.capstone.domain.User.User;
import com.muje.capstone.domain.User.Graduate;
import com.muje.capstone.domain.User.Student;
import com.muje.capstone.dto.User.SingUp_In.AddUserRequest;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.dto.User.UserInfo.UserUpdateRequest;
import com.muje.capstone.repository.User.GraduateRepository;
import com.muje.capstone.repository.User.StudentRepository;
import com.muje.capstone.repository.User.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final GraduateRepository graduateRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean nicknameExists(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    public Long save(AddUserRequest dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + dto.getEmail());
        }

        User user;

        if (dto.getUserType() == User.UserType.STUDENT) {
            user = Student.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .school(dto.getSchool())
                    .department(dto.getDepartment())
                    .studentYear(dto.getStudentYear())
                    .userType(dto.getUserType())
                    .profileImage(dto.getProfileImage())
                    .isSchoolVerified(dto.getIsSchoolVerified())
                    .isSocialLogin(dto.getIsSocialLogin())
                    .enabled(true)
                    .subscriptionFee(dto.getSubscriptionFee())
                    .build();
        } else if (dto.getUserType() == User.UserType.GRADUATE) {
            user = Graduate.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .school(dto.getSchool())
                    .department(dto.getDepartment())
                    .studentYear(dto.getStudentYear())
                    .userType(dto.getUserType())
                    .profileImage(dto.getProfileImage())
                    .isSchoolVerified(dto.getIsSchoolVerified())
                    .isSocialLogin(dto.getIsSocialLogin())
                    .enabled(true)
                    .currentCompany(dto.getCurrentCompany())
                    .currentSalary(dto.getCurrentSalary())
                    .occupation(dto.getOccupation())
                    .skills(dto.getSkills())
                    .isCompanyVerified(dto.getIsCompanyVerified())
                    .build();
        } else {
            throw new IllegalArgumentException("지원하지 않는 사용자 유형입니다: " + dto.getUserType());
        }

        return userRepository.save(user).getId();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    public UserInfoResponse getUserInfoByEmail(String email) {
        User user = findByEmail(email);

        // 하위 클래스까지 강제 로딩
        if (user.getUserType() == User.UserType.STUDENT) {
            user = studentRepository.findByEmail(email); // 업캐스팅
        } else if (user.getUserType() == User.UserType.GRADUATE) {
            user = graduateRepository.findByEmail(email); // 업캐스팅
        }

        // 공통 필드
        Long id = user.getId();
        String userEmail = user.getEmail();
        String nickname = user.getNickname();
        String school = user.getSchool();
        String department = user.getDepartment();
        Integer studentYear = user.getStudentYear();
        User.UserType userType = user.getUserType();
        Integer points = user.getPoints();
        String profileImage = user.getProfileImage();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime updatedAt = user.getUpdatedAt();
        Boolean isSchoolVerified = user.getIsSchoolVerified();
        Boolean isSocialLogin = user.getIsSocialLogin();
        Boolean enabled = user.getEnabled();

        // 재학생 전용 필드
        Boolean isSubscribed = null;
        LocalDateTime subscriptionStartDate = null;
        LocalDateTime subscriptionEndDate = null;

        // 졸업생 전용 필드
        String currentCompany = null;
        String currentSalary = null;
        String occupation = null;
        String skills = null;
        Boolean isCompanyVerified = null;

        if (user instanceof Student student) {
            isSubscribed = student.isSubscriptionActive();
            subscriptionStartDate = student.getSubscriptionStart();
            subscriptionEndDate = student.getSubscriptionEnd();
        } else if (user instanceof Graduate graduate) {
            currentCompany = graduate.getCurrentCompany();
            currentSalary = graduate.getCurrentSalary();
            occupation = graduate.getOccupation();
            skills = graduate.getSkills();
            isCompanyVerified = graduate.getIsCompanyVerified();
        }

        return new UserInfoResponse(
                id, userEmail, nickname, school, department, studentYear, userType, points,
                profileImage, createdAt, updatedAt, isSchoolVerified, isSocialLogin, enabled,
                isSubscribed, subscriptionStartDate, subscriptionEndDate,
                currentCompany, currentSalary, occupation, skills, isCompanyVerified
        );
    }

    public Graduate getGraduateByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Graduate not found with email: " + email));
        if (user.getUserType() == User.UserType.GRADUATE) {
            return (Graduate) user;
        } else {
            throw new IllegalArgumentException("User is not a Graduate");
        }
    }

    public void deactivateUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User userToDeactivate = userOptional.get();
            userToDeactivate.setEnabled(false);
            userRepository.save(userToDeactivate);
        } else {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
    }

    public boolean isGraduate(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getUserType() == User.UserType.GRADUATE)
                .isPresent();
    }

    public boolean isSubscribedStudent(String email) {
        User user = findByEmail(email);
        if (user instanceof Student student) {
            return student.isSubscriptionActive()
                    && student.getSubscriptionEnd() != null
                    && !student.getSubscriptionEnd().isBefore(LocalDateTime.now());
        }
        return false;
    }

    public boolean canViewGraduateReviews(String email) {
        return isGraduate(email) || isSubscribedStudent(email);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(String email, UserUpdateRequest request) {
        User user = findByEmail(email);

        // 비밀번호 업데이트
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 닉네임 업데이트
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }

        // 졸업생 관련 필드는 UserType이 GRADUATE일 때만 업데이트
        if (user.getUserType() == User.UserType.GRADUATE) {
            Graduate graduate = (Graduate) user;

            if (request.getCurrentSalary() != null) {
                graduate.setCurrentSalary(request.getCurrentSalary());
            }
            if (request.getOccupation() != null) {
                graduate.setOccupation(request.getOccupation());
            }
            if (request.getSkills() != null) {
                graduate.setSkills(request.getSkills());
            }

        }

        User updatedUser = userRepository.save(user); // 변경된 내용 저장
        return new UserInfoResponse(updatedUser); // 업데이트된 정보로 응답
    }

    @Transactional
    public void updateProfileImage(String email, String fileName) {
        User user = findByEmail(email);
        user.setProfileImage(fileName);
        userRepository.save(user);
    }
}