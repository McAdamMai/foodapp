package com.foodapp.price_reader.adapters.api.controller;

import com.foodapp.price_reader.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private  final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password){
        if ("admin".equals(username)&&"password".equals(password)){
            return ResponseEntity.ok(jwtUtil.generateToken(username));
        }
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
