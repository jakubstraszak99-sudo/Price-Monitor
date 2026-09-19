package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.NotificationApi;
import com.github.pricemonitor.model.page.NotificationPage;
import com.github.pricemonitor.service.NotificationService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationResource implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<NotificationPage> getNotifications(final Pageable pageable, final UUID userPublicId) {
        final NotificationPage page = this.notificationService.getNotifications(pageable, userPublicId);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @Override
    public ResponseEntity<Long> getUnreadCount(final UUID userPublicId) {
        final long count = this.notificationService.getUnreadCount(userPublicId);
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    @Override
    public ResponseEntity<Void> markAsRead(@Nullable final UUID notificationPublicId, final UUID userPublicId) {
        this.notificationService.markAsRead(notificationPublicId, userPublicId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deleteNotification(@Nullable final UUID notificationPublicId, final UUID userPublicId) {
        this.notificationService.deleteNotification(notificationPublicId, userPublicId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
