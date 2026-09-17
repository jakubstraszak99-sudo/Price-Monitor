package com.github.pricemonitor.redis.repository;

import com.github.pricemonitor.redis.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRedisRepository extends CrudRepository<PasswordResetToken, String> {
}
