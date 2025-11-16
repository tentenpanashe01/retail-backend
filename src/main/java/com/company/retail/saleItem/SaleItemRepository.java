package com.company.retail.saleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItemModel, Long> {
    List<SaleItemModel> findBySale_SaleId(Long saleId);
}