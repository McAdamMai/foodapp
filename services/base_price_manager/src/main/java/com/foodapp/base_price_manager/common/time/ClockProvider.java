package com.foodapp.base_price_manager.common.time;

import java.time.Instant;

public interface ClockProvider {
    Instant nowInstant();
}
