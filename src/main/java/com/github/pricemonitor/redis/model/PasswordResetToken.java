package com.github.pricemonitor.redis.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("PasswordResetToken")
public class PasswordResetToken {

    @Id
    @NonNull
    private String tokenId;

    @NonNull
    @TimeToLive
    private Long expirationInSeconds;

}
