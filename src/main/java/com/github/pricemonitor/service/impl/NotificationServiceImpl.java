package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.model.dto.Notification;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.mapper.NotificationMapper;
import com.github.pricemonitor.model.page.NotificationPage;
import com.github.pricemonitor.repository.NotificationRepository;
import com.github.pricemonitor.service.NotificationService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.pricemonitor.model.helper.NotificationType.PRICE_DROP;
import static com.github.pricemonitor.model.helper.NotificationType.PRODUCT_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notifyPriceDrop(final UserEntity user, final ProductEntity product, final BigDecimal triggerPrice) {
        this.notificationRepository.save(this.notificationMapper.map(user, product, PRICE_DROP, triggerPrice));
    }

    @Override
    @Transactional
    public void notifyProductUnavailable(final UserEntity user, final ProductEntity product) {
        this.notificationRepository.save(this.notificationMapper.map(user, product, PRODUCT_UNAVAILABLE, null));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPage getNotifications(final Pageable pageable, final UUID userPublicId) {
        final Page<Notification> page = this.notificationRepository
                .findByUserPublicIdOrderByCreatedAtDesc(userPublicId, pageable)
                .map(this.notificationMapper::map);
        return new NotificationPage(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(final UUID userPublicId) {
        return this.notificationRepository.countByUserPublicIdAndReadFalse(userPublicId);
    }

    @Override
    @Transactional
    public void markAsRead(@Nullable final UUID notificationPublicId, final UUID userPublicId) {
        if (notificationPublicId != null) {
            this.notificationRepository.findByPublicIdAndUserPublicId(notificationPublicId, userPublicId)
                    .ifPresent(notification -> notification.setRead(true));
        }

        else {
            this.notificationRepository.markAllAsRead(userPublicId);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(@Nullable final UUID notificationPublicId, final UUID userPublicId) {
        if (notificationPublicId != null) {
            this.notificationRepository.deleteByPublicIdAndUserPublicId(notificationPublicId, userPublicId);
        }

        else {
            this.notificationRepository.deleteByUserPublicId(userPublicId);
        }
    }

}
