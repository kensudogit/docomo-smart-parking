package com.docomo.admin.config;

import com.docomo.admin.entity.ParkingLot;
import com.docomo.admin.entity.User;
import com.docomo.admin.service.ParkingLotService;
import com.docomo.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ParkingLotService parkingLotService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 管理者ユーザーの作成
        if (userService.getAllUsers().isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setEmail("admin@docomo-smart-parking.com");
            admin.setFullName("システム管理者");
            admin.setRole(User.UserRole.ADMIN);
            userService.createUser(admin);
        }

        // サンプル駐車場の作成
        if (parkingLotService.getAllParkingLots().isEmpty()) {
            ParkingLot lot1 = new ParkingLot();
            lot1.setName("東京駅前駐車場");
            lot1.setAddress("東京都千代田区丸の内1-1-1");
            lot1.setTotalSpaces(100);
            lot1.setAvailableSpaces(85);
            lot1.setHourlyRate(new BigDecimal("300"));
            lot1.setDailyRate(new BigDecimal("2000"));
            lot1.setStatus(ParkingLot.ParkingLotStatus.ACTIVE);
            parkingLotService.createParkingLot(lot1);

            ParkingLot lot2 = new ParkingLot();
            lot2.setName("渋谷スクランブル駐車場");
            lot2.setAddress("東京都渋谷区渋谷2-1-1");
            lot2.setTotalSpaces(50);
            lot2.setAvailableSpaces(30);
            lot2.setHourlyRate(new BigDecimal("400"));
            lot2.setDailyRate(new BigDecimal("2500"));
            lot2.setStatus(ParkingLot.ParkingLotStatus.ACTIVE);
            parkingLotService.createParkingLot(lot2);

            ParkingLot lot3 = new ParkingLot();
            lot3.setName("新宿西口駐車場");
            lot3.setAddress("東京都新宿区西新宿1-1-1");
            lot3.setTotalSpaces(80);
            lot3.setAvailableSpaces(0);
            lot3.setHourlyRate(new BigDecimal("350"));
            lot3.setDailyRate(new BigDecimal("2200"));
            lot3.setStatus(ParkingLot.ParkingLotStatus.MAINTENANCE);
            parkingLotService.createParkingLot(lot3);
        }
    }
} 