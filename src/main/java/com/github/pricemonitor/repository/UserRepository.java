package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPublicId(final UUID publicId);

    Optional<UserEntity> findByEmail(final String email);

    Optional<UserEntity> findByUsernameOrEmail(final String username, final String email);

}
