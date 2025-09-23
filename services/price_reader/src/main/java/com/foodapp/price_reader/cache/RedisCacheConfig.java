package com.foodapp.price_reader.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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

    @Bean
    public RedisCacheConfiguration baseRedisCacheConfiguration( // create a foundation configuration that will be used in all caches
            @Value("${app.cache.redis.cacheNullValue:false}") boolean cacheNullValue,
            @Value("${app.cache.redis.keyPrefix}") String keyPrefix
    ) {
        // serialize the key to plain text;
        var keySerializer = new StringRedisSerializer();
        var objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Serialize an object into JSON using the configured mapper
        var valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        if (cacheNullValue) {
            defaultCacheConfig = defaultCacheConfig.entryTtl(Duration.ZERO); // no-op; keeping disable by default
        }
        if (keyPrefix != null && !keyPrefix.isBlank()) {
            defaultCacheConfig = defaultCacheConfig.prefixCacheNameWith(keyPrefix + "::");
        }
        return defaultCacheConfig;
    }

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration baseRedisCacheConfiguration,
            @Value("${app.cache.price.ttl:PT5M}") Duration priceTtl,
            @Value("${app.cache.timeline.ttl:PT5M}") Duration timelineTtl
    ) {
        Map<String, RedisCacheConfiguration> perCacheConfigurations = new HashMap<>();
        // creates two distinct caches with different expiration time
        perCacheConfigurations.put(PRICE_CACHE, baseRedisCacheConfiguration.entryTtl(priceTtl)); // add individual manager
        perCacheConfigurations.put(TIMELINE_CACHE, baseRedisCacheConfiguration.entryTtl(timelineTtl));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseRedisCacheConfiguration)
                .withInitialCacheConfigurations(perCacheConfigurations)
                .transactionAware()
                .build();
    }
}
