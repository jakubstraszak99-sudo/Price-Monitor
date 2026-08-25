package com.github.pricemonitor.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "_created", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
