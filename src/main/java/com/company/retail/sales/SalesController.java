package com.company.retail.sales;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @PreAuthorize("hasAnyRole('CASHIER', 'SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @PostMapping("/{shopId}/{cashierId}")
    public ResponseEntity<?> createSale(@PathVariable Long shopId,
                                        @PathVariable Long cashierId,
                                        @RequestBody SalesModel sale) {
        return ResponseEntity.ok(salesService.createSale(sale, shopId, cashierId));
    }

    // ONLY ADMIN + SUPERADMIN
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<SalesModel>> getAllSales() {
        return ResponseEntity.ok(salesService.getAllSales());
    }

    // ADMIN + SUPERVISOR
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<SalesModel>> getSalesByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(salesService.getSalesByShop(shopId));
    }

    // ADMIN + SUPERVISOR
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/range")
    public ResponseEntity<List<SalesModel>> getSalesByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(salesService.getSalesBetween(start, end));
    }

    // CASHIER sees ONLY his daily; others can see ALL users
    @PreAuthorize("hasAnyRole('CASHIER','SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/user/{userId}/daily")
    public ResponseEntity<List<SalesModel>> getUserDailySales(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return ResponseEntity.ok(salesService.getSalesForUserOnDate(userId, start, end));
    }

    // ADMIN ONLY
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        salesService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }
}