package com.company.retail.shop;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShopService {

    private final ShopRepository repo;

    public ShopService(ShopRepository repo) {
        this.repo = repo;
    }

    public List<ShopModel> getAllShops() {
        return repo.findAll();
    }

    public ShopModel getShopById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public ShopModel createShop(ShopModel shop) {
        if (repo.existsByShopNameIgnoreCase(shop.getShopName())) {
            throw new IllegalArgumentException("Shop with name '" + shop.getShopName() + "' already exists.");
        }
        return repo.save(shop);
    }


    public ShopModel updateShop(ShopModel shop) {
        return repo.save(shop);
    }

    public void deleteShop(Long id) {
        repo.deleteById(id);
    }
}
