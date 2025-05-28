package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.Graduate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraduateRepository extends JpaRepository<Graduate, Long> {
    Graduate findByEmail(String email);
}