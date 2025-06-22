package com.docomo.admin.repository;

import com.docomo.admin.entity.Transaction;
import com.docomo.admin.entity.Transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByParkingLotId(Long parkingLotId);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.parkingLot.id = :parkingLotId AND t.status = 'COMPLETED'")
    BigDecimal getTotalRevenueByParkingLot(@Param("parkingLotId") Long parkingLotId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.entryTime BETWEEN :startDate AND :endDate AND t.status = 'COMPLETED'")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 