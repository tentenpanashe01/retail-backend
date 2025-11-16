package com.company.retail.stocktransfer;

import com.company.retail.shop.ShopModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransferModel, Long> {

    List<StockTransferModel> findByFromShopOrToShop(ShopModel fromShop, ShopModel toShop);

}
