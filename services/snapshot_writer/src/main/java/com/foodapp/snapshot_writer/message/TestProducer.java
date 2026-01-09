package com.foodapp.snapshot_writer.message;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class TestProducer implements CommandLineRunner {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void run(String... args) {
        String key = "test-key";
        String payload = "{\"hello\":\"snapshot-writer\"}";

        log.info("Sending test message to topic={} key={} payload={}", topic, key, payload);

        kafkaTemplate.send(topic, key, payload);
    }
}
