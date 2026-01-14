package com.yeditepe.userservice.service;

import com.yeditepe.userservice.dto.LoginRequest;
import com.yeditepe.userservice.dto.LoginResponse;
import com.yeditepe.userservice.dto.RegisterRequest;
import com.yeditepe.userservice.entity.Role;
import com.yeditepe.userservice.entity.User;
import com.yeditepe.userservice.repository.RoleRepository;
import com.yeditepe.userservice.repository.UserRepository;
import com.yeditepe.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role(roleName);
                            return roleRepository.save(newRole);
                        });
                roles.add(role);
            }
        } else {
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER", "Default user role")));
            roles.add(userRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getUsername());

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        LoginResponse response = new LoginResponse(token, savedUser.getUsername(), savedUser.getEmail(), roleNames);
        response.setId(savedUser.getId());
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtUtil.generateToken(user.getUsername());

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            LoginResponse response = new LoginResponse(token, user.getUsername(), user.getEmail(), roleNames);
            response.setId(user.getId());
            return response;
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }
}
