package com.nutries.authservice.controller;

import com.nutries.authservice.dto.JwtResponse;
import com.nutries.authservice.dto.LoginDto;
import com.nutries.authservice.dto.ProfileRegisterDto;
import com.nutries.authservice.dto.RegisterDto;
import com.nutries.authservice.model.Role;
import com.nutries.authservice.model.User;
import com.nutries.authservice.repository.RoleRepository;
import com.nutries.authservice.repository.UserRepository;
import com.nutries.authservice.security.CustomUserDetailsService;
import com.nutries.authservice.security.JwtUtil;
import com.nutries.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> registerUser(@RequestBody RegisterDto registerDto) {

        if (userRepository.existsByUsername(registerDto.getUsername()) || userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        // Создаем пользователя
        User user = new User();
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setUsername(registerDto.getUsername());

        // Присваиваем роль пользователю
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not set."));
        user.setRoles(Collections.singleton(userRole));

        // Сохраняем пользователя
        userRepository.save(user);

        // Создаем профиль в другом сервисе
        ProfileRegisterDto profileRegisterDto = new ProfileRegisterDto();
        profileRegisterDto.setEmail(user.getEmail());
        profileRegisterDto.setUsername(user.getUsername());

        // Обрабатываем возможную ошибку при создании профиля
        try {
            authService.createProfileInProfileService(profileRegisterDto);
        } catch (Exception e) {
            throw new RuntimeException("Profile creation failed: " + e.getMessage());
        }
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginDto loginDto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsername());

        JwtResponse response = new JwtResponse();
        response.setAccessToken(jwtUtil.generateToken(userDetails.getUsername()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
