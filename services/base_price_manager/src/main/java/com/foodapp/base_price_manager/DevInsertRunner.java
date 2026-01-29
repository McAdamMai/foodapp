package com.foodapp.base_price_manager;

import com.foodapp.base_price_manager.infrastructure.persistence.entity.BasePriceChangeRequestEntity;
import com.foodapp.base_price_manager.infrastructure.persistence.repository.BasePriceChangeRequestWriteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.foodapp.base_price_manager.common.time.ClockProvider;

import java.math.BigDecimal;

@Component
@Profile("dev")
public class DevInsertRunner implements CommandLineRunner {

    private final BasePriceChangeRequestWriteRepository writeRepository;
    private final ClockProvider clock;

    public DevInsertRunner(BasePriceChangeRequestWriteRepository writeRepository,
                           ClockProvider clock) {
        this.writeRepository = writeRepository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        var now = clock.nowInstant();

        BasePriceChangeRequestEntity entity = BasePriceChangeRequestEntity.createSubmitted(
                1L,                // tenantId
                10L,               // storeId
                100L,              // skuId
                0L,                // userSegId
                1L,                // channelId (nullable in DB, but you can fill it)
                now.plusSeconds(3600), // startAtUtc
                new BigDecimal("9.99"), // basePrice
                "dev insert",       // reason
                "dev",              // operator
                now
        );



        BasePriceChangeRequestEntity saved = writeRepository.save(entity);

        System.out.println("✅ inserted BasePriceChangeRequest id=" + saved.getId()
                + " version=" + saved.getVersion()
                + " status=" + saved.getStatus());
    }
}
