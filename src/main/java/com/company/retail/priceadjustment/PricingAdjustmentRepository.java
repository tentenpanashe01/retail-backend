package com.company.retail.priceadjustment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingAdjustmentRepository extends JpaRepository<PricingAdjustmentModel, Long> {
    List<PricingAdjustmentModel> findByProduct_ProductId(Long productId);
    List<PricingAdjustmentModel> findByShop_Id(Long shopId);
}
