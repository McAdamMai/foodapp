package com.foodapp.price_reader.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.foodapp.price_reader.domain.models.PriceInterval;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig { // a class contain RedisCacheConfiguration and RedisCacheManager
    public static final String PRICE_CACHE = "price_cache";
    public static final String TIMELINE_CACHE = "timelineCache";

    private ObjectMapper objectMapper(){
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    }

    @Bean
    public RedisCacheConfiguration baseRedisCacheConfiguration( // create a foundation configuration that will be used in all caches
            @Value("${app.cache.redis.cacheNullValue:false}") boolean cacheNullValue, //
            @Value("${app.cache.redis.keyPrefix}") String keyPrefix
    ) {
        // serialize the key from Java string to UTF-8bytes;
        // deserialize the UTF-8bytes to Java string;
        var keySerializer = new StringRedisSerializer();

        // add keySerializer first
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .disableCachingNullValues();

        // null value won't be stored in redis
        if (cacheNullValue) {
            config = config.entryTtl(Duration.ZERO); // no-op; keeping disable by default
        }
        if (keyPrefix != null && !keyPrefix.isBlank()) {
            config = config.prefixCacheNameWith(keyPrefix + "::");
        }
        return config;
    }

    // where various distinct forms of data are managed;
    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration baseRedisCacheConfiguration,
            // define ttl for price and timeline
            @Value("${app.cache.price.ttl:PT5M}") Duration priceTtl,
            @Value("${app.cache.timeline.ttl:PT5M}") Duration timelineTtl
    ) {
        ObjectMapper mapper = objectMapper();

        // Type-aware value serializers
        var priceIntervalSerializer = new Jackson2JsonRedisSerializer<>(objectMapper(), PriceInterval.class);

        // TODO: add other models here, e.g. Timeline.class:
        // var timelineValueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper(), Timeline.class);

        Map<String, RedisCacheConfiguration> perCacheConfigurations = new HashMap<>();
        // creates two distinct caches with different expiration time
        perCacheConfigurations.put(PRICE_CACHE, baseRedisCacheConfiguration
                .entryTtl(priceTtl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(priceIntervalSerializer))); // add individual manager
        perCacheConfigurations.put(TIMELINE_CACHE, baseRedisCacheConfiguration
                .entryTtl(timelineTtl));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseRedisCacheConfiguration)
                .withInitialCacheConfigurations(perCacheConfigurations)
                .transactionAware()
                .build();
    }
}
