package com.github.pricemonitor.api;

import com.github.pricemonitor.model.page.NotificationPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Notification")
@RequestMapping("api/v1/notification")
public interface NotificationApi {

    @Operation(summary = "Fetch notifications", description = "Returns a paginated list of the current user's notifications")
    @ApiResponse(
            responseCode = "200",
            description = "Page of notifications retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationPage.class)
            )
    )
    @GetMapping
    ResponseEntity<NotificationPage> getNotifications(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") final Pageable pageable,
            @AuthenticationPrincipal final UUID userPublicId
    );

    @Operation(summary = "Get unread notification count", description = "Returns how many of the current user's notifications are unread")
    @ApiResponse(
            responseCode = "200",
            description = "Unread count retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Long.class))
    )
    @GetMapping("/unread/count")
    ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal final UUID userPublicId);

    @Operation(summary = "Mark notification as read", description = "Marks notification belonging to the current user as read")
    @ApiResponse(responseCode = "204", description = "Notification marked as read")
    @PatchMapping("/read")
    ResponseEntity<Void> markAsRead(
            @RequestParam(value = "publicId", required = false) final UUID notificationPublicId,
            @AuthenticationPrincipal final UUID userPublicId
    );

    @Operation(summary = "Delete notification", description = "Removes notification belonging to the current user")
    @ApiResponse(responseCode = "204", description = "Notification removed successfully")
    @DeleteMapping("/delete")
    ResponseEntity<Void> deleteNotification(
            @RequestParam(value = "publicId", required = false) final UUID notificationPublicId,
            @AuthenticationPrincipal final UUID userPublicId
    );

}
