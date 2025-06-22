package com.docomo.admin.controller;

import com.docomo.admin.service.ParkingLotService;
import com.docomo.admin.service.TransactionService;
import com.docomo.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserService userService;
    private final ParkingLotService parkingLotService;
    private final TransactionService transactionService;
    
    @GetMapping("/")
    public String dashboard(Model model) {
        // 統計情報を取得
        long totalUsers = userService.getAllUsers().size();
        long totalParkingLots = parkingLotService.getAllParkingLots().size();
        long activeParkingLots = parkingLotService.getParkingLotsByStatus(
            com.docomo.admin.entity.ParkingLot.ParkingLotStatus.ACTIVE
        ).size();
        
        BigDecimal todayRevenue = transactionService.getTotalRevenue();
        BigDecimal monthlyRevenue = transactionService.getMonthlyRevenue();
        
        // モデルにデータを追加
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalParkingLots", totalParkingLots);
        model.addAttribute("activeParkingLots", activeParkingLots);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        
        return "dashboard";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
} 