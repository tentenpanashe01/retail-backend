package com.company.retail.report.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {

    private Long saleId;
    private String shopName;
    private String cashierName;

    private Double totalAmountUSD;
    private Double totalAmountZWL;
    private Double totalProfitUSD;
    private Double totalProfitZWL;

    private Integer totalItemsSold;
    private LocalDateTime saleDate;

    // Optional for dashboards
    private String topProductName;
    private Integer topProductQuantity;
}

