package com.foodapp.snapshot_writer.infrastructure.persistence;

import com.foodapp.snapshot_writer.message.dto.PriceSnapshotIntervalUpsertedEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SnapshotIntervalWriteRepository {
    void upsert(PriceSnapshotIntervalUpsertedEvent event);
}
