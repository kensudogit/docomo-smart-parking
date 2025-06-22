package com.docomo.admin.service;

import com.docomo.admin.entity.ParkingLot;
import com.docomo.admin.entity.ParkingLot.ParkingLotStatus;
import com.docomo.admin.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingLotService {
    
    private final ParkingLotRepository parkingLotRepository;
    
    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }
    
    public Optional<ParkingLot> getParkingLotById(Long id) {
        return parkingLotRepository.findById(id);
    }
    
    public List<ParkingLot> getParkingLotsByStatus(ParkingLotStatus status) {
        return parkingLotRepository.findByStatus(status);
    }
    
    public List<ParkingLot> searchParkingLotsByName(String name) {
        return parkingLotRepository.findByNameContainingIgnoreCase(name);
    }
    
    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        if (parkingLot.getStatus() == null) {
            parkingLot.setStatus(ParkingLotStatus.ACTIVE);
        }
        if (parkingLot.getAvailableSpaces() == null) {
            parkingLot.setAvailableSpaces(parkingLot.getTotalSpaces());
        }
        return parkingLotRepository.save(parkingLot);
    }
    
    public ParkingLot updateParkingLot(Long id, ParkingLot parkingLotDetails) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        parkingLot.setName(parkingLotDetails.getName());
        parkingLot.setAddress(parkingLotDetails.getAddress());
        parkingLot.setTotalSpaces(parkingLotDetails.getTotalSpaces());
        parkingLot.setAvailableSpaces(parkingLotDetails.getAvailableSpaces());
        parkingLot.setHourlyRate(parkingLotDetails.getHourlyRate());
        parkingLot.setDailyRate(parkingLotDetails.getDailyRate());
        parkingLot.setStatus(parkingLotDetails.getStatus());
        
        return parkingLotRepository.save(parkingLot);
    }
    
    public void deleteParkingLot(Long id) {
        parkingLotRepository.deleteById(id);
    }
    
    public ParkingLot updateParkingLotStatus(Long id, ParkingLotStatus status) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        parkingLot.setStatus(status);
        return parkingLotRepository.save(parkingLot);
    }
    
    public ParkingLot updateAvailableSpaces(Long id, Integer availableSpaces) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        
        if (availableSpaces > parkingLot.getTotalSpaces()) {
            throw new RuntimeException("Available spaces cannot exceed total spaces");
        }
        
        parkingLot.setAvailableSpaces(availableSpaces);
        return parkingLotRepository.save(parkingLot);
    }
} 