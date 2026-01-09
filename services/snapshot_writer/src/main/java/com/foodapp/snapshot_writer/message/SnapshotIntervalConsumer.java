package com.foodapp.snapshot_writer.message;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.foodapp.snapshot_writer.application.SnapshotIntervalUpsertUseCase;
import com.foodapp.snapshot_writer.message.dto.PriceSnapshotIntervalUpsertedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotIntervalConsumer {

    private final ObjectMapper mapper;
//    private final SnapshotIntervalUpsertUseCase useCase;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.groupId}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.debug("topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            PriceSnapshotIntervalUpsertedEvent event =
                    mapper.readValue(record.value(), PriceSnapshotIntervalUpsertedEvent.class);

//            useCase.handle(event);  // DB tx inside
            ack.acknowledge();      // commit offset AFTER DB success

        } catch (Exception e) {
            // IMPORTANT: don't swallow. Let Kafka retry / DLQ.
            log.error("SnapshotWriter failed. topic={}, partition={}, offset={}, payload={}",
                    record.topic(), record.partition(), record.offset(), record.value(), e);
            throw new RuntimeException(e);
        }
    }
}
