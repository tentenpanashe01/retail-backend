package com.company.retail.report.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReportDTO {

    private Long productId;
    private String productName;
    private String category;
    private String shopName;

    private Integer currentQuantity;
    private Integer reorderLevel;

    private Double costPriceUSD;
    private Double costPriceZWL;
    private Double sellingPriceUSD;
    private Double sellingPriceZWL;

    // Valuation
    private Double totalValueAtCostUSD;
    private Double totalValueAtCostZWL;
    private Double totalValueAtSellingUSD;
    private Double totalValueAtSellingZWL;
}
