package com.company.retail.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<SalesModel, Long> {
    List<SalesModel> findByShop_Id(Long shopId);
    List<SalesModel> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
}