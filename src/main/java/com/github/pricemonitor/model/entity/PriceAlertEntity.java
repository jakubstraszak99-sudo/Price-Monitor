package com.github.pricemonitor.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "price_alerts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_product_alert", columnNames = {"user_id", "product_id"})
})
public class PriceAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private Boolean active = true;

}