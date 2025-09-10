package com.foodapp.price_reader.enums;

public enum LimitConstraints {
    MIN_LIMIT(1),
    DEFAULT_LIMIT(100),
    MAX_LIMIT(1000);

    private final int value;

    LimitConstraints(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
