package com.docomo.admin.service.mongo;

import com.docomo.admin.entity.mongo.MongoParkingLot;
import com.docomo.admin.repository.mongo.MongoParkingLotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class MongoParkingLotServiceTest {
    
    @Autowired
    private MongoParkingLotService parkingLotService;
    
    @Autowired
    private MongoParkingLotRepository parkingLotRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private MongoParkingLot testParkingLot1;
    private MongoParkingLot testParkingLot2;
    private MongoParkingLot testParkingLot3;
    
    @BeforeEach
    void setUp() {
        // Clear the collection before each test
        mongoTemplate.dropCollection("parking_lots");
        
        // Create test parking lots
        testParkingLot1 = new MongoParkingLot("Central Parking", "123 Main St", 100, 
            new BigDecimal("500.00"), new BigDecimal("5000.00"), MongoParkingLot.ParkingLotStatus.ACTIVE);
        testParkingLot1.setAvailableSpaces(50);
        
        testParkingLot2 = new MongoParkingLot("Downtown Parking", "456 Oak Ave", 200, 
            new BigDecimal("600.00"), new BigDecimal("6000.00"), MongoParkingLot.ParkingLotStatus.ACTIVE);
        testParkingLot2.setAvailableSpaces(0);
        
        testParkingLot3 = new MongoParkingLot("Suburban Parking", "789 Pine Rd", 150, 
            new BigDecimal("400.00"), new BigDecimal("4000.00"), MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        testParkingLot3.setAvailableSpaces(75);
        
        // Save test parking lots
        testParkingLot1 = parkingLotRepository.save(testParkingLot1);
        testParkingLot2 = parkingLotRepository.save(testParkingLot2);
        testParkingLot3 = parkingLotRepository.save(testParkingLot3);
    }
    
    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("parking_lots");
    }
    
    @Test
    void testGetAllParkingLots() {
        List<MongoParkingLot> parkingLots = parkingLotService.getAllParkingLots();
        assertEquals(3, parkingLots.size());
    }
    
    @Test
    void testGetParkingLotById() {
        Optional<MongoParkingLot> foundParkingLot = parkingLotService.getParkingLotById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals("Central Parking", foundParkingLot.get().getName());
        assertEquals("123 Main St", foundParkingLot.get().getAddress());
    }
    
    @Test
    void testGetParkingLotsByStatus() {
        List<MongoParkingLot> activeParkingLots = parkingLotService.getParkingLotsByStatus(MongoParkingLot.ParkingLotStatus.ACTIVE);
        assertEquals(2, activeParkingLots.size());
        
        List<MongoParkingLot> maintenanceParkingLots = parkingLotService.getParkingLotsByStatus(MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        assertEquals(1, maintenanceParkingLots.size());
        assertEquals("Suburban Parking", maintenanceParkingLots.get(0).getName());
    }
    
    @Test
    void testSearchParkingLotsByName() {
        List<MongoParkingLot> parkingLots = parkingLotService.searchParkingLotsByName("Central");
        assertEquals(1, parkingLots.size());
        assertEquals("Central Parking", parkingLots.get(0).getName());
    }
    
    @Test
    void testSearchParkingLotsByAddress() {
        List<MongoParkingLot> parkingLots = parkingLotService.searchParkingLotsByAddress("Main");
        assertEquals(1, parkingLots.size());
        assertEquals("123 Main St", parkingLots.get(0).getAddress());
    }
    
    @Test
    void testGetParkingLotsWithAvailableSpaces() {
        List<MongoParkingLot> parkingLots = parkingLotService.getParkingLotsWithAvailableSpaces(0);
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> parkingLotsWithManySpaces = parkingLotService.getParkingLotsWithAvailableSpaces(25);
        assertEquals(2, parkingLotsWithManySpaces.size());
    }
    
    @Test
    void testGetParkingLotsByHourlyRate() {
        List<MongoParkingLot> parkingLots = parkingLotService.getParkingLotsByHourlyRate(new BigDecimal("500.00"));
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> cheapParkingLots = parkingLotService.getParkingLotsByHourlyRate(new BigDecimal("400.00"));
        assertEquals(1, cheapParkingLots.size());
        assertEquals("Suburban Parking", cheapParkingLots.get(0).getName());
    }
    
    @Test
    void testGetParkingLotsByDailyRate() {
        List<MongoParkingLot> parkingLots = parkingLotService.getParkingLotsByDailyRate(new BigDecimal("5000.00"));
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> cheapParkingLots = parkingLotService.getParkingLotsByDailyRate(new BigDecimal("4000.00"));
        assertEquals(1, cheapParkingLots.size());
        assertEquals("Suburban Parking", cheapParkingLots.get(0).getName());
    }
    
    @Test
    void testGetParkingLotsByTotalSpaces() {
        List<MongoParkingLot> parkingLots = parkingLotService.getParkingLotsByTotalSpaces(150);
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> largeParkingLots = parkingLotService.getParkingLotsByTotalSpaces(200);
        assertEquals(1, largeParkingLots.size());
        assertEquals("Downtown Parking", largeParkingLots.get(0).getName());
    }
    
    @Test
    void testGetAvailableParkingLots() {
        List<MongoParkingLot> availableParkingLots = parkingLotService.getAvailableParkingLots();
        assertEquals(2, availableParkingLots.size());
        
        // Verify that Downtown Parking (0 available spaces) is not included
        boolean downtownIncluded = availableParkingLots.stream()
            .anyMatch(p -> p.getName().equals("Downtown Parking"));
        assertFalse(downtownIncluded);
    }
    
    @Test
    void testGetActiveAvailableParkingLots() {
        List<MongoParkingLot> activeAvailableParkingLots = parkingLotService.getActiveAvailableParkingLots();
        assertEquals(1, activeAvailableParkingLots.size());
        assertEquals("Central Parking", activeAvailableParkingLots.get(0).getName());
        
        // Verify that Suburban Parking (maintenance status) is not included
        boolean suburbanIncluded = activeAvailableParkingLots.stream()
            .anyMatch(p -> p.getName().equals("Suburban Parking"));
        assertFalse(suburbanIncluded);
    }
    
    @Test
    void testCreateParkingLot() {
        MongoParkingLot newParkingLot = new MongoParkingLot("New Parking", "999 New St", 75, 
            new BigDecimal("300.00"), new BigDecimal("3000.00"), MongoParkingLot.ParkingLotStatus.ACTIVE);
        newParkingLot.setAvailableSpaces(25);
        
        MongoParkingLot createdParkingLot = parkingLotService.createParkingLot(newParkingLot);
        
        assertNotNull(createdParkingLot.getId());
        assertEquals("New Parking", createdParkingLot.getName());
        assertEquals("999 New St", createdParkingLot.getAddress());
        assertNotNull(createdParkingLot.getCreatedAt());
        assertNotNull(createdParkingLot.getUpdatedAt());
        
        // Verify it was actually saved
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(createdParkingLot.getId());
        assertTrue(foundParkingLot.isPresent());
    }
    
    @Test
    void testUpdateParkingLot() {
        MongoParkingLot updateDetails = new MongoParkingLot();
        updateDetails.setName("Updated Central Parking");
        updateDetails.setAddress("Updated Address");
        updateDetails.setTotalSpaces(150);
        updateDetails.setAvailableSpaces(75);
        updateDetails.setHourlyRate(new BigDecimal("700.00"));
        updateDetails.setDailyRate(new BigDecimal("7000.00"));
        updateDetails.setStatus(MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        
        MongoParkingLot updatedParkingLot = parkingLotService.updateParkingLot(testParkingLot1.getId(), updateDetails);
        
        assertEquals("Updated Central Parking", updatedParkingLot.getName());
        assertEquals("Updated Address", updatedParkingLot.getAddress());
        assertEquals(150, updatedParkingLot.getTotalSpaces());
        assertEquals(75, updatedParkingLot.getAvailableSpaces());
        assertEquals(new BigDecimal("700.00"), updatedParkingLot.getHourlyRate());
        assertEquals(new BigDecimal("7000.00"), updatedParkingLot.getDailyRate());
        assertEquals(MongoParkingLot.ParkingLotStatus.MAINTENANCE, updatedParkingLot.getStatus());
        assertNotNull(updatedParkingLot.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals("Updated Central Parking", foundParkingLot.get().getName());
    }
    
    @Test
    void testUpdateParkingLotNotFound() {
        MongoParkingLot updateDetails = new MongoParkingLot();
        updateDetails.setName("Updated Parking");
        
        assertThrows(RuntimeException.class, () -> {
            parkingLotService.updateParkingLot("nonexistent-id", updateDetails);
        });
    }
    
    @Test
    void testUpdateAvailableSpaces() {
        MongoParkingLot updatedParkingLot = parkingLotService.updateAvailableSpaces(testParkingLot1.getId(), 25);
        
        assertEquals(25, updatedParkingLot.getAvailableSpaces());
        assertNotNull(updatedParkingLot.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals(25, foundParkingLot.get().getAvailableSpaces());
    }
    
    @Test
    void testUpdateAvailableSpacesNotFound() {
        assertThrows(RuntimeException.class, () -> {
            parkingLotService.updateAvailableSpaces("nonexistent-id", 25);
        });
    }
    
    @Test
    void testUpdateStatus() {
        MongoParkingLot updatedParkingLot = parkingLotService.updateStatus(testParkingLot1.getId(), MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        
        assertEquals(MongoParkingLot.ParkingLotStatus.MAINTENANCE, updatedParkingLot.getStatus());
        assertNotNull(updatedParkingLot.getUpdatedAt());
        
        // Verify the update was persisted
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals(MongoParkingLot.ParkingLotStatus.MAINTENANCE, foundParkingLot.get().getStatus());
    }
    
    @Test
    void testUpdateStatusNotFound() {
        assertThrows(RuntimeException.class, () -> {
            parkingLotService.updateStatus("nonexistent-id", MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        });
    }
    
    @Test
    void testDeleteParkingLot() {
        parkingLotService.deleteParkingLot(testParkingLot1.getId());
        
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertFalse(foundParkingLot.isPresent());
        
        List<MongoParkingLot> remainingParkingLots = parkingLotService.getAllParkingLots();
        assertEquals(2, remainingParkingLots.size());
    }
    
    @Test
    void testDeleteParkingLotNotFound() {
        assertThrows(RuntimeException.class, () -> {
            parkingLotService.deleteParkingLot("nonexistent-id");
        });
    }
    
    @Test
    void testExistsById() {
        assertTrue(parkingLotService.existsById(testParkingLot1.getId()));
        assertTrue(parkingLotService.existsById(testParkingLot2.getId()));
        assertFalse(parkingLotService.existsById("nonexistent-id"));
    }
} 