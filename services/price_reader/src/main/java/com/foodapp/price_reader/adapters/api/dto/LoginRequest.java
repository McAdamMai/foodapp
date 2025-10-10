package com.foodapp.price_reader.adapters.api.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}