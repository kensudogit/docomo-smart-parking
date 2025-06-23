package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataMongoTest
class MongoUserRepositoryTest {
    
    @Autowired
    private MongoUserRepository userRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private MongoUser testUser1;
    private MongoUser testUser2;
    private MongoUser testUser3;
    
    @BeforeEach
    void setUp() {
        // Clear the collection before each test
        mongoTemplate.dropCollection("users");
        
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
    void testFindAll() {
        List<MongoUser> users = userRepository.findAll();
        assertEquals(3, users.size());
    }
    
    @Test
    void testFindById() {
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("admin1", foundUser.get().getUsername());
        assertEquals("admin1@example.com", foundUser.get().getEmail());
    }
    
    @Test
    void testFindByUsername() {
        Optional<MongoUser> foundUser = userRepository.findByUsername("admin1");
        assertTrue(foundUser.isPresent());
        assertEquals("admin1", foundUser.get().getUsername());
        assertEquals(MongoUser.UserRole.ADMIN, foundUser.get().getRole());
    }
    
    @Test
    void testFindByEmail() {
        Optional<MongoUser> foundUser = userRepository.findByEmail("manager1@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("manager1@example.com", foundUser.get().getEmail());
        assertEquals(MongoUser.UserRole.MANAGER, foundUser.get().getRole());
    }
    
    @Test
    void testFindByRole() {
        List<MongoUser> adminUsers = userRepository.findByRole(MongoUser.UserRole.ADMIN);
        assertEquals(1, adminUsers.size());
        assertEquals("admin1", adminUsers.get(0).getUsername());
        
        List<MongoUser> managerUsers = userRepository.findByRole(MongoUser.UserRole.MANAGER);
        assertEquals(1, managerUsers.size());
        assertEquals("manager1", managerUsers.get(0).getUsername());
        
        List<MongoUser> operatorUsers = userRepository.findByRole(MongoUser.UserRole.OPERATOR);
        assertEquals(1, operatorUsers.size());
        assertEquals("operator1", operatorUsers.get(0).getUsername());
    }
    
    @Test
    void testFindByUsernameContainingIgnoreCase() {
        List<MongoUser> users = userRepository.findByUsernameContainingIgnoreCase("admin");
        assertEquals(1, users.size());
        assertEquals("admin1", users.get(0).getUsername());
        
        // Test case insensitive
        List<MongoUser> usersCaseInsensitive = userRepository.findByUsernameContainingIgnoreCase("ADMIN");
        assertEquals(1, usersCaseInsensitive.size());
    }
    
    @Test
    void testFindByFullNameContainingIgnoreCase() {
        List<MongoUser> users = userRepository.findByFullNameContainingIgnoreCase("Admin");
        assertEquals(1, users.size());
        assertEquals("Admin User 1", users.get(0).getFullName());
        
        // Test case insensitive
        List<MongoUser> usersCaseInsensitive = userRepository.findByFullNameContainingIgnoreCase("admin");
        assertEquals(1, usersCaseInsensitive.size());
    }
    
    @Test
    void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername("admin1"));
        assertTrue(userRepository.existsByUsername("manager1"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
    
    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("admin1@example.com"));
        assertTrue(userRepository.existsByEmail("manager1@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }
    
    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime now = LocalDateTime.now();
        List<MongoUser> users = userRepository.findByCreatedAtAfter(now.minusDays(1));
        assertEquals(3, users.size());
        
        List<MongoUser> futureUsers = userRepository.findByCreatedAtAfter(now.plusDays(1));
        assertEquals(0, futureUsers.size());
    }
    
    @Test
    void testSave() {
        MongoUser newUser = new MongoUser("newuser", "password", "new@example.com", "New User", MongoUser.UserRole.OPERATOR);
        MongoUser savedUser = userRepository.save(newUser);
        
        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());
        
        // Verify it was actually saved
        Optional<MongoUser> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
    }
    
    @Test
    void testUpdate() {
        testUser1.setFullName("Updated Admin User");
        testUser1.setEmail("updated@example.com");
        MongoUser updatedUser = userRepository.save(testUser1);
        
        assertEquals("Updated Admin User", updatedUser.getFullName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        
        // Verify the update was persisted
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Admin User", foundUser.get().getFullName());
    }
    
    @Test
    void testDelete() {
        userRepository.deleteById(testUser1.getId());
        
        Optional<MongoUser> foundUser = userRepository.findById(testUser1.getId());
        assertFalse(foundUser.isPresent());
        
        List<MongoUser> remainingUsers = userRepository.findAll();
        assertEquals(2, remainingUsers.size());
    }
    
    @Test
    void testDeleteAll() {
        userRepository.deleteAll();
        
        List<MongoUser> users = userRepository.findAll();
        assertEquals(0, users.size());
    }
} 