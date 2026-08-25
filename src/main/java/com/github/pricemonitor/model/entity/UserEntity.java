package com.github.pricemonitor.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_public_id", columnList = "public_id", unique = true)
})
public class UserEntity extends BaseEntity {

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @Column(nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PriceAlertEntity> priceAlerts = new ArrayList<>();

}
