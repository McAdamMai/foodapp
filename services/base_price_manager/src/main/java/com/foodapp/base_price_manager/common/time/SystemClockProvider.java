package com.foodapp.base_price_manager.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class SystemClockProvider implements ClockProvider {

    private final Clock clock;


    public SystemClockProvider() {
        this.clock = Clock.systemUTC();
    }


    public SystemClockProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant nowInstant() {
        return Instant.now(clock);
    }
}
