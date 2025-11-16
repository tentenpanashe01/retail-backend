package com.company.retail.ShopStock;

import com.company.retail.product.ProductModel;
import com.company.retail.shop.ShopModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopStockRepository extends JpaRepository<ShopStockModel, Long> {
    Optional<ShopStockModel> findByShopAndProduct(ShopModel shop, ProductModel product);
    List<ShopStockModel> findByShop(ShopModel shop);
    List<ShopStockModel> findByProduct(ProductModel product);

    @Query("SELECT SUM(s.quantityInStock) FROM ShopStockModel s WHERE s.product = :product")
    Integer sumQuantityByProduct(@Param("product") ProductModel product);


    List<ShopStockModel> findByShopAndQuantityInStockLessThanEqual(ShopModel shop, int i);

    List<ShopStockModel> findByShop_Id(Long shopId);

    Optional<ShopStockModel> findByShop_IdAndProduct_ProductId(Long shopId, Long productId);
}
