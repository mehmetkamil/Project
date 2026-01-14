package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.dto.UserDTO;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Create user (Register)
    public UserDTO registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User(username, email);
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Get all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get user by ID
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Get user by username
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    // Login - validate username and password
    public boolean validateLogin(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return passwordEncoder.matches(password, user.get().getPassword());
        }
        return false;
    }

    // Update user
    public UserDTO updateUser(Long id, String email) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmail(email);
            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        }
        throw new RuntimeException("User not found!");
    }

    // Delete user
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found!");
        }
    }

    // Change password
    public void changePassword(Long id, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
            } else {
                throw new RuntimeException("Old password is incorrect!");
            }
        } else {
            throw new RuntimeException("User not found!");
        }
    }

    // Find users by username (search)
    public List<UserDTO> searchByUsername(String username) {
        return userRepository.findByUsernameStartingWith(username)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Convert User entity to UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }
}
