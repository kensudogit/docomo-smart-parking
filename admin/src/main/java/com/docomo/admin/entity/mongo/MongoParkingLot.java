package com.docomo.admin.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "parking_lots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MongoParkingLot {
    
    @Id
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("address")
    private String address;
    
    @Field("total_spaces")
    private Integer totalSpaces;
    
    @Field("available_spaces")
    private Integer availableSpaces;
    
    @Field("hourly_rate")
    private BigDecimal hourlyRate;
    
    @Field("daily_rate")
    private BigDecimal dailyRate;
    
    @Field("status")
    private ParkingLotStatus status;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    public enum ParkingLotStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
    
    public MongoParkingLot(String name, String address, Integer totalSpaces, 
                          BigDecimal hourlyRate, BigDecimal dailyRate, ParkingLotStatus status) {
        this.name = name;
        this.address = address;
        this.totalSpaces = totalSpaces;
        this.availableSpaces = totalSpaces;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 