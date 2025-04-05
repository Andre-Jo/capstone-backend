package com.muje.capstone.repository;

import com.muje.capstone.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import com.muje.capstone.domain.User;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllByUserOrderByCreatedAtDesc(User user);
}