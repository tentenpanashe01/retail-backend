package com.company.retail.report.dto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseReportDTO {

    private Long expenseId;
    private String shopName;
    private String category;
    private String description;

    private Double amountUSD;
    private Double amountZWL;

    private LocalDateTime date;

    // Optional for summaries
    private Double totalExpensesUSD;
    private Double totalExpensesZWL;
}

