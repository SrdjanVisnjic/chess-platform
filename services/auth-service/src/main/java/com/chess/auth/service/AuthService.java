package com.chess.auth.service;

import com.chess.auth.dto.UserDTO;
import com.chess.auth.dto.requests.LoginRequest;
import com.chess.auth.dto.requests.RegisterRequest;
import com.chess.auth.dto.responses.AuthResponse;
import com.chess.auth.entity.User;
import com.chess.auth.repository.UserRepository;
import com.chess.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException(("Username already exists"));
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();
        User dbUser = userRepository.save(user);

        String token = jwtService.generateToke(dbUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .user(UserDTO.fromUser(dbUser))
                .build();
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("Invalid username"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToke(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .user(UserDTO.fromUser(user))
                .build();
    }
}
