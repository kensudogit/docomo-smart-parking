package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MongoUserRepository extends MongoRepository<MongoUser, String> {
    
    Optional<MongoUser> findByUsername(String username);
    
    Optional<MongoUser> findByEmail(String email);
    
    List<MongoUser> findByRole(MongoUser.UserRole role);
    
    @Query("{'username': {$regex: ?0, $options: 'i'}}")
    List<MongoUser> findByUsernameContainingIgnoreCase(String username);
    
    @Query("{'full_name': {$regex: ?0, $options: 'i'}}")
    List<MongoUser> findByFullNameContainingIgnoreCase(String fullName);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("{'created_at': {$gte: ?0}}")
    List<MongoUser> findByCreatedAtAfter(java.time.LocalDateTime date);
} 