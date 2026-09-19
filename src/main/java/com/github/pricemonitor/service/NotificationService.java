package com.github.pricemonitor.service;

import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.page.NotificationPage;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationService {

    void notifyPriceDrop(final UserEntity user, final ProductEntity product, final BigDecimal triggerPrice);

    void notifyProductUnavailable(final UserEntity user, final ProductEntity product);

    NotificationPage getNotifications(final Pageable pageable, final UUID userPublicId);

    long getUnreadCount(final UUID userPublicId);

    void markAsRead(@Nullable final UUID notificationPublicId, final UUID userPublicId);

    void deleteNotification(@Nullable final UUID notificationPublicId, final UUID userPublicId);

}
