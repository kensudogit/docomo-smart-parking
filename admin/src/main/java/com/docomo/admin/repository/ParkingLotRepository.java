package com.docomo.admin.repository;

import com.docomo.admin.entity.ParkingLot;
import com.docomo.admin.entity.ParkingLot.ParkingLotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByStatus(ParkingLotStatus status);
    List<ParkingLot> findByNameContainingIgnoreCase(String name);
} 