package com.docomo.admin.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MongoUser {
    
    @Id
    private String id;
    
    @Field("username")
    private String username;
    
    @Field("password")
    private String password;
    
    @Field("email")
    private String email;
    
    @Field("full_name")
    private String fullName;
    
    @Field("role")
    private UserRole role;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        ADMIN, MANAGER, OPERATOR
    }
    
    public MongoUser(String username, String password, String email, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 