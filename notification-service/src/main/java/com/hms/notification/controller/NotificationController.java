package com.hms.notification.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.dto.PageResponse;
import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.common.web.TraceIdSupport;
import com.hms.notification.dto.NotificationView;
import com.hms.notification.dto.SendNotificationRequest;
import com.hms.notification.dto.UpdatePreferenceRequest;
import com.hms.notification.entity.Notification;
import com.hms.notification.model.NotificationStatus;
import com.hms.notification.repository.NotificationRepository;
import com.hms.notification.service.NotificationDispatchService;
import com.hms.notification.service.PreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** SRS 6.2.9. */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationDispatchService dispatchService;
    private final NotificationRepository notificationRepository;
    private final PreferenceService preferenceService;

    public NotificationController(NotificationDispatchService dispatchService,
                                   NotificationRepository notificationRepository,
                                   PreferenceService preferenceService) {
        this.dispatchService = dispatchService;
        this.notificationRepository = notificationRepository;
        this.preferenceService = preferenceService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationView>> send(@Valid @RequestBody SendNotificationRequest request) {
        Notification notification = dispatchService.dispatch(
                request.userId(), request.channel(), request.templateCode(), request.variablesOrEmpty());
        return ResponseEntity.status(201).body(
                ApiResponse.created(NotificationView.from(notification), "Notification dispatched", TraceIdSupport.current()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<NotificationView>>> query(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> result;
        if (userId != null && status != null) {
            result = notificationRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            result = notificationRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            result = notificationRepository.findByStatus(status, pageable);
        } else {
            result = notificationRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result.map(NotificationView::from)), TraceIdSupport.current()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<Void>> updatePreferences(@Valid @RequestBody UpdatePreferenceRequest request,
                                                                 HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        preferenceService.upsert(userId, request.channel(), request.enabled());
        return ResponseEntity.ok(ApiResponse.ok(null, TraceIdSupport.current()));
    }
}
