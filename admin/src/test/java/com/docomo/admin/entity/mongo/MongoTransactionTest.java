package com.docomo.admin.entity.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

class MongoTransactionTest {
    
    private MongoTransaction transaction;
    
    @BeforeEach
    void setUp() {
        transaction = new MongoTransaction();
        transaction.setId("test-id");
        transaction.setParkingLotId("parking-lot-1");
        transaction.setUserId("user-1");
        transaction.setLicensePlate("ABC-123");
        transaction.setEntryTime(LocalDateTime.now());
        transaction.setExitTime(LocalDateTime.now().plusHours(2));
        transaction.setDurationHours(2.0);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus(MongoTransaction.TransactionStatus.COMPLETED);
        transaction.setPaymentMethod(MongoTransaction.PaymentMethod.CREDIT_CARD);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testMongoTransactionCreation() {
        assertNotNull(transaction);
        assertEquals("test-id", transaction.getId());
        assertEquals("parking-lot-1", transaction.getParkingLotId());
        assertEquals("user-1", transaction.getUserId());
        assertEquals("ABC-123", transaction.getLicensePlate());
        assertNotNull(transaction.getEntryTime());
        assertNotNull(transaction.getExitTime());
        assertEquals(2.0, transaction.getDurationHours());
        assertEquals(new BigDecimal("1000.00"), transaction.getAmount());
        assertEquals(MongoTransaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(MongoTransaction.PaymentMethod.CREDIT_CARD, transaction.getPaymentMethod());
        assertNotNull(transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());
    }
    
    @Test
    void testMongoTransactionConstructor() {
        LocalDateTime entryTime = LocalDateTime.now();
        MongoTransaction constructorTransaction = new MongoTransaction(
            "parking-lot-2",
            "user-2",
            "XYZ-789",
            entryTime,
            MongoTransaction.TransactionStatus.PENDING,
            MongoTransaction.PaymentMethod.CASH
        );
        
        assertEquals("parking-lot-2", constructorTransaction.getParkingLotId());
        assertEquals("user-2", constructorTransaction.getUserId());
        assertEquals("XYZ-789", constructorTransaction.getLicensePlate());
        assertEquals(entryTime, constructorTransaction.getEntryTime());
        assertEquals(MongoTransaction.TransactionStatus.PENDING, constructorTransaction.getStatus());
        assertEquals(MongoTransaction.PaymentMethod.CASH, constructorTransaction.getPaymentMethod());
        assertNotNull(constructorTransaction.getCreatedAt());
        assertNotNull(constructorTransaction.getUpdatedAt());
    }
    
    @Test
    void testMongoTransactionSettersAndGetters() {
        transaction.setId("new-id");
        transaction.setParkingLotId("new-parking-lot");
        transaction.setUserId("new-user");
        transaction.setLicensePlate("NEW-456");
        transaction.setAmount(new BigDecimal("1500.00"));
        transaction.setStatus(MongoTransaction.TransactionStatus.CANCELLED);
        transaction.setPaymentMethod(MongoTransaction.PaymentMethod.MOBILE_PAYMENT);
        
        LocalDateTime now = LocalDateTime.now();
        transaction.setEntryTime(now);
        transaction.setExitTime(now.plusHours(3));
        transaction.setDurationHours(3.0);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        
        assertEquals("new-id", transaction.getId());
        assertEquals("new-parking-lot", transaction.getParkingLotId());
        assertEquals("new-user", transaction.getUserId());
        assertEquals("NEW-456", transaction.getLicensePlate());
        assertEquals(now, transaction.getEntryTime());
        assertEquals(now.plusHours(3), transaction.getExitTime());
        assertEquals(3.0, transaction.getDurationHours());
        assertEquals(new BigDecimal("1500.00"), transaction.getAmount());
        assertEquals(MongoTransaction.TransactionStatus.CANCELLED, transaction.getStatus());
        assertEquals(MongoTransaction.PaymentMethod.MOBILE_PAYMENT, transaction.getPaymentMethod());
        assertEquals(now, transaction.getCreatedAt());
        assertEquals(now, transaction.getUpdatedAt());
    }
    
    @Test
    void testTransactionStatusEnum() {
        assertEquals("PENDING", MongoTransaction.TransactionStatus.PENDING.name());
        assertEquals("COMPLETED", MongoTransaction.TransactionStatus.COMPLETED.name());
        assertEquals("CANCELLED", MongoTransaction.TransactionStatus.CANCELLED.name());
        assertEquals("REFUNDED", MongoTransaction.TransactionStatus.REFUNDED.name());
        
        assertEquals(0, MongoTransaction.TransactionStatus.PENDING.ordinal());
        assertEquals(1, MongoTransaction.TransactionStatus.COMPLETED.ordinal());
        assertEquals(2, MongoTransaction.TransactionStatus.CANCELLED.ordinal());
        assertEquals(3, MongoTransaction.TransactionStatus.REFUNDED.ordinal());
    }
    
    @Test
    void testPaymentMethodEnum() {
        assertEquals("CASH", MongoTransaction.PaymentMethod.CASH.name());
        assertEquals("CREDIT_CARD", MongoTransaction.PaymentMethod.CREDIT_CARD.name());
        assertEquals("MOBILE_PAYMENT", MongoTransaction.PaymentMethod.MOBILE_PAYMENT.name());
        assertEquals("SUBSCRIPTION", MongoTransaction.PaymentMethod.SUBSCRIPTION.name());
        
        assertEquals(0, MongoTransaction.PaymentMethod.CASH.ordinal());
        assertEquals(1, MongoTransaction.PaymentMethod.CREDIT_CARD.ordinal());
        assertEquals(2, MongoTransaction.PaymentMethod.MOBILE_PAYMENT.ordinal());
        assertEquals(3, MongoTransaction.PaymentMethod.SUBSCRIPTION.ordinal());
    }
    
    @Test
    void testMongoTransactionEquality() {
        MongoTransaction transaction1 = new MongoTransaction();
        transaction1.setId("same-id");
        transaction1.setLicensePlate("SAME-123");
        
        MongoTransaction transaction2 = new MongoTransaction();
        transaction2.setId("same-id");
        transaction2.setLicensePlate("SAME-123");
        
        assertEquals(transaction1.getId(), transaction2.getId());
        assertEquals(transaction1.getLicensePlate(), transaction2.getLicensePlate());
    }
    
    @Test
    void testMongoTransactionToString() {
        String transactionString = transaction.toString();
        assertNotNull(transactionString);
        assertTrue(transactionString.contains("ABC-123"));
        assertTrue(transactionString.contains("parking-lot-1"));
    }
    
    @Test
    void testDurationCalculation() {
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusHours(2).plusMinutes(30);
        
        transaction.setEntryTime(entryTime);
        transaction.setExitTime(exitTime);
        transaction.setDurationHours(2.5);
        
        assertEquals(entryTime, transaction.getEntryTime());
        assertEquals(exitTime, transaction.getExitTime());
        assertEquals(2.5, transaction.getDurationHours());
    }
    
    @Test
    void testNullValues() {
        MongoTransaction nullTransaction = new MongoTransaction();
        nullTransaction.setId("null-test");
        
        assertNull(nullTransaction.getParkingLotId());
        assertNull(nullTransaction.getUserId());
        assertNull(nullTransaction.getLicensePlate());
        assertNull(nullTransaction.getEntryTime());
        assertNull(nullTransaction.getExitTime());
        assertNull(nullTransaction.getDurationHours());
        assertNull(nullTransaction.getAmount());
        assertNull(nullTransaction.getStatus());
        assertNull(nullTransaction.getPaymentMethod());
    }
} 