package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoTransaction;
import com.docomo.admin.repository.mongo.MongoTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MongoTransactionService {
    
    @Autowired
    private MongoTransactionRepository transactionRepository;
    
    public List<MongoTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<MongoTransaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }
    
    public List<MongoTransaction> getTransactionsByParkingLotId(String parkingLotId) {
        return transactionRepository.findByParkingLotId(parkingLotId);
    }
    
    public List<MongoTransaction> getTransactionsByUserId(String userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<MongoTransaction> getTransactionsByStatus(MongoTransaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
    
    public List<MongoTransaction> getTransactionsByPaymentMethod(MongoTransaction.PaymentMethod paymentMethod) {
        return transactionRepository.findByPaymentMethod(paymentMethod);
    }
    
    public List<MongoTransaction> searchTransactionsByLicensePlate(String licensePlate) {
        return transactionRepository.findByLicensePlateContainingIgnoreCase(licensePlate);
    }
    
    public List<MongoTransaction> getTransactionsByEntryTimeRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByEntryTimeBetween(start, end);
    }
    
    public List<MongoTransaction> getTransactionsByExitTimeRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByExitTimeBetween(start, end);
    }
    
    public List<MongoTransaction> getTransactionsByCreatedAtRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByCreatedAtBetween(start, end);
    }
    
    public List<MongoTransaction> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        List<MongoTransaction> minTransactions = transactionRepository.findByAmountGreaterThanOrEqualTo(minAmount);
        return minTransactions.stream()
            .filter(t -> t.getAmount().compareTo(maxAmount) <= 0)
            .toList();
    }
    
    public List<MongoTransaction> getTransactionsByParkingLotAndStatus(String parkingLotId, MongoTransaction.TransactionStatus status) {
        return transactionRepository.findByParkingLotIdAndStatus(parkingLotId, status);
    }
    
    public List<MongoTransaction> getTransactionsByUserAndStatus(String userId, MongoTransaction.TransactionStatus status) {
        return transactionRepository.findByUserIdAndStatus(userId, status);
    }
    
    public List<MongoTransaction> getActiveTransactions(LocalDateTime since) {
        return transactionRepository.findActiveTransactions(since);
    }
    
    public List<MongoTransaction> getOngoingTransactions() {
        return transactionRepository.findOngoingTransactions();
    }
    
    public MongoTransaction createTransaction(MongoTransaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
    
    public MongoTransaction updateTransaction(String id, MongoTransaction transactionDetails) {
        MongoTransaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        transaction.setParkingLotId(transactionDetails.getParkingLotId());
        transaction.setUserId(transactionDetails.getUserId());
        transaction.setLicensePlate(transactionDetails.getLicensePlate());
        transaction.setEntryTime(transactionDetails.getEntryTime());
        transaction.setExitTime(transactionDetails.getExitTime());
        transaction.setDurationHours(transactionDetails.getDurationHours());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setStatus(transactionDetails.getStatus());
        transaction.setPaymentMethod(transactionDetails.getPaymentMethod());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    public MongoTransaction completeTransaction(String id, LocalDateTime exitTime, BigDecimal amount) {
        MongoTransaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        transaction.setExitTime(exitTime);
        transaction.setAmount(amount);
        transaction.setStatus(MongoTransaction.TransactionStatus.COMPLETED);
        
        if (transaction.getEntryTime() != null && exitTime != null) {
            double durationHours = ChronoUnit.MINUTES.between(transaction.getEntryTime(), exitTime) / 60.0;
            transaction.setDurationHours(durationHours);
        }
        
        transaction.setUpdatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    public MongoTransaction cancelTransaction(String id) {
        MongoTransaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        transaction.setStatus(MongoTransaction.TransactionStatus.CANCELLED);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    public void deleteTransaction(String id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }
    
    public boolean existsById(String id) {
        return transactionRepository.existsById(id);
    }
} 