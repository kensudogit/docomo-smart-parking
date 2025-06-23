package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoParkingLot;
import com.docomo.admin.repository.mongo.MongoParkingLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MongoParkingLotService {
    
    @Autowired
    private MongoParkingLotRepository parkingLotRepository;
    
    public List<MongoParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }
    
    public Optional<MongoParkingLot> getParkingLotById(String id) {
        return parkingLotRepository.findById(id);
    }
    
    public List<MongoParkingLot> getParkingLotsByStatus(MongoParkingLot.ParkingLotStatus status) {
        return parkingLotRepository.findByStatus(status);
    }
    
    public List<MongoParkingLot> searchParkingLotsByName(String name) {
        return parkingLotRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<MongoParkingLot> searchParkingLotsByAddress(String address) {
        return parkingLotRepository.findByAddressContainingIgnoreCase(address);
    }
    
    public List<MongoParkingLot> getParkingLotsWithAvailableSpaces(Integer minSpaces) {
        return parkingLotRepository.findByAvailableSpacesGreaterThan(minSpaces);
    }
    
    public List<MongoParkingLot> getParkingLotsByHourlyRate(BigDecimal maxRate) {
        return parkingLotRepository.findByHourlyRateLessThanOrEqualTo(maxRate);
    }
    
    public List<MongoParkingLot> getParkingLotsByDailyRate(BigDecimal maxRate) {
        return parkingLotRepository.findByDailyRateLessThanOrEqualTo(maxRate);
    }
    
    public List<MongoParkingLot> getParkingLotsByTotalSpaces(Integer minSpaces) {
        return parkingLotRepository.findByTotalSpacesGreaterThanOrEqualTo(minSpaces);
    }
    
    public List<MongoParkingLot> getAvailableParkingLots() {
        return parkingLotRepository.findAvailableParkingLots();
    }
    
    public List<MongoParkingLot> getActiveAvailableParkingLots() {
        return parkingLotRepository.findActiveAvailableParkingLots();
    }
    
    public MongoParkingLot createParkingLot(MongoParkingLot parkingLot) {
        parkingLot.setCreatedAt(LocalDateTime.now());
        parkingLot.setUpdatedAt(LocalDateTime.now());
        return parkingLotRepository.save(parkingLot);
    }
    
    public MongoParkingLot updateParkingLot(String id, MongoParkingLot parkingLotDetails) {
        MongoParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        parkingLot.setName(parkingLotDetails.getName());
        parkingLot.setAddress(parkingLotDetails.getAddress());
        parkingLot.setTotalSpaces(parkingLotDetails.getTotalSpaces());
        parkingLot.setAvailableSpaces(parkingLotDetails.getAvailableSpaces());
        parkingLot.setHourlyRate(parkingLotDetails.getHourlyRate());
        parkingLot.setDailyRate(parkingLotDetails.getDailyRate());
        parkingLot.setStatus(parkingLotDetails.getStatus());
        parkingLot.setUpdatedAt(LocalDateTime.now());
        
        return parkingLotRepository.save(parkingLot);
    }
    
    public MongoParkingLot updateAvailableSpaces(String id, Integer availableSpaces) {
        MongoParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        parkingLot.setAvailableSpaces(availableSpaces);
        parkingLot.setUpdatedAt(LocalDateTime.now());
        
        return parkingLotRepository.save(parkingLot);
    }
    
    public MongoParkingLot updateStatus(String id, MongoParkingLot.ParkingLotStatus status) {
        MongoParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        parkingLot.setStatus(status);
        parkingLot.setUpdatedAt(LocalDateTime.now());
        
        return parkingLotRepository.save(parkingLot);
    }
    
    public void deleteParkingLot(String id) {
        if (!parkingLotRepository.existsById(id)) {
            throw new RuntimeException("Parking lot not found");
        }
        parkingLotRepository.deleteById(id);
    }
    
    public boolean existsById(String id) {
        return parkingLotRepository.existsById(id);
    }
} 