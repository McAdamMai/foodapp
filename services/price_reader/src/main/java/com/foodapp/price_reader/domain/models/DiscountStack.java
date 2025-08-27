package com.foodapp.price_reader.domain.models;

import com.google.protobuf.Enum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscountStack {
    private String discountId;
    private Enum discountType;
    private double amount;

    //No-arg constructor for frameworks like Spring Data
    public DiscountStack(){}

    //All-arg constructor for convenience
    public DiscountStack(String discountId, Enum discountType, double amount) {}
}
