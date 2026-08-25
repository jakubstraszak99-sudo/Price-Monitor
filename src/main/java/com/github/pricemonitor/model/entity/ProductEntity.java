package com.github.pricemonitor.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_url", columnList = "product_url", unique = true)
})
public class ProductEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String productUrl;

    @Column(length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal currentPrice;

    @Column(length = 3)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PriceHistoryEntity> priceHistories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PriceAlertEntity> priceAlerts = new ArrayList<>();

}
