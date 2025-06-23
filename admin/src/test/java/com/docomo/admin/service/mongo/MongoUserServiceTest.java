package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoUser;
import com.docomo.admin.repository.mongo.MongoUserRepository;
import com.docomo.admin.config.MongoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ContextConfiguration(classes = MongoTestConfig.class)
class MongoUserServiceTest {
    
    @Autowired
    private MongoUserService userService;
    
    @Autowired
    private MongoUserRepository userRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    private MongoUser testUser1;
    private MongoUser testUser2;
    private MongoUser testUser3;
    
    @BeforeEach
    void setUp() {
        // Clear the collection before each test
        mongoTemplate.dropCollection("users");
        
        // Mock password encoder
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        
        // Create test users
        testUser1 = new MongoUser("admin1", "password1", "admin1@example.com", "Admin User 1", MongoUser.UserRole.ADMIN);
        testUser2 = new MongoUser("manager1", "password2", "manager1@example.com", "Manager User 1", MongoUser.UserRole.MANAGER);
        testUser3 = new MongoUser("operator1", "password3", "operator1@example.com", "Operator User 1", MongoUser.UserRole.OPERATOR);
        
        // Save test users
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
    }
    
    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("users");
    }
    
    @Test
    void testGetAllUsers() {
        List<MongoUser> users = userService.getAllUsers();
        assertEquals(3, users.size());
    }
    
    @Test
    void testGetUserById() {
        Optional<MongoUser> foundUser = userService.getUserById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("admin1", foundUser.get().getUsername());
        assertEquals("admin1@example.com", foundUser.get().getEmail());
    }
    
    @Test
    void testGetUserByUsername() {
        Optional<MongoUser> foundUser = userService.getUserByUsername("admin1");
        assertTrue(foundUser.isPresent());
        assertEquals("admin1", foundUser.get().getUsername());
        assertEquals(MongoUser.UserRole.ADMIN, foundUser.get().getRole());
    }
    
    @Test
    void testGetUserByEmail() {
        Optional<MongoUser> foundUser = userService.getUserByEmail("manager1@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("manager1@example.com", foundUser.get().getEmail());
        assertEquals(MongoUser.UserRole.MANAGER, foundUser.get().getRole());
    }
    
    @Test
    void testGetUsersByRole() {
        List<MongoUser> adminUsers = userService.getUsersByRole(MongoUser.UserRole.ADMIN);
        assertEquals(1, adminUsers.size());
        assertEquals("admin1", adminUsers.get(0).getUsername());
        
        List<MongoUser> managerUsers = userService.getUsersByRole(MongoUser.UserRole.MANAGER);
        assertEquals(1, managerUsers.size());
        assertEquals("manager1", managerUsers.get(0).getUsername());
        
        List<MongoUser> operatorUsers = userService.getUsersByRole(MongoUser.UserRole.OPERATOR);
        assertEquals(1, operatorUsers.size());
        assertEquals("operator1", operatorUsers.get(0).getUsername());
    }
    
    @Test
    void testSearchUsersByUsername() {
        List<MongoUser> users = userService.searchUsersByUsername("admin");
        assertEquals(1, users.size());
        assertEquals("admin1", users.get(0).getUsername());
    }
    
    @Test
    void testSearchUsersByFullName() {
        List<MongoUser> users = userService.searchUsersByFullName("Admin");
        assertEquals(1, users.size());
        assertEquals("Admin User 1", users.get(0).getFullName());
    }
    
    @Test
    void testCreateUser() {
        MongoUser newUser = new MongoUser("newuser", "password", "new@example.com", "New User", MongoUser.UserRole.OPERATOR);
        MongoUser createdUser = userService.createUser(newUser);
        
        assertNotNull(createdUser.getId());
        assertEquals("newuser", createdUser.getUsername());
        assertEquals("new@example.com", createdUser.getEmail());
        assertEquals("encoded-password", createdUser.getPassword());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());
        
        // Verify it was actually saved
        Optional<MongoUser> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
    }
    
    @Test
    void testCreateUserWithDuplicateUsername() {
        MongoUser duplicateUser = new MongoUser("admin1", "password", "different@example.com", "Different User", MongoUser.UserRole.OPERATOR);
        
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(duplicateUser);
        });
    }
    
    @Test
    void testCreateUserWithDuplicateEmail() {
        MongoUser duplicateUser = new MongoUser("differentuser", "password", "admin1@example.com", "Different User", MongoUser.UserRole.OPERATOR);
        
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(duplicateUser);
        });
    }
    
    @Test
    void testUpdateUser() {
        MongoUser updateDetails = new MongoUser();
        updateDetails.setFullName("Updated Admin User");
        updateDetails.setEmail("updated@example.com");
        updateDetails.setRole(MongoUser.UserRole.MANAGER);
        
        MongoUser updatedUser = userService.updateUser(testUser1.getId(), updateDetails);
        
        assertEquals("Updated Admin User", updatedUser.getFullName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(MongoUser.UserRole.MANAGER, updatedUser.getRole());
        assertNotNull(updatedUser.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Admin User", foundUser.get().getFullName());
    }
    
    @Test
    void testUpdateUserNotFound() {
        MongoUser updateDetails = new MongoUser();
        updateDetails.setFullName("Updated User");
        
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser("nonexistent-id", updateDetails);
        });
    }
    
    @Test
    void testUpdatePassword() {
        MongoUser updatedUser = userService.updatePassword(testUser1.getId(), "newpassword");
        
        assertEquals("encoded-password", updatedUser.getPassword());
        assertNotNull(updatedUser.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("encoded-password", foundUser.get().getPassword());
    }
    
    @Test
    void testUpdatePasswordUserNotFound() {
        assertThrows(RuntimeException.class, () -> {
            userService.updatePassword("nonexistent-id", "newpassword");
        });
    }
    
    @Test
    void testDeleteUser() {
        userService.deleteUser(testUser1.getId());
        
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertFalse(foundUser.isPresent());
        
        List<MongoUser> remainingUsers = userService.getAllUsers();
        assertEquals(2, remainingUsers.size());
    }
    
    @Test
    void testDeleteUserNotFound() {
        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser("nonexistent-id");
        });
    }
    
    @Test
    void testExistsByUsername() {
        assertTrue(userService.existsByUsername("admin1"));
        assertTrue(userService.existsByUsername("manager1"));
        assertFalse(userService.existsByUsername("nonexistent"));
    }
    
    @Test
    void testExistsByEmail() {
        assertTrue(userService.existsByEmail("admin1@example.com"));
        assertTrue(userService.existsByEmail("manager1@example.com"));
        assertFalse(userService.existsByEmail("nonexistent@example.com"));
    }
    
    @Test
    void testGetUsersCreatedAfter() {
        LocalDateTime now = LocalDateTime.now();
        List<MongoUser> users = userService.getUsersCreatedAfter(now.minusDays(1));
        assertEquals(3, users.size());
        
        List<MongoUser> futureUsers = userService.getUsersCreatedAfter(now.plusDays(1));
        assertEquals(0, futureUsers.size());
    }
} 