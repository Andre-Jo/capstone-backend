package com.muje.capstone.repository.Track;

import com.muje.capstone.domain.Track.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}