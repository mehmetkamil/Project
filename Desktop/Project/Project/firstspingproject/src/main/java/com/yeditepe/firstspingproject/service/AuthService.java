package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.dto.LoginRequest;
import com.yeditepe.firstspingproject.dto.LoginResponse;
import com.yeditepe.firstspingproject.dto.RegisterRequest;
import com.yeditepe.firstspingproject.entity.Role;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.RoleRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.security.JwtUtil;
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

    // Register new user
    public LoginResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign roles
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
            // Default role: USER
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER", "Default user role")));
            roles.add(userRole);
        }
        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getUsername());

        // Prepare response
        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        LoginResponse response = new LoginResponse(token, savedUser.getUsername(), savedUser.getEmail(), roleNames);
        response.setId(savedUser.getId());
        return response;
    }

    // Login user
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            // Fetch user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            // Prepare response
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
