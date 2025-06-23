package com.docomo.admin.entity.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

class MongoParkingLotTest {
    
    private MongoParkingLot parkingLot;
    
    @BeforeEach
    void setUp() {
        parkingLot = new MongoParkingLot();
        parkingLot.setId("test-id");
        parkingLot.setName("Test Parking Lot");
        parkingLot.setAddress("123 Test Street, Test City");
        parkingLot.setTotalSpaces(100);
        parkingLot.setAvailableSpaces(50);
        parkingLot.setHourlyRate(new BigDecimal("500.00"));
        parkingLot.setDailyRate(new BigDecimal("5000.00"));
        parkingLot.setStatus(MongoParkingLot.ParkingLotStatus.ACTIVE);
        parkingLot.setCreatedAt(LocalDateTime.now());
        parkingLot.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testMongoParkingLotCreation() {
        assertNotNull(parkingLot);
        assertEquals("test-id", parkingLot.getId());
        assertEquals("Test Parking Lot", parkingLot.getName());
        assertEquals("123 Test Street, Test City", parkingLot.getAddress());
        assertEquals(100, parkingLot.getTotalSpaces());
        assertEquals(50, parkingLot.getAvailableSpaces());
        assertEquals(new BigDecimal("500.00"), parkingLot.getHourlyRate());
        assertEquals(new BigDecimal("5000.00"), parkingLot.getDailyRate());
        assertEquals(MongoParkingLot.ParkingLotStatus.ACTIVE, parkingLot.getStatus());
        assertNotNull(parkingLot.getCreatedAt());
        assertNotNull(parkingLot.getUpdatedAt());
    }
    
    @Test
    void testMongoParkingLotConstructor() {
        MongoParkingLot constructorParkingLot = new MongoParkingLot(
            "New Parking Lot",
            "456 New Street, New City",
            200,
            new BigDecimal("600.00"),
            new BigDecimal("6000.00"),
            MongoParkingLot.ParkingLotStatus.ACTIVE
        );
        
        assertEquals("New Parking Lot", constructorParkingLot.getName());
        assertEquals("456 New Street, New City", constructorParkingLot.getAddress());
        assertEquals(200, constructorParkingLot.getTotalSpaces());
        assertEquals(200, constructorParkingLot.getAvailableSpaces()); // Should be same as total initially
        assertEquals(new BigDecimal("600.00"), constructorParkingLot.getHourlyRate());
        assertEquals(new BigDecimal("6000.00"), constructorParkingLot.getDailyRate());
        assertEquals(MongoParkingLot.ParkingLotStatus.ACTIVE, constructorParkingLot.getStatus());
        assertNotNull(constructorParkingLot.getCreatedAt());
        assertNotNull(constructorParkingLot.getUpdatedAt());
    }
    
    @Test
    void testMongoParkingLotSettersAndGetters() {
        parkingLot.setId("new-id");
        parkingLot.setName("New Parking Lot Name");
        parkingLot.setAddress("New Address");
        parkingLot.setTotalSpaces(150);
        parkingLot.setAvailableSpaces(75);
        parkingLot.setHourlyRate(new BigDecimal("700.00"));
        parkingLot.setDailyRate(new BigDecimal("7000.00"));
        parkingLot.setStatus(MongoParkingLot.ParkingLotStatus.MAINTENANCE);
        
        LocalDateTime now = LocalDateTime.now();
        parkingLot.setCreatedAt(now);
        parkingLot.setUpdatedAt(now);
        
        assertEquals("new-id", parkingLot.getId());
        assertEquals("New Parking Lot Name", parkingLot.getName());
        assertEquals("New Address", parkingLot.getAddress());
        assertEquals(150, parkingLot.getTotalSpaces());
        assertEquals(75, parkingLot.getAvailableSpaces());
        assertEquals(new BigDecimal("700.00"), parkingLot.getHourlyRate());
        assertEquals(new BigDecimal("7000.00"), parkingLot.getDailyRate());
        assertEquals(MongoParkingLot.ParkingLotStatus.MAINTENANCE, parkingLot.getStatus());
        assertEquals(now, parkingLot.getCreatedAt());
        assertEquals(now, parkingLot.getUpdatedAt());
    }
    
    @Test
    void testParkingLotStatusEnum() {
        assertEquals("ACTIVE", MongoParkingLot.ParkingLotStatus.ACTIVE.name());
        assertEquals("INACTIVE", MongoParkingLot.ParkingLotStatus.INACTIVE.name());
        assertEquals("MAINTENANCE", MongoParkingLot.ParkingLotStatus.MAINTENANCE.name());
        
        assertEquals(0, MongoParkingLot.ParkingLotStatus.ACTIVE.ordinal());
        assertEquals(1, MongoParkingLot.ParkingLotStatus.INACTIVE.ordinal());
        assertEquals(2, MongoParkingLot.ParkingLotStatus.MAINTENANCE.ordinal());
    }
    
    @Test
    void testMongoParkingLotEquality() {
        MongoParkingLot parkingLot1 = new MongoParkingLot();
        parkingLot1.setId("same-id");
        parkingLot1.setName("Same Name");
        
        MongoParkingLot parkingLot2 = new MongoParkingLot();
        parkingLot2.setId("same-id");
        parkingLot2.setName("Same Name");
        
        assertEquals(parkingLot1.getId(), parkingLot2.getId());
        assertEquals(parkingLot1.getName(), parkingLot2.getName());
    }
    
    @Test
    void testMongoParkingLotToString() {
        String parkingLotString = parkingLot.toString();
        assertNotNull(parkingLotString);
        assertTrue(parkingLotString.contains("Test Parking Lot"));
        assertTrue(parkingLotString.contains("123 Test Street"));
    }
    
    @Test
    void testAvailableSpacesValidation() {
        // Available spaces should not exceed total spaces
        parkingLot.setTotalSpaces(50);
        parkingLot.setAvailableSpaces(60);
        
        // This is a business logic validation that should be handled in service layer
        // For entity test, we just verify the setters work
        assertEquals(50, parkingLot.getTotalSpaces());
        assertEquals(60, parkingLot.getAvailableSpaces());
    }
} 