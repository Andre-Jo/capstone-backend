package com.muje.capstone.repository;

import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findByStudentOrderByStartDateDesc(Student student);
    Optional<SubscriptionHistory> findTopByStudentOrderByStartDateDesc(Student student);
    List<SubscriptionHistory> findAllByStatusAndEndDateBefore(SubscriptionHistory.Status status, LocalDateTime cutoff);
}