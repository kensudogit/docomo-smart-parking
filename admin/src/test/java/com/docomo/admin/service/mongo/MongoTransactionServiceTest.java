package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoTransaction;
import com.docomo.admin.repository.mongo.MongoTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class MongoTransactionServiceTest {
    
    @Autowired
    private MongoTransactionService transactionService;
    
    @Autowired
    private MongoTransactionRepository transactionRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private MongoTransaction testTransaction1;
    private MongoTransaction testTransaction2;
    private MongoTransaction testTransaction3;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        // Clear the collection before each test
        mongoTemplate.dropCollection("transactions");
        
        baseTime = LocalDateTime.now();
        
        // Create test transactions
        testTransaction1 = new MongoTransaction("parking-lot-1", "user-1", "ABC-123", 
            baseTime, MongoTransaction.TransactionStatus.COMPLETED, MongoTransaction.PaymentMethod.CREDIT_CARD);
        testTransaction1.setExitTime(baseTime.plusHours(2));
        testTransaction1.setDurationHours(2.0);
        testTransaction1.setAmount(new BigDecimal("1000.00"));
        
        testTransaction2 = new MongoTransaction("parking-lot-1", "user-2", "XYZ-789", 
            baseTime.plusHours(1), MongoTransaction.TransactionStatus.PENDING, MongoTransaction.PaymentMethod.CASH);
        
        testTransaction3 = new MongoTransaction("parking-lot-2", "user-3", "DEF-456", 
            baseTime.minusHours(1), MongoTransaction.TransactionStatus.CANCELLED, MongoTransaction.PaymentMethod.MOBILE_PAYMENT);
        testTransaction3.setAmount(new BigDecimal("500.00"));
        
        // Save test transactions
        testTransaction1 = transactionRepository.save(testTransaction1);
        testTransaction2 = transactionRepository.save(testTransaction2);
        testTransaction3 = transactionRepository.save(testTransaction3);
    }
    
    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("transactions");
    }
    
    @Test
    void testGetAllTransactions() {
        List<MongoTransaction> transactions = transactionService.getAllTransactions();
        assertEquals(3, transactions.size());
    }
    
    @Test
    void testGetTransactionById() {
        Optional<MongoTransaction> foundTransaction = transactionService.getTransactionById(testTransaction1.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals("ABC-123", foundTransaction.get().getLicensePlate());
        assertEquals("parking-lot-1", foundTransaction.get().getParkingLotId());
    }
    
    @Test
    void testGetTransactionsByParkingLotId() {
        List<MongoTransaction> transactions = transactionService.getTransactionsByParkingLotId("parking-lot-1");
        assertEquals(2, transactions.size());
        
        List<MongoTransaction> transactions2 = transactionService.getTransactionsByParkingLotId("parking-lot-2");
        assertEquals(1, transactions2.size());
        assertEquals("DEF-456", transactions2.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByUserId() {
        List<MongoTransaction> transactions = transactionService.getTransactionsByUserId("user-1");
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> transactions2 = transactionService.getTransactionsByUserId("user-2");
        assertEquals(1, transactions2.size());
        assertEquals("XYZ-789", transactions2.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByStatus() {
        List<MongoTransaction> completedTransactions = transactionService.getTransactionsByStatus(MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, completedTransactions.size());
        assertEquals("ABC-123", completedTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionService.getTransactionsByStatus(MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> cancelledTransactions = transactionService.getTransactionsByStatus(MongoTransaction.TransactionStatus.CANCELLED);
        assertEquals(1, cancelledTransactions.size());
        assertEquals("DEF-456", cancelledTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByPaymentMethod() {
        List<MongoTransaction> creditCardTransactions = transactionService.getTransactionsByPaymentMethod(MongoTransaction.PaymentMethod.CREDIT_CARD);
        assertEquals(1, creditCardTransactions.size());
        assertEquals("ABC-123", creditCardTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> cashTransactions = transactionService.getTransactionsByPaymentMethod(MongoTransaction.PaymentMethod.CASH);
        assertEquals(1, cashTransactions.size());
        assertEquals("XYZ-789", cashTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> mobileTransactions = transactionService.getTransactionsByPaymentMethod(MongoTransaction.PaymentMethod.MOBILE_PAYMENT);
        assertEquals(1, mobileTransactions.size());
        assertEquals("DEF-456", mobileTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testSearchTransactionsByLicensePlate() {
        List<MongoTransaction> transactions = transactionService.searchTransactionsByLicensePlate("ABC");
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByEntryTimeRange() {
        LocalDateTime start = baseTime.minusMinutes(30);
        LocalDateTime end = baseTime.plusMinutes(30);
        
        List<MongoTransaction> transactions = transactionService.getTransactionsByEntryTimeRange(start, end);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByExitTimeRange() {
        LocalDateTime start = baseTime.plusHours(1);
        LocalDateTime end = baseTime.plusHours(3);
        
        List<MongoTransaction> transactions = transactionService.getTransactionsByExitTimeRange(start, end);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByCreatedAtRange() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);
        
        List<MongoTransaction> transactions = transactionService.getTransactionsByCreatedAtRange(start, end);
        assertEquals(3, transactions.size());
    }
    
    @Test
    void testGetTransactionsByAmountRange() {
        List<MongoTransaction> transactions = transactionService.getTransactionsByAmountRange(new BigDecimal("500.00"), new BigDecimal("1000.00"));
        assertEquals(2, transactions.size());
        
        List<MongoTransaction> expensiveTransactions = transactionService.getTransactionsByAmountRange(new BigDecimal("1000.00"), new BigDecimal("1500.00"));
        assertEquals(1, expensiveTransactions.size());
        assertEquals("ABC-123", expensiveTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByParkingLotAndStatus() {
        List<MongoTransaction> transactions = transactionService.getTransactionsByParkingLotAndStatus("parking-lot-1", MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionService.getTransactionsByParkingLotAndStatus("parking-lot-1", MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetTransactionsByUserAndStatus() {
        List<MongoTransaction> transactions = transactionService.getTransactionsByUserAndStatus("user-1", MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionService.getTransactionsByUserAndStatus("user-2", MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testGetActiveTransactions() {
        LocalDateTime since = baseTime.minusHours(2);
        List<MongoTransaction> transactions = transactionService.getActiveTransactions(since);
        assertEquals(2, transactions.size());
    }
    
    @Test
    void testGetOngoingTransactions() {
        List<MongoTransaction> transactions = transactionService.getOngoingTransactions();
        assertEquals(2, transactions.size()); // testTransaction2 and testTransaction3 have no exit time
    }
    
    @Test
    void testCreateTransaction() {
        MongoTransaction newTransaction = new MongoTransaction("parking-lot-3", "user-4", "GHI-789", 
            LocalDateTime.now(), MongoTransaction.TransactionStatus.PENDING, MongoTransaction.PaymentMethod.SUBSCRIPTION);
        
        MongoTransaction createdTransaction = transactionService.createTransaction(newTransaction);
        
        assertNotNull(createdTransaction.getId());
        assertEquals("GHI-789", createdTransaction.getLicensePlate());
        assertEquals("parking-lot-3", createdTransaction.getParkingLotId());
        assertNotNull(createdTransaction.getCreatedAt());
        assertNotNull(createdTransaction.getUpdatedAt());
        
        // Verify it was actually saved
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(createdTransaction.getId());
        assertTrue(foundTransaction.isPresent());
    }
    
    @Test
    void testUpdateTransaction() {
        MongoTransaction updateDetails = new MongoTransaction();
        updateDetails.setParkingLotId("parking-lot-3");
        updateDetails.setUserId("user-4");
        updateDetails.setLicensePlate("UPDATED-123");
        updateDetails.setEntryTime(baseTime.plusHours(3));
        updateDetails.setExitTime(baseTime.plusHours(5));
        updateDetails.setDurationHours(2.0);
        updateDetails.setAmount(new BigDecimal("1500.00"));
        updateDetails.setStatus(MongoTransaction.TransactionStatus.REFUNDED);
        updateDetails.setPaymentMethod(MongoTransaction.PaymentMethod.SUBSCRIPTION);
        
        MongoTransaction updatedTransaction = transactionService.updateTransaction(testTransaction1.getId(), updateDetails);
        
        assertEquals("parking-lot-3", updatedTransaction.getParkingLotId());
        assertEquals("user-4", updatedTransaction.getUserId());
        assertEquals("UPDATED-123", updatedTransaction.getLicensePlate());
        assertEquals(baseTime.plusHours(3), updatedTransaction.getEntryTime());
        assertEquals(baseTime.plusHours(5), updatedTransaction.getExitTime());
        assertEquals(2.0, updatedTransaction.getDurationHours());
        assertEquals(new BigDecimal("1500.00"), updatedTransaction.getAmount());
        assertEquals(MongoTransaction.TransactionStatus.REFUNDED, updatedTransaction.getStatus());
        assertEquals(MongoTransaction.PaymentMethod.SUBSCRIPTION, updatedTransaction.getPaymentMethod());
        assertNotNull(updatedTransaction.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction1.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals("UPDATED-123", foundTransaction.get().getLicensePlate());
    }
    
    @Test
    void testUpdateTransactionNotFound() {
        MongoTransaction updateDetails = new MongoTransaction();
        updateDetails.setLicensePlate("UPDATED-123");
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.updateTransaction("nonexistent-id", updateDetails);
        });
    }
    
    @Test
    void testCompleteTransaction() {
        LocalDateTime exitTime = baseTime.plusHours(3);
        BigDecimal amount = new BigDecimal("1500.00");
        
        MongoTransaction completedTransaction = transactionService.completeTransaction(testTransaction2.getId(), exitTime, amount);
        
        assertEquals(exitTime, completedTransaction.getExitTime());
        assertEquals(amount, completedTransaction.getAmount());
        assertEquals(MongoTransaction.TransactionStatus.COMPLETED, completedTransaction.getStatus());
        assertEquals(2.0, completedTransaction.getDurationHours()); // 2 hours difference
        assertNotNull(completedTransaction.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction2.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals(MongoTransaction.TransactionStatus.COMPLETED, foundTransaction.get().getStatus());
    }
    
    @Test
    void testCompleteTransactionNotFound() {
        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("1000.00");
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.completeTransaction("nonexistent-id", exitTime, amount);
        });
    }
    
    @Test
    void testCancelTransaction() {
        MongoTransaction cancelledTransaction = transactionService.cancelTransaction(testTransaction2.getId());
        
        assertEquals(MongoTransaction.TransactionStatus.CANCELLED, cancelledTransaction.getStatus());
        assertNotNull(cancelledTransaction.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction2.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals(MongoTransaction.TransactionStatus.CANCELLED, foundTransaction.get().getStatus());
    }
    
    @Test
    void testCancelTransactionNotFound() {
        assertThrows(RuntimeException.class, () -> {
            transactionService.cancelTransaction("nonexistent-id");
        });
    }
    
    @Test
    void testDeleteTransaction() {
        transactionService.deleteTransaction(testTransaction1.getId());
        
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction1.getId());
        assertFalse(foundTransaction.isPresent());
        
        List<MongoTransaction> remainingTransactions = transactionService.getAllTransactions();
        assertEquals(2, remainingTransactions.size());
    }
    
    @Test
    void testDeleteTransactionNotFound() {
        assertThrows(RuntimeException.class, () -> {
            transactionService.deleteTransaction("nonexistent-id");
        });
    }
    
    @Test
    void testExistsById() {
        assertTrue(transactionService.existsById(testTransaction1.getId()));
        assertTrue(transactionService.existsById(testTransaction2.getId()));
        assertFalse(transactionService.existsById("nonexistent-id"));
    }
} 