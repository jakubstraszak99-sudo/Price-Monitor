package com.github.pricemonitor.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "price_alerts", indexes = {
        @Index(name = "idx_price_alerts_public_id", columnList = "public_id", unique = true)
})
public class PriceAlertEntity extends BaseEntity {

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductEntity product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

}