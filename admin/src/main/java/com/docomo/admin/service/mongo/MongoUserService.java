package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoUser;
import com.docomo.admin.repository.mongo.MongoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MongoUserService {
    
    @Autowired
    private MongoUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<MongoUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<MongoUser> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    public Optional<MongoUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<MongoUser> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<MongoUser> getUsersByRole(MongoUser.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<MongoUser> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }
    
    public List<MongoUser> searchUsersByFullName(String fullName) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName);
    }
    
    public MongoUser createUser(MongoUser user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public MongoUser updateUser(String id, MongoUser userDetails) {
        MongoUser user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public MongoUser updatePassword(String id, String newPassword) {
        MongoUser user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public List<MongoUser> getUsersCreatedAfter(LocalDateTime date) {
        return userRepository.findByCreatedAtAfter(date);
    }
} 