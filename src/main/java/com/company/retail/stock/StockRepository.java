package com.company.retail.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockModel, Long> {

    List<StockModel> findByShop_Id(Long shopId);

    List<StockModel> findByProduct_ProductId(Long productId);

}
