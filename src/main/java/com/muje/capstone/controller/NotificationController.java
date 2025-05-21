package com.muje.capstone.controller;

import com.muje.capstone.dto.User.NotificationDto;
import com.muje.capstone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService svc;

    @GetMapping
    public List<NotificationDto> list(Principal principal) {
        return svc.findAll(principal.getName());
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id, Principal principal) {
        svc.markAsRead(id, principal.getName());
    }
}