package com.company.retail.report.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReportDTO {

    private Long purchaseOrderId;
    private String shopName;
    private String supplierName;

    private Double totalCostUSD;
    private Double totalCostZWL;

    private LocalDateTime orderDate;
    private LocalDateTime receivedDate;
    private String status;

    // Optional aggregation
    private Integer totalItems;
}
