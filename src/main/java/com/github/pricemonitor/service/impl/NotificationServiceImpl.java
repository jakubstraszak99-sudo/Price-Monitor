package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.model.dto.Notification;
import com.github.pricemonitor.model.entity.NotificationEntity;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.pricemonitor.model.helper.NotificationType.PRICE_DROP;
import static com.github.pricemonitor.model.helper.NotificationType.PRODUCT_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String NOTIFICATION_QUEUE = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void notifyPriceDrop(final UserEntity user, final ProductEntity product, final BigDecimal triggerPrice) {
        final NotificationEntity saved = this.notificationRepository.save(
                this.notificationMapper.map(user, product, PRICE_DROP, triggerPrice));
        this.pushNotification(user, saved);
    }

    @Override
    @Transactional
    public void notifyProductUnavailable(final UserEntity user, final ProductEntity product) {
        final NotificationEntity saved = this.notificationRepository.save(
                this.notificationMapper.map(user, product, PRODUCT_UNAVAILABLE, null));
        this.pushNotification(user, saved);
    }

    @Override
    @Transactional
    public void notifyProductRemoved(final UserEntity user, final ProductEntity product) {
        final NotificationEntity notification = this.notificationMapper.map(user, product, PRODUCT_UNAVAILABLE, null);
        notification.setProduct(null);
        final NotificationEntity saved = this.notificationRepository.save(notification);
        this.pushNotification(user, saved);
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

    private void pushNotification(final UserEntity user, final NotificationEntity saved) {
        final Notification notification = this.notificationMapper.map(saved);
        final Runnable push = () -> this.messagingTemplate.convertAndSendToUser(
                user.getPublicId().toString(), NOTIFICATION_QUEUE, notification);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push.run();
                }
            });
        } else {
            push.run();
        }
    }

}
