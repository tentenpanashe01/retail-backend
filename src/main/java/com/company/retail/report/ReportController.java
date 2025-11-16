package com.company.retail.report;

import com.company.retail.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ================================
    // 🧾 SALES REPORT
    // ================================
    @GetMapping("/sales")
    public List<SalesReportDTO> getSalesReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false) Long shopId
    ) {
        return reportService.generateSalesReport(start, end, shopId);
    }

    // ================================
    // 📦 STOCK REPORT
    // ================================
    @GetMapping("/stock")
    public List<StockReportDTO> getStockReport(
            @RequestParam(required = false) Long shopId
    ) {
        return reportService.generateStockReport(shopId);
    }

    // ================================
    // 💰 EXPENSE REPORT
    // ================================
    @GetMapping("/expenses")
    public List<ExpenseReportDTO> getExpenseReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false) Long shopId
    ) {
        return reportService.generateExpenseReport(start, end, shopId);
    }

    // ================================
    // 🧾 PURCHASE REPORT
    // ================================
    @GetMapping("/purchases")
    public List<PurchaseReportDTO> getPurchaseReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        return reportService.generatePurchaseReport(start, end);
    }

    // ================================
    // 🟣 PROFIT REPORT
    // ================================
    @GetMapping("/profit")
    public ProfitReportDTO getProfitReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false) Long shopId
    ) {
        return reportService.generateProfitReport(start, end, shopId);
    }

    // ================================
    // 🟤 CASH FLOW REPORT
    // ================================
    @GetMapping("/cashflow")
    public CashflowReportDTO getCashFlowReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false) Long shopId
    ) {
        return reportService.generateCashflowReport(start, end, shopId);
    }

    // ================================
    // 📊 DASHBOARD SUMMARY
    // ================================
    @GetMapping("/dashboard")
    public DashboardSummaryDTO getDashboardSummary(
            @RequestParam LocalDate date
    ) {
        return reportService.generateDashboardSummary(date);
    }

    // ================================
    // 📤 EXPORT TO PDF
    // ================================
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam String type,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        byte[] pdf = reportService.exportToPdf(type, start, end);
        String fileName = type + "_report.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ================================
    // 📤 EXPORT TO EXCEL
    // ================================

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        byte[] excelBytes = reportService.exportToExcel(type, start, end);

        String fileName = String.format("%s_report_%s_to_%s.xlsx",
                type.toLowerCase(), start.toString(), end.toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
