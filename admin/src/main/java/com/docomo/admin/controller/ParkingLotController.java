package com.docomo.admin.controller;

import com.docomo.admin.entity.ParkingLot;
import com.docomo.admin.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {
    
    private final ParkingLotService parkingLotService;
    
    @GetMapping
    public String listParkingLots(Model model) {
        model.addAttribute("parkingLots", parkingLotService.getAllParkingLots());
        return "parking-lots/list";
    }
    
    @GetMapping("/new")
    public String newParkingLotForm(Model model) {
        model.addAttribute("parkingLot", new ParkingLot());
        model.addAttribute("statuses", ParkingLot.ParkingLotStatus.values());
        return "parking-lots/form";
    }
    
    @PostMapping
    public String createParkingLot(@ModelAttribute ParkingLot parkingLot, RedirectAttributes redirectAttributes) {
        try {
            parkingLotService.createParkingLot(parkingLot);
            redirectAttributes.addFlashAttribute("success", "駐車場が正常に作成されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parking-lots";
    }
    
    @GetMapping("/{id}/edit")
    public String editParkingLotForm(@PathVariable Long id, Model model) {
        ParkingLot parkingLot = parkingLotService.getParkingLotById(id)
            .orElseThrow(() -> new RuntimeException("Parking lot not found"));
        model.addAttribute("parkingLot", parkingLot);
        model.addAttribute("statuses", ParkingLot.ParkingLotStatus.values());
        return "parking-lots/form";
    }
    
    @PostMapping("/{id}")
    public String updateParkingLot(@PathVariable Long id, @ModelAttribute ParkingLot parkingLot, RedirectAttributes redirectAttributes) {
        try {
            parkingLotService.updateParkingLot(id, parkingLot);
            redirectAttributes.addFlashAttribute("success", "駐車場が正常に更新されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parking-lots";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteParkingLot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parkingLotService.deleteParkingLot(id);
            redirectAttributes.addFlashAttribute("success", "駐車場が正常に削除されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parking-lots";
    }
    
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ParkingLot.ParkingLotStatus status, RedirectAttributes redirectAttributes) {
        try {
            parkingLotService.updateParkingLotStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "駐車場のステータスが正常に更新されました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parking-lots";
    }
} 