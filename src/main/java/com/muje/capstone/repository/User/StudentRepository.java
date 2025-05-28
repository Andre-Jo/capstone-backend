package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> { // Assuming Long is the ID type
    // 스케줄러가 사용할 갱신 대상 학생 찾기
    List<Student> findBySubscriptionStatusAndSubscriptionEndBefore(
            Student.SubscriptionStatus status,
            LocalDateTime cutoffDate
    );
    // CustomerKey로 Student 찾기 (CustomerKey가 Student 엔티티에 있다고 가정)
    Optional<Student> findByCustomerKey(String customerKey);

    Student findByEmail(String email);
}