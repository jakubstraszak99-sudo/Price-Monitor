package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = "product")
    Page<NotificationEntity> findByUserPublicIdOrderByCreatedAtDesc(final UUID userPublicId, final Pageable pageable);

    long countByUserPublicIdAndReadFalse(final UUID userPublicId);

    Optional<NotificationEntity> findByPublicIdAndUserPublicId(final UUID publicId, final UUID userPublicId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.user.publicId = :userPublicId AND n.read = false")
    void markAllAsRead(final UUID userPublicId);

    void deleteByPublicIdAndUserPublicId(final UUID publicId, final UUID userPublicId);

    void deleteByUserPublicId(final UUID userPublicId);

}
