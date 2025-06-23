package com.docomo.admin.repository.mongo;

import com.docomo.admin.entity.mongo.MongoParkingLot;
import com.docomo.admin.config.MongoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ContextConfiguration(classes = MongoTestConfig.class)
class MongoParkingLotRepositoryTest {
    
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
    void testFindAll() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findAll();
        assertEquals(3, parkingLots.size());
    }
    
    @Test
    void testFindById() {
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals("Central Parking", foundParkingLot.get().getName());
        assertEquals("123 Main St", foundParkingLot.get().getAddress());
    }
    
    @Test
    void testFindByStatus() {
        List<MongoParkingLot> activeParkingLots = parkingLotRepository.findByStatus(MongoParkingLot.ParkingLotStatus.ACTIVE);
        assertEquals(2, activeParkingLots.size());
        
        List<MongoParkingLot> maintenanceParkingLots = parkingLotRepository.findByStatus(MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        assertEquals(1, maintenanceParkingLots.size());
        assertEquals("Suburban Parking", maintenanceParkingLots.get(0).getName());
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByNameContainingIgnoreCase("Central");
        assertEquals(1, parkingLots.size());
        assertEquals("Central Parking", parkingLots.get(0).getName());
        
        // Test case insensitive
        List<MongoParkingLot> parkingLotsCaseInsensitive = parkingLotRepository.findByNameContainingIgnoreCase("central");
        assertEquals(1, parkingLotsCaseInsensitive.size());
    }
    
    @Test
    void testFindByAddressContainingIgnoreCase() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByAddressContainingIgnoreCase("Main");
        assertEquals(1, parkingLots.size());
        assertEquals("123 Main St", parkingLots.get(0).getAddress());
        
        // Test case insensitive
        List<MongoParkingLot> parkingLotsCaseInsensitive = parkingLotRepository.findByAddressContainingIgnoreCase("main");
        assertEquals(1, parkingLotsCaseInsensitive.size());
    }
    
    @Test
    void testFindByAvailableSpacesGreaterThan() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByAvailableSpacesGreaterThan(0);
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> parkingLotsWithManySpaces = parkingLotRepository.findByAvailableSpacesGreaterThan(25);
        assertEquals(2, parkingLotsWithManySpaces.size());
    }
    
    @Test
    void testFindByHourlyRateLessThanOrEqualTo() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByHourlyRateLessThanOrEqualTo(new BigDecimal("500.00"));
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> cheapParkingLots = parkingLotRepository.findByHourlyRateLessThanOrEqualTo(new BigDecimal("400.00"));
        assertEquals(1, cheapParkingLots.size());
        assertEquals("Suburban Parking", cheapParkingLots.get(0).getName());
    }
    
    @Test
    void testFindByDailyRateLessThanOrEqualTo() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByDailyRateLessThanOrEqualTo(new BigDecimal("5000.00"));
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> cheapParkingLots = parkingLotRepository.findByDailyRateLessThanOrEqualTo(new BigDecimal("4000.00"));
        assertEquals(1, cheapParkingLots.size());
        assertEquals("Suburban Parking", cheapParkingLots.get(0).getName());
    }
    
    @Test
    void testFindByTotalSpacesGreaterThanOrEqualTo() {
        List<MongoParkingLot> parkingLots = parkingLotRepository.findByTotalSpacesGreaterThanOrEqualTo(150);
        assertEquals(2, parkingLots.size());
        
        List<MongoParkingLot> largeParkingLots = parkingLotRepository.findByTotalSpacesGreaterThanOrEqualTo(200);
        assertEquals(1, largeParkingLots.size());
        assertEquals("Downtown Parking", largeParkingLots.get(0).getName());
    }
    
    @Test
    void testFindAvailableParkingLots() {
        List<MongoParkingLot> availableParkingLots = parkingLotRepository.findAvailableParkingLots();
        assertEquals(2, availableParkingLots.size());
        
        // Verify that Downtown Parking (0 available spaces) is not included
        boolean downtownIncluded = availableParkingLots.stream()
            .anyMatch(p -> p.getName().equals("Downtown Parking"));
        assertFalse(downtownIncluded);
    }
    
    @Test
    void testFindActiveAvailableParkingLots() {
        List<MongoParkingLot> activeAvailableParkingLots = parkingLotRepository.findActiveAvailableParkingLots();
        assertEquals(1, activeAvailableParkingLots.size());
        assertEquals("Central Parking", activeAvailableParkingLots.get(0).getName());
        
        // Verify that Suburban Parking (maintenance status) is not included
        boolean suburbanIncluded = activeAvailableParkingLots.stream()
            .anyMatch(p -> p.getName().equals("Suburban Parking"));
        assertFalse(suburbanIncluded);
    }
    
    @Test
    void testSave() {
        MongoParkingLot newParkingLot = new MongoParkingLot("New Parking", "999 New St", 75, 
            new BigDecimal("300.00"), new BigDecimal("3000.00"), MongoParkingLot.ParkingLotStatus.ACTIVE);
        newParkingLot.setAvailableSpaces(25);
        
        MongoParkingLot savedParkingLot = parkingLotRepository.save(newParkingLot);
        
        assertNotNull(savedParkingLot.getId());
        assertEquals("New Parking", savedParkingLot.getName());
        assertEquals("999 New St", savedParkingLot.getAddress());
        
        // Verify it was actually saved
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(savedParkingLot.getId());
        assertTrue(foundParkingLot.isPresent());
    }
    
    @Test
    void testUpdate() {
        testParkingLot1.setName("Updated Central Parking");
        testParkingLot1.setAvailableSpaces(25);
        MongoParkingLot updatedParkingLot = parkingLotRepository.save(testParkingLot1);
        
        assertEquals("Updated Central Parking", updatedParkingLot.getName());
        assertEquals(25, updatedParkingLot.getAvailableSpaces());
        
        // Verify the update was persisted
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertTrue(foundParkingLot.isPresent());
        assertEquals("Updated Central Parking", foundParkingLot.get().getName());
    }
    
    @Test
    void testDelete() {
        parkingLotRepository.deleteById(testParkingLot1.getId());
        
        Optional<MongoParkingLot> foundParkingLot = parkingLotRepository.findById(testParkingLot1.getId());
        assertFalse(foundParkingLot.isPresent());
        
        List<MongoParkingLot> remainingParkingLots = parkingLotRepository.findAll();
        assertEquals(2, remainingParkingLots.size());
    }
    
    @Test
    void testDeleteAll() {
        parkingLotRepository.deleteAll();
        
        List<MongoParkingLot> parkingLots = parkingLotRepository.findAll();
        assertEquals(0, parkingLots.size());
    }
} 