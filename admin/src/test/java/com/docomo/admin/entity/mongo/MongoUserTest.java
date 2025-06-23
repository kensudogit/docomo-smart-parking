package com.docomo.admin.entity.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class MongoUserTest {
    
    private MongoUser user;
    
    @BeforeEach
    void setUp() {
        user = new MongoUser();
        user.setId("test-id");
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(MongoUser.UserRole.ADMIN);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testMongoUserCreation() {
        assertNotNull(user);
        assertEquals("test-id", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals(MongoUser.UserRole.ADMIN, user.getRole());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
    
    @Test
    void testMongoUserConstructor() {
        MongoUser constructorUser = new MongoUser(
            "newuser", 
            "newpassword", 
            "new@example.com", 
            "New User", 
            MongoUser.UserRole.MANAGER
        );
        
        assertEquals("newuser", constructorUser.getUsername());
        assertEquals("newpassword", constructorUser.getPassword());
        assertEquals("new@example.com", constructorUser.getEmail());
        assertEquals("New User", constructorUser.getFullName());
        assertEquals(MongoUser.UserRole.MANAGER, constructorUser.getRole());
        assertNotNull(constructorUser.getCreatedAt());
        assertNotNull(constructorUser.getUpdatedAt());
    }
    
    @Test
    void testMongoUserSettersAndGetters() {
        user.setId("new-id");
        user.setUsername("newusername");
        user.setPassword("newpassword");
        user.setEmail("new@example.com");
        user.setFullName("New Full Name");
        user.setRole(MongoUser.UserRole.OPERATOR);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        assertEquals("new-id", user.getId());
        assertEquals("newusername", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("New Full Name", user.getFullName());
        assertEquals(MongoUser.UserRole.OPERATOR, user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
    
    @Test
    void testUserRoleEnum() {
        assertEquals("ADMIN", MongoUser.UserRole.ADMIN.name());
        assertEquals("MANAGER", MongoUser.UserRole.MANAGER.name());
        assertEquals("OPERATOR", MongoUser.UserRole.OPERATOR.name());
        
        assertEquals(0, MongoUser.UserRole.ADMIN.ordinal());
        assertEquals(1, MongoUser.UserRole.MANAGER.ordinal());
        assertEquals(2, MongoUser.UserRole.OPERATOR.ordinal());
    }
    
    @Test
    void testMongoUserEquality() {
        MongoUser user1 = new MongoUser();
        user1.setId("same-id");
        user1.setUsername("sameuser");
        
        MongoUser user2 = new MongoUser();
        user2.setId("same-id");
        user2.setUsername("sameuser");
        
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getUsername(), user2.getUsername());
    }
    
    @Test
    void testMongoUserToString() {
        String userString = user.toString();
        assertNotNull(userString);
        assertTrue(userString.contains("testuser"));
        assertTrue(userString.contains("test@example.com"));
    }
} 