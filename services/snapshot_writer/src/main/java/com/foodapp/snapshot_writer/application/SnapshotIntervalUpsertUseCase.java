import com.foodapp.snapshot_writer.infrastructure.persistence.SnapshotIntervalWriteRepository;
import com.foodapp.snapshot_writer.message.dto.PriceSnapshotIntervalUpsertedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
/*/
@Service
@RequiredArgsConstructor
public class SnapshotIntervalUpsertUseCase {

    private final SnapshotIntervalWriteRepository writeRepo;
    private final WriterAggregateProgressRepository progressRepo;

    @Transactional
    public void handle(PriceSnapshotIntervalUpsertedEvent event) {
        UUID aggId = UUID.fromString(event.sourceAggregateId());

        Long last = progressRepo.findLastVersion(aggId);
        if (last != null && event.sourceAggregateVersion() <= last) {
            return; // Ignore duplicate or stale events (idempotency)
        }

        writeRepo.upsert(event); // Perform the actual write/upsert to price_snapshot_interval

        progressRepo.upsertLastVersion(aggId, event.sourceAggregateVersion());
    }
}
/*/