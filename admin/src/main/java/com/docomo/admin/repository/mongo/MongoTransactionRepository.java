package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MongoTransactionRepository extends MongoRepository<MongoTransaction, String> {
    
    List<MongoTransaction> findByParkingLotId(String parkingLotId);
    
    List<MongoTransaction> findByUserId(String userId);
    
    List<MongoTransaction> findByStatus(MongoTransaction.TransactionStatus status);
    
    List<MongoTransaction> findByPaymentMethod(MongoTransaction.PaymentMethod paymentMethod);
    
    @Query("{'license_plate': {$regex: ?0, $options: 'i'}}")
    List<MongoTransaction> findByLicensePlateContainingIgnoreCase(String licensePlate);
    
    @Query("{'entry_time': {$gte: ?0, $lte: ?1}}")
    List<MongoTransaction> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{'exit_time': {$gte: ?0, $lte: ?1}}")
    List<MongoTransaction> findByExitTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{'created_at': {$gte: ?0, $lte: ?1}}")
    List<MongoTransaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{'amount': {$gte: ?0}}")
    List<MongoTransaction> findByAmountGreaterThanOrEqualTo(BigDecimal amount);
    
    @Query("{'amount': {$lte: ?0}}")
    List<MongoTransaction> findByAmountLessThanOrEqualTo(BigDecimal amount);
    
    @Query("{'parking_lot_id': ?0, 'status': ?1}")
    List<MongoTransaction> findByParkingLotIdAndStatus(String parkingLotId, MongoTransaction.TransactionStatus status);
    
    @Query("{'user_id': ?0, 'status': ?1}")
    List<MongoTransaction> findByUserIdAndStatus(String userId, MongoTransaction.TransactionStatus status);
    
    @Query("{'entry_time': {$gte: ?0}}")
    List<MongoTransaction> findActiveTransactions(LocalDateTime since);
    
    @Query("{'exit_time': null}")
    List<MongoTransaction> findOngoingTransactions();
} 