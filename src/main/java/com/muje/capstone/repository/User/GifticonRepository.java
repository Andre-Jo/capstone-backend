package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.Gifticon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GifticonRepository extends JpaRepository<Gifticon,Long> {
    @Query(value = "SELECT * FROM gifticons ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Gifticon findRandomOne();
}