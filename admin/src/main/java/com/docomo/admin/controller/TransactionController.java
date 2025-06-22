package com.docomo.admin.controller;

import com.docomo.admin.entity.Transaction;
import com.docomo.admin.service.ParkingLotService;
import com.docomo.admin.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    private final ParkingLotService parkingLotService;
    
    @GetMapping
    public String listTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactions());
        return "transactions/list";
    }
    
    @GetMapping("/new")
    public String newTransactionForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("parkingLots", parkingLotService.getAllParkingLots());
        model.addAttribute("statuses", Transaction.TransactionStatus.values());
        model.addAttribute("paymentMethods", Transaction.PaymentMethod.values());
        return "transactions/form";
    }
    
    @PostMapping
    public String createTransaction(@ModelAttribute Transaction transaction, RedirectAttributes redirectAttributes) {
        try {
            transactionService.createTransaction(transaction);
            redirectAttributes.addFlashAttribute("success", "取引が正常に作成されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }
    
    @GetMapping("/{id}/edit")
    public String editTransactionForm(@PathVariable Long id, Model model) {
        Transaction transaction = transactionService.getTransactionById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        model.addAttribute("transaction", transaction);
        model.addAttribute("parkingLots", parkingLotService.getAllParkingLots());
        model.addAttribute("statuses", Transaction.TransactionStatus.values());
        model.addAttribute("paymentMethods", Transaction.PaymentMethod.values());
        return "transactions/form";
    }
    
    @PostMapping("/{id}")
    public String updateTransaction(@PathVariable Long id, @ModelAttribute Transaction transaction, RedirectAttributes redirectAttributes) {
        try {
            transactionService.updateTransaction(id, transaction);
            redirectAttributes.addFlashAttribute("success", "取引が正常に更新されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("success", "取引が正常に削除されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }
    
    @GetMapping("/revenue")
    public String revenueReport(Model model) {
        BigDecimal todayRevenue = transactionService.getTotalRevenue();
        BigDecimal monthlyRevenue = transactionService.getMonthlyRevenue();
        
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("parkingLots", parkingLotService.getAllParkingLots());
        
        return "transactions/revenue";
    }
    
    @GetMapping("/revenue/parking-lot/{id}")
    public String parkingLotRevenue(@PathVariable Long id, Model model) {
        BigDecimal revenue = transactionService.getTotalRevenueByParkingLot(id);
        model.addAttribute("revenue", revenue);
        model.addAttribute("parkingLot", parkingLotService.getParkingLotById(id).orElse(null));
        return "transactions/parking-lot-revenue";
    }
} 