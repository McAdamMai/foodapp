package com.foodapp.price_reader.adapters.api.controller;

import com.foodapp.price_reader.adapters.api.dto.LoginRequest;
import com.foodapp.price_reader.persistence.entity.UserEntity;
import com.foodapp.price_reader.persistence.repository.UserRepository;
import com.foodapp.price_reader.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user ->{
                    String token = jwtUtil.generateToken(user.getUsername(),user.getRole());
                    return ResponseEntity.ok(token);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password"));
    }
    /*

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username,
                                           @RequestParam String password,
                                           @RequestParam(defaultValue = "ROLE_USER") String role){
        if (userRepository.existsById(username)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        userRepository.save(new UserEntity(username, encodedPassword, role));
        return ResponseEntity.ok("User Registered successfully");

    }  */
}
