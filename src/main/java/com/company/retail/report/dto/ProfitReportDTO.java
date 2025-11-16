package com.company.retail.report.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitReportDTO {

    private String shopName;
    private Double totalSalesUSD;
    private Double totalSalesZWL;

    private Double totalCostOfGoodsUSD;
    private Double totalCostOfGoodsZWL;

    private Double totalExpensesUSD;
    private Double totalExpensesZWL;

    private Double grossProfitUSD;
    private Double grossProfitZWL;

    private Double netProfitUSD;
    private Double netProfitZWL;

    private String period;
}
