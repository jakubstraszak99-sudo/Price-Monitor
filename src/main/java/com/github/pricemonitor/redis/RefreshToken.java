package com.github.pricemonitor.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RefreshToken")
public class RefreshToken {

    @Id
    @NonNull
    private String token;

    @NonNull
    private UUID userPublicId;

    @NonNull
    @TimeToLive
    private Long expirationInSeconds;

}
