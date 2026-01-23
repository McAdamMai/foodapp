package com.foodapp.promotion_expander.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cglib.core.Local;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartitionMaintenanceService {

    private final JdbcTemplate jdbcTemplate;

    // CONFIGURATION
    // --------------------------------------------------------
    // How many future months to create ahead of time?
    // Recommended: 3-6 (Safety against job failures)
    @Value("${expander.partition.future-months:6}")
    private int futureMonths;

    // How many past months to KEEP?
    // Example: 1 = Keep This Month + Last Month. Drop everything older.
    // Setting this to 0 is dangerous (drops last month immediately on the 1st).
    @Value("${expander.partition.retention-months:2}")
    private int retentionMonths;

    /**
     * Run Weekly (e.g., Sunday at 3 AM).
     * This single job manages the entire lifecycle (Birth & Death).
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensurePartitionsExist() {
        log.info("Starting Partition Maintenance (Creation & Cleanup)...");
        LocalDate today = LocalDate.now();

        // CYCLE: BIRTH
        for (int i = 0; i < futureMonths; i++) {
            createPartition(today.plusMonths(i));
        }
    }

    @Scheduled(cron = "0 0 4 * * SUN") // Run 1 hour after creation
    @Transactional
    public void cleanupExpiredPartitions() {
        log.info("Starting expired partition cleanup...");
        LocalDate today = LocalDate.now();

        int cleanStart = retentionMonths + 1;
        int cleanEnd = retentionMonths + 6;

        for (int i = cleanStart; i <= cleanEnd; i++) {
            dropPartition(today.minusMonths(i));
        }
    }

    private void createPartition(LocalDate date) {
        String tableName = getPartitionName(date);

        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF time_slice " +
                        "FOR VALUES FROM ('%s') TO ('%s')", tableName, start, end
        );
        try {
            jdbcTemplate.execute(sql);
            log.debug("Verified partition exists: {}", tableName);
        }catch (Exception e) {
            log.error("Failed to create partition {}", tableName, e);
        }
    }

    private void dropPartition(LocalDate date){
        String tableName = getPartitionName(date);

        LocalDate startOfPartition = date.withDayOfMonth(1);
        LocalDate endOfPartition = startOfPartition.plusMonths(1);
        LocalDate safeDropDate = endOfPartition.plusDays(1);

        LocalDate todayUTC = LocalDate.now(ZoneId.of("UTC"));

        if (!safeDropDate.isBefore(todayUTC)) {
            log.warn("SAFETY ABORT: Attempted to drop Current/Future partition {}. Skipping.", tableName);
            return;
        }

        String sql = String.format("DROP TABLE IF EXISTS %s", tableName);

        try{
            jdbcTemplate.execute(sql);
            log.warn("DROPPED EXPIRED PARTITION: {}", tableName); // here is a warning
        }catch (Exception e) {
            log.error("Failed to drop partition {}", tableName, e);
        }
    }

    private String getPartitionName(LocalDate date) {
        return "time_slice_" + date.format(DateTimeFormatter.ofPattern("yyyy_MM"));
    }
}