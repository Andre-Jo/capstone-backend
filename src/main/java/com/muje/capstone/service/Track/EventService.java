package com.muje.capstone.service.Track;

import com.muje.capstone.domain.Track.Event;
import com.muje.capstone.domain.Track.Session;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Track.EventRequestDto;
import com.muje.capstone.repository.Track.EventRepository;
import com.muje.capstone.repository.Track.SessionRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public void saveEvents(List<EventRequestDto> dtos) {
        for (EventRequestDto dto : dtos) {
            Session session = sessionRepository.findById(dto.getSession_id())
                    .orElseThrow(() -> new IllegalArgumentException("세션 없음: " + dto.getSession_id()));

            User user = null;
            if (dto.getUserId() != null) {
                user = userRepository.findByEmail(dto.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + dto.getUserId()));
            }

            LocalDateTime parsedTimestamp;
            try {
                parsedTimestamp = LocalDateTime.parse(dto.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("잘못된 timestamp 형식: " + dto.getTimestamp());
            }

            Event event = Event.builder()
                    .session(session)
                    .user(user)
                    .eventType(dto.getEvent_type())
                    .eventValue(dto.getEvent_value())
                    .timestamp(parsedTimestamp)
                    .build();

            eventRepository.save(event);
        }
    }
}