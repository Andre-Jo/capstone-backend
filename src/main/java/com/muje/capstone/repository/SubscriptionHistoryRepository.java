package com.muje.capstone.repository;

import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findByStudentOrderByRequestedAtDesc(Student student);
    Optional<SubscriptionHistory> findByOrderId(String orderId);
}