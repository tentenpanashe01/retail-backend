package com.company.retail.shop;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }

    // ✅ All authenticated users can view shop list
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'CASHIER', 'SUPERVISOR')")
    @GetMapping
    public List<ShopModel> getAllShops() {
        return service.getAllShops();
    }

    // ✅ Anyone logged in can view a specific shop
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'CASHIER', 'SUPERVISOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ShopModel> getShop(@PathVariable Long id) {
        ShopModel shop = service.getShopById(id);
        return (shop != null)
                ? ResponseEntity.ok(shop)
                : ResponseEntity.notFound().build();
    }

    // ✅ Only Admins or SuperAdmins can create new shops
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<?> createShop(@RequestBody ShopModel shop) {
        try {
            ShopModel created = service.createShop(shop);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ✅ Only Admins or SuperAdmins can update shops
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShop(@PathVariable Long id, @RequestBody ShopModel shop) {
        try {
            shop.setId(id);
            ShopModel updated = service.updateShop(shop);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ✅ Only Admins or SuperAdmins can delete shops
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        service.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}