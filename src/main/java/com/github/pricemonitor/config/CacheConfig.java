package com.github.pricemonitor.config;

import com.github.pricemonitor.model.dto.PriceHistory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRICE_HISTORY = "priceHistory";

    @Bean
    public RedisCacheConfiguration priceHistoryCacheConfiguration(final ObjectMapper objectMapper) {
        final JacksonJsonRedisSerializer<List<PriceHistory>> serializer = new JacksonJsonRedisSerializer<>(
                objectMapper, objectMapper.getTypeFactory().constructCollectionType(List.class, PriceHistory.class));
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory,
                                         final RedisCacheConfiguration priceHistoryCacheConfiguration) {
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(Map.of(PRICE_HISTORY, priceHistoryCacheConfiguration))
                .disableCreateOnMissingCache()
                .transactionAware()
                .build();
    }

}
