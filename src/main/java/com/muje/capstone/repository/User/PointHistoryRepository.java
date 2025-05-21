package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import com.muje.capstone.domain.User.User;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllByUserOrderByCreatedAtDesc(User user);
}