package com.company.retail.report.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private Double totalSalesUSD;
    private Double totalSalesZWL;

    private Double totalProfitUSD;
    private Double totalProfitZWL;

    private Double totalExpensesUSD;
    private Double totalExpensesZWL;

    private Double totalCashFlowUSD;
    private Double totalCashFlowZWL;

    private Integer totalTransactions;
    private Integer lowStockItems;

    // Top metrics
    private String topProductName;
    private Integer topProductSold;
    private String topShopName;

    // For frontend charts
    private Object salesTrendData;
    private Object expenseTrendData;
    private Object profitTrendData;
}
