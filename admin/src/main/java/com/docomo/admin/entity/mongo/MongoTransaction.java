package com.docomo.admin.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MongoTransaction {
    
    @Id
    private String id;
    
    @Field("parking_lot_id")
    private String parkingLotId;
    
    @Field("user_id")
    private String userId;
    
    @Field("license_plate")
    private String licensePlate;
    
    @Field("entry_time")
    private LocalDateTime entryTime;
    
    @Field("exit_time")
    private LocalDateTime exitTime;
    
    @Field("duration_hours")
    private Double durationHours;
    
    @Field("amount")
    private BigDecimal amount;
    
    @Field("status")
    private TransactionStatus status;
    
    @Field("payment_method")
    private PaymentMethod paymentMethod;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    public enum TransactionStatus {
        PENDING, COMPLETED, CANCELLED, REFUNDED
    }
    
    public enum PaymentMethod {
        CASH, CREDIT_CARD, MOBILE_PAYMENT, SUBSCRIPTION
    }
    
    public MongoTransaction(String parkingLotId, String userId, String licensePlate, 
                           LocalDateTime entryTime, TransactionStatus status, PaymentMethod paymentMethod) {
        this.parkingLotId = parkingLotId;
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 