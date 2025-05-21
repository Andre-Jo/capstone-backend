package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.Student;
import com.muje.capstone.domain.User.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findByStudentOrderByRequestedAtDesc(Student student);
    Optional<SubscriptionHistory> findByOrderId(String orderId);
}