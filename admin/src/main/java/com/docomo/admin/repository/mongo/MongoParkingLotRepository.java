package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoParkingLot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MongoParkingLotRepository extends MongoRepository<MongoParkingLot, String> {
    
    List<MongoParkingLot> findByStatus(MongoParkingLot.ParkingLotStatus status);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<MongoParkingLot> findByNameContainingIgnoreCase(String name);
    
    @Query("{'address': {$regex: ?0, $options: 'i'}}")
    List<MongoParkingLot> findByAddressContainingIgnoreCase(String address);
    
    List<MongoParkingLot> findByAvailableSpacesGreaterThan(Integer spaces);
    
    @Query("{'hourly_rate': {$lte: ?0}}")
    List<MongoParkingLot> findByHourlyRateLessThanOrEqualTo(BigDecimal rate);
    
    @Query("{'daily_rate': {$lte: ?0}}")
    List<MongoParkingLot> findByDailyRateLessThanOrEqualTo(BigDecimal rate);
    
    @Query("{'total_spaces': {$gte: ?0}}")
    List<MongoParkingLot> findByTotalSpacesGreaterThanOrEqualTo(Integer totalSpaces);
    
    @Query("{'available_spaces': {$gt: 0}}")
    List<MongoParkingLot> findAvailableParkingLots();
    
    @Query("{'status': 'ACTIVE', 'available_spaces': {$gt: 0}}")
    List<MongoParkingLot> findActiveAvailableParkingLots();
} 