package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataMongoTest
class MongoTransactionRepositoryTest {
    
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
    void testFindAll() {
        List<MongoTransaction> transactions = transactionRepository.findAll();
        assertEquals(3, transactions.size());
    }
    
    @Test
    void testFindById() {
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction1.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals("ABC-123", foundTransaction.get().getLicensePlate());
        assertEquals("parking-lot-1", foundTransaction.get().getParkingLotId());
    }
    
    @Test
    void testFindByParkingLotId() {
        List<MongoTransaction> transactions = transactionRepository.findByParkingLotId("parking-lot-1");
        assertEquals(2, transactions.size());
        
        List<MongoTransaction> transactions2 = transactionRepository.findByParkingLotId("parking-lot-2");
        assertEquals(1, transactions2.size());
        assertEquals("DEF-456", transactions2.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByUserId() {
        List<MongoTransaction> transactions = transactionRepository.findByUserId("user-1");
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> transactions2 = transactionRepository.findByUserId("user-2");
        assertEquals(1, transactions2.size());
        assertEquals("XYZ-789", transactions2.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByStatus() {
        List<MongoTransaction> completedTransactions = transactionRepository.findByStatus(MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, completedTransactions.size());
        assertEquals("ABC-123", completedTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionRepository.findByStatus(MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> cancelledTransactions = transactionRepository.findByStatus(MongoTransaction.TransactionStatus.CANCELLED);
        assertEquals(1, cancelledTransactions.size());
        assertEquals("DEF-456", cancelledTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByPaymentMethod() {
        List<MongoTransaction> creditCardTransactions = transactionRepository.findByPaymentMethod(MongoTransaction.PaymentMethod.CREDIT_CARD);
        assertEquals(1, creditCardTransactions.size());
        assertEquals("ABC-123", creditCardTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> cashTransactions = transactionRepository.findByPaymentMethod(MongoTransaction.PaymentMethod.CASH);
        assertEquals(1, cashTransactions.size());
        assertEquals("XYZ-789", cashTransactions.get(0).getLicensePlate());
        
        List<MongoTransaction> mobileTransactions = transactionRepository.findByPaymentMethod(MongoTransaction.PaymentMethod.MOBILE_PAYMENT);
        assertEquals(1, mobileTransactions.size());
        assertEquals("DEF-456", mobileTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByLicensePlateContainingIgnoreCase() {
        List<MongoTransaction> transactions = transactionRepository.findByLicensePlateContainingIgnoreCase("ABC");
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        // Test case insensitive
        List<MongoTransaction> transactionsCaseInsensitive = transactionRepository.findByLicensePlateContainingIgnoreCase("abc");
        assertEquals(1, transactionsCaseInsensitive.size());
    }
    
    @Test
    void testFindByEntryTimeBetween() {
        LocalDateTime start = baseTime.minusMinutes(30);
        LocalDateTime end = baseTime.plusMinutes(30);
        
        List<MongoTransaction> transactions = transactionRepository.findByEntryTimeBetween(start, end);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByExitTimeBetween() {
        LocalDateTime start = baseTime.plusHours(1);
        LocalDateTime end = baseTime.plusHours(3);
        
        List<MongoTransaction> transactions = transactionRepository.findByExitTimeBetween(start, end);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);
        
        List<MongoTransaction> transactions = transactionRepository.findByCreatedAtBetween(start, end);
        assertEquals(3, transactions.size());
    }
    
    @Test
    void testFindByAmountGreaterThanOrEqualTo() {
        List<MongoTransaction> transactions = transactionRepository.findByAmountGreaterThanOrEqualTo(new BigDecimal("500.00"));
        assertEquals(2, transactions.size());
        
        List<MongoTransaction> expensiveTransactions = transactionRepository.findByAmountGreaterThanOrEqualTo(new BigDecimal("1000.00"));
        assertEquals(1, expensiveTransactions.size());
        assertEquals("ABC-123", expensiveTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByAmountLessThanOrEqualTo() {
        List<MongoTransaction> transactions = transactionRepository.findByAmountLessThanOrEqualTo(new BigDecimal("1000.00"));
        assertEquals(2, transactions.size());
        
        List<MongoTransaction> cheapTransactions = transactionRepository.findByAmountLessThanOrEqualTo(new BigDecimal("500.00"));
        assertEquals(1, cheapTransactions.size());
        assertEquals("DEF-456", cheapTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByParkingLotIdAndStatus() {
        List<MongoTransaction> transactions = transactionRepository.findByParkingLotIdAndStatus("parking-lot-1", MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionRepository.findByParkingLotIdAndStatus("parking-lot-1", MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindByUserIdAndStatus() {
        List<MongoTransaction> transactions = transactionRepository.findByUserIdAndStatus("user-1", MongoTransaction.TransactionStatus.COMPLETED);
        assertEquals(1, transactions.size());
        assertEquals("ABC-123", transactions.get(0).getLicensePlate());
        
        List<MongoTransaction> pendingTransactions = transactionRepository.findByUserIdAndStatus("user-2", MongoTransaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals("XYZ-789", pendingTransactions.get(0).getLicensePlate());
    }
    
    @Test
    void testFindActiveTransactions() {
        LocalDateTime since = baseTime.minusHours(2);
        List<MongoTransaction> transactions = transactionRepository.findActiveTransactions(since);
        assertEquals(2, transactions.size());
    }
    
    @Test
    void testFindOngoingTransactions() {
        List<MongoTransaction> transactions = transactionRepository.findOngoingTransactions();
        assertEquals(2, transactions.size()); // testTransaction2 and testTransaction3 have no exit time
    }
    
    @Test
    void testSave() {
        MongoTransaction newTransaction = new MongoTransaction("parking-lot-3", "user-4", "GHI-789", 
            LocalDateTime.now(), MongoTransaction.TransactionStatus.PENDING, MongoTransaction.PaymentMethod.SUBSCRIPTION);
        
        MongoTransaction savedTransaction = transactionRepository.save(newTransaction);
        
        assertNotNull(savedTransaction.getId());
        assertEquals("GHI-789", savedTransaction.getLicensePlate());
        assertEquals("parking-lot-3", savedTransaction.getParkingLotId());
        
        // Verify it was actually saved
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(savedTransaction.getId());
        assertTrue(foundTransaction.isPresent());
    }
    
    @Test
    void testUpdate() {
        testTransaction1.setAmount(new BigDecimal("1500.00"));
        testTransaction1.setStatus(MongoTransaction.TransactionStatus.REFUNDED);
        MongoTransaction updatedTransaction = transactionRepository.save(testTransaction1);
        
        assertEquals(new BigDecimal("1500.00"), updatedTransaction.getAmount());
        assertEquals(MongoTransaction.TransactionStatus.REFUNDED, updatedTransaction.getStatus());
        
        // Verify the update was persisted
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction1.getId());
        assertTrue(foundTransaction.isPresent());
        assertEquals(new BigDecimal("1500.00"), foundTransaction.get().getAmount());
    }
    
    @Test
    void testDelete() {
        transactionRepository.deleteById(testTransaction1.getId());
        
        Optional<MongoTransaction> foundTransaction = transactionRepository.findById(testTransaction1.getId());
        assertFalse(foundTransaction.isPresent());
        
        List<MongoTransaction> remainingTransactions = transactionRepository.findAll();
        assertEquals(2, remainingTransactions.size());
    }
    
    @Test
    void testDeleteAll() {
        transactionRepository.deleteAll();
        
        List<MongoTransaction> transactions = transactionRepository.findAll();
        assertEquals(0, transactions.size());
    }
} 