package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.repository.PriceAlertRepository;
import com.github.pricemonitor.service.PriceAlertNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.github.pricemonitor.kafka.KafkaConstants.ALERT_NOTIFICATION_TOPIC;

@Service
@RequiredArgsConstructor
public class PriceAlertNotificationServiceImpl implements PriceAlertNotificationService {

    private final PriceAlertRepository priceAlertRepository;
    private final KafkaEventPublisher eventPublisher;

    @Override
    @Transactional
    public void notifyAboutPriceChange(final ProductEntity product, final BigDecimal newPrice) {
        this.priceAlertRepository.findActiveAlertsForProduct(product.getId(), newPrice).forEach(alert -> {
            final String email = alert.getUser().getEmail();
            final EmailNotificationMessage event = new EmailNotificationMessage(email, product.getProductUrl());
            this.eventPublisher.publish(ALERT_NOTIFICATION_TOPIC, email, event);
            alert.setActive(false);
        });
    }

}
