package com.docomo.admin.service;

import com.docomo.admin.entity.Transaction;
import com.docomo.admin.entity.Transaction.TransactionStatus;
import com.docomo.admin.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> getTransactionsByParkingLot(Long parkingLotId) {
        return transactionRepository.findByParkingLotId(parkingLotId);
    }
    
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByEntryTimeBetween(start, end);
    }
    
    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getStatus() == null) {
            transaction.setStatus(TransactionStatus.PENDING);
        }
        return transactionRepository.save(transaction);
    }
    
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        transaction.setExitTime(transactionDetails.getExitTime());
        transaction.setDurationHours(transactionDetails.getDurationHours());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setStatus(transactionDetails.getStatus());
        transaction.setPaymentMethod(transactionDetails.getPaymentMethod());
        
        return transactionRepository.save(transaction);
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
    
    public BigDecimal getTotalRevenueByParkingLot(Long parkingLotId) {
        BigDecimal revenue = transactionRepository.getTotalRevenueByParkingLot(parkingLotId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = transactionRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalRevenue() {
        return getTotalRevenueByDateRange(
            LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0),
            LocalDateTime.now()
        );
    }
    
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        return getTotalRevenueByDateRange(startOfMonth, LocalDateTime.now());
    }
} 