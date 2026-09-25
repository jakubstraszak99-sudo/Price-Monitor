package com.github.pricemonitor.model.entity;

import com.github.pricemonitor.model.helper.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_notifications_user_id", columnList = "user_id")
})
public class NotificationEntity extends BaseEntity {

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column
    private BigDecimal triggerPrice;

}
