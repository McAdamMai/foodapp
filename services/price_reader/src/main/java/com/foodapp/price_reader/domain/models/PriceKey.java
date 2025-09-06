package com.foodapp.price_reader.domain.models;

public record PriceKey(
        String tenantId,
        String storeId,
        String skuId,
        String userSegId, // nullable
        String channelId // nullable
){
    public PriceKey{
        if (isBlank(tenantId) || isBlank(storeId) || isBlank(skuId)) {
            throw new IllegalArgumentException("tenantId, storeId and skuId are required");
        }
    }
    // static method can be accessed by directly using the class without the need to create an object
    private static boolean isBlank(String s){
        // isBlank() return true if string contains only whitespace character, but throw an Exception if is null
        return s == null || s.isBlank();
    }
}

