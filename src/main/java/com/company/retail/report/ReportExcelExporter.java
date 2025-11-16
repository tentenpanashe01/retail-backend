package com.company.retail.report;

import com.company.retail.report.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ReportExcelExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] export(String type, List<?> data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(type.toUpperCase() + " REPORT");
            sheet.setDefaultColumnWidth(20);

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(type.toUpperCase() + " REPORT");
            titleCell.setCellStyle(headerStyle);

            // Date generated
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated on: " + LocalDateTime.now().format(FORMATTER));

            int rowIndex = 3;
            switch (type.toLowerCase()) {
                case "sales" -> rowIndex = buildSalesSheet(sheet, headerStyle, rowIndex, (List<SalesReportDTO>) data);
                case "stock" -> rowIndex = buildStockSheet(sheet, headerStyle, rowIndex, (List<StockReportDTO>) data);
                case "expenses" -> rowIndex = buildExpenseSheet(sheet, headerStyle, rowIndex, (List<ExpenseReportDTO>) data);
                case "purchases" -> rowIndex = buildPurchaseSheet(sheet, headerStyle, rowIndex, (List<PurchaseReportDTO>) data);
                case "profit" -> rowIndex = buildProfitSheet(sheet, headerStyle, rowIndex, (ProfitReportDTO) data.get(0));
                case "cashflow" -> rowIndex = buildCashflowSheet(sheet, headerStyle, rowIndex, (CashflowReportDTO) data.get(0));
                case "dashboard" -> rowIndex = buildDashboardSheet(sheet, headerStyle, rowIndex, (DashboardSummaryDTO) data.get(0));
                default -> throw new IllegalArgumentException("Invalid report type: " + type);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("❌ Error exporting Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    // 🧾 SALES SHEET
    private int buildSalesSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, List<SalesReportDTO> data) {
        String[] headers = {"Sale ID", "Shop", "Cashier", "Total USD", "Total ZWL", "Profit USD", "Profit ZWL", "Items Sold", "Date"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        for (SalesReportDTO s : data) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(s.getSaleId());
            row.createCell(1).setCellValue(s.getShopName());
            row.createCell(2).setCellValue(s.getCashierName());
            row.createCell(3).setCellValue(s.getTotalAmountUSD());
            row.createCell(4).setCellValue(s.getTotalAmountZWL());
            row.createCell(5).setCellValue(s.getTotalProfitUSD());
            row.createCell(6).setCellValue(s.getTotalProfitZWL());
            row.createCell(7).setCellValue(s.getTotalItemsSold());
            row.createCell(8).setCellValue(s.getSaleDate().format(FORMATTER));
        }
        return rowIndex;
    }

    // 📦 STOCK SHEET
    private int buildStockSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, List<StockReportDTO> data) {
        String[] headers = {"Product ID", "Product", "Category", "Shop", "Qty", "Reorder", "Cost USD", "Cost ZWL", "Sell USD", "Sell ZWL",
                "Total Cost USD", "Total Cost ZWL", "Total Sell USD", "Total Sell ZWL"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        for (StockReportDTO s : data) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(s.getProductId());
            row.createCell(1).setCellValue(s.getProductName());
            row.createCell(2).setCellValue(s.getCategory());
            row.createCell(3).setCellValue(s.getShopName());
            row.createCell(4).setCellValue(s.getCurrentQuantity());
            row.createCell(5).setCellValue(s.getReorderLevel());
            row.createCell(6).setCellValue(s.getCostPriceUSD());
            row.createCell(7).setCellValue(s.getCostPriceZWL());
            row.createCell(8).setCellValue(s.getSellingPriceUSD());
            row.createCell(9).setCellValue(s.getSellingPriceZWL());
            row.createCell(10).setCellValue(s.getTotalValueAtCostUSD());
            row.createCell(11).setCellValue(s.getTotalValueAtCostZWL());
            row.createCell(12).setCellValue(s.getTotalValueAtSellingUSD());
            row.createCell(13).setCellValue(s.getTotalValueAtSellingZWL());
        }
        return rowIndex;
    }

    // 💰 EXPENSE SHEET
    private int buildExpenseSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, List<ExpenseReportDTO> data) {
        String[] headers = {"Expense ID", "Shop", "Category", "Description", "Amount USD", "Amount ZWL", "Date"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        for (ExpenseReportDTO e : data) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(e.getExpenseId());
            row.createCell(1).setCellValue(e.getShopName());
            row.createCell(2).setCellValue(e.getCategory());
            row.createCell(3).setCellValue(e.getDescription());
            row.createCell(4).setCellValue(e.getAmountUSD());
            row.createCell(5).setCellValue(e.getAmountZWL());
            row.createCell(6).setCellValue(e.getDate().format(FORMATTER));
        }
        return rowIndex;
    }

    // 🧾 PURCHASE SHEET
    private int buildPurchaseSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, List<PurchaseReportDTO> data) {
        String[] headers = {"Order ID", "Shop", "Supplier", "Total USD", "Total ZWL", "Order Date", "Received Date", "Status", "Items"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        for (PurchaseReportDTO p : data) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(p.getPurchaseOrderId());
            row.createCell(1).setCellValue(p.getShopName());
            row.createCell(2).setCellValue(p.getSupplierName());
            row.createCell(3).setCellValue(p.getTotalCostUSD());
            row.createCell(4).setCellValue(p.getTotalCostZWL());
            row.createCell(5).setCellValue(p.getOrderDate().format(FORMATTER));
            row.createCell(6).setCellValue(p.getReceivedDate() != null ? p.getReceivedDate().format(FORMATTER) : "Pending");
            row.createCell(7).setCellValue(p.getStatus());
            row.createCell(8).setCellValue(p.getTotalItems());
        }
        return rowIndex;
    }

    // 🟣 PROFIT SHEET
    private int buildProfitSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, ProfitReportDTO p) {
        String[] headers = {"Shop", "Period", "Sales USD", "Sales ZWL", "COGS USD", "COGS ZWL", "Expenses USD", "Expenses ZWL",
                "Gross USD", "Gross ZWL", "Net USD", "Net ZWL"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(p.getShopName());
        row.createCell(1).setCellValue(p.getPeriod());
        row.createCell(2).setCellValue(p.getTotalSalesUSD());
        row.createCell(3).setCellValue(p.getTotalSalesZWL());
        row.createCell(4).setCellValue(p.getTotalCostOfGoodsUSD());
        row.createCell(5).setCellValue(p.getTotalCostOfGoodsZWL());
        row.createCell(6).setCellValue(p.getTotalExpensesUSD());
        row.createCell(7).setCellValue(p.getTotalExpensesZWL());
        row.createCell(8).setCellValue(p.getGrossProfitUSD());
        row.createCell(9).setCellValue(p.getGrossProfitZWL());
        row.createCell(10).setCellValue(p.getNetProfitUSD());
        row.createCell(11).setCellValue(p.getNetProfitZWL());

        return rowIndex;
    }

    // 🟤 CASHFLOW SHEET
    private int buildCashflowSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, CashflowReportDTO c) {
        String[] headers = {"Shop", "Period", "Inflows USD", "Inflows ZWL", "Outflows USD", "Outflows ZWL", "Net USD", "Net ZWL"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(c.getShopName());
        row.createCell(1).setCellValue(c.getPeriod());
        row.createCell(2).setCellValue(c.getInflowsUSD());
        row.createCell(3).setCellValue(c.getInflowsZWL());
        row.createCell(4).setCellValue(c.getOutflowsUSD());
        row.createCell(5).setCellValue(c.getOutflowsZWL());
        row.createCell(6).setCellValue(c.getNetCashFlowUSD());
        row.createCell(7).setCellValue(c.getNetCashFlowZWL());

        return rowIndex;
    }

    // 📊 DASHBOARD SHEET
    private int buildDashboardSheet(Sheet sheet, CellStyle headerStyle, int rowIndex, DashboardSummaryDTO d) {
        String[] headers = {"Total Sales USD", "Total Sales ZWL", "Total Profit USD", "Total Profit ZWL", "Total Expenses USD",
                "Total Expenses ZWL", "Cash Flow USD", "Cash Flow ZWL", "Transactions", "Low Stock Items", "Top Product", "Top Product Sold"};
        Row header = sheet.createRow(rowIndex++);
        createHeaderRow(header, headerStyle, headers);

        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(d.getTotalSalesUSD());
        row.createCell(1).setCellValue(d.getTotalSalesZWL());
        row.createCell(2).setCellValue(d.getTotalProfitUSD());
        row.createCell(3).setCellValue(d.getTotalProfitZWL());
        row.createCell(4).setCellValue(d.getTotalExpensesUSD());
        row.createCell(5).setCellValue(d.getTotalExpensesZWL());
        row.createCell(6).setCellValue(d.getTotalCashFlowUSD());
        row.createCell(7).setCellValue(d.getTotalCashFlowZWL());
        row.createCell(8).setCellValue(d.getTotalTransactions());
        row.createCell(9).setCellValue(d.getLowStockItems());
        row.createCell(10).setCellValue(d.getTopProductName());
        row.createCell(11).setCellValue(d.getTopProductSold());

        return rowIndex;
    }

    // 🧰 Utility
    private void createHeaderRow(Row header, CellStyle headerStyle, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
}