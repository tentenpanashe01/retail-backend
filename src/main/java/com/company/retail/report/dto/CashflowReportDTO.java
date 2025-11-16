package com.company.retail.report.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashflowReportDTO {

    private String shopName;
    private String period;

    private Double openingBalanceUSD;
    private Double closingBalanceUSD;
    private Double inflowsUSD;
    private Double outflowsUSD;

    private Double openingBalanceZWL;
    private Double closingBalanceZWL;
    private Double inflowsZWL;
    private Double outflowsZWL;

    private Double netCashFlowUSD;
    private Double netCashFlowZWL;
}

