package com.foodapp.price_reader.security;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DevHashGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder(10); // cost 10
        String plain = "Admin123!";
        System.out.println(encoder.encode(plain));
    }
}