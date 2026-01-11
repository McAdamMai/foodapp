package com.foodapp.promotion_expander.infra.config;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    // 1. Define the Consumer Factory
    // This tells Kafka: "Keys are Strings, Values are JSON mapped to ExpanderEvent"
    @Value("${app.kafka.topics.concurrency}")
    private int concurrency;

    private final KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<String, ExpanderEvent> consumerFactory() {
        // This makes your Java code respect "spring.kafka.consumer.*" in your YAML
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        // 2. CONFIGURE JSON HERE (Via Properties, NOT Setters)
        // This resolves the "not both" conflict
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.foodapp.*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // 1. Create the Deserializer manually
        JsonDeserializer<ExpanderEvent> payloadDeserializer = new JsonDeserializer<>(ExpanderEvent.class);

        // KEY DESERIALIZER: String
        // VALUE DESERIALIZER: JsonDeserializer (wrapped in ErrorHandling to prevent infinite loops on bad JSON)
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(payloadDeserializer)
        );
    }

    @Bean
    // It Builds the "Runtime Engine" for the Listener
    public ConcurrentKafkaListenerContainerFactory<String, ExpanderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ExpanderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        // Applies the Custom Configuration
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        return factory;
    }
}
