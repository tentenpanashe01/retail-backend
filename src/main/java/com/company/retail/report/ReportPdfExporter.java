package com.company.retail.report;


import com.company.retail.report.dto.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportPdfExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] export(String type, List<?> data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);

            Paragraph title = new Paragraph(type.toUpperCase() + " REPORT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold();
            document.add(title);
            document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(FORMATTER))
                    .setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            switch (type.toLowerCase()) {
                case "sales" -> buildSalesTable(document, (List<SalesReportDTO>) data);
                case "stock" -> buildStockTable(document, (List<StockReportDTO>) data);
                case "expenses" -> buildExpenseTable(document, (List<ExpenseReportDTO>) data);
                case "purchases" -> buildPurchaseTable(document, (List<PurchaseReportDTO>) data);
                case "profit" -> buildProfitTable(document, (ProfitReportDTO) data.get(0));
                case "cashflow" -> buildCashFlowTable(document, (CashflowReportDTO) data.get(0));
                case "dashboard" -> buildDashboardTable(document, (DashboardSummaryDTO) data.get(0));
                default -> throw new IllegalArgumentException("Invalid report type: " + type);
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void buildSalesTable(Document doc, List<SalesReportDTO> list) {
        String[] headers = {"Sale ID", "Shop", "Cashier", "Total USD", "Total ZWL", "Profit USD", "Profit ZWL", "Items Sold", "Date"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        for (SalesReportDTO s : list) {
            table.addCell(String.valueOf(s.getSaleId()));
            table.addCell(s.getShopName());
            table.addCell(s.getCashierName());
            table.addCell(format(s.getTotalAmountUSD()));
            table.addCell(format(s.getTotalAmountZWL()));
            table.addCell(format(s.getTotalProfitUSD()));
            table.addCell(format(s.getTotalProfitZWL()));
            table.addCell(String.valueOf(s.getTotalItemsSold()));
            table.addCell(formatDate(s.getSaleDate()));
        }
        doc.add(table);
    }

    private void buildStockTable(Document doc, List<StockReportDTO> list) {
        String[] headers = {"Product ID", "Name", "Category", "Shop", "Qty", "Reorder", "Cost USD", "Cost ZWL", "Sell USD", "Sell ZWL", "Value Cost USD", "Value Cost ZWL"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        for (StockReportDTO s : list) {
            table.addCell(String.valueOf(s.getProductId()));
            table.addCell(s.getProductName());
            table.addCell(s.getCategory());
            table.addCell(s.getShopName());
            table.addCell(String.valueOf(s.getCurrentQuantity()));
            table.addCell(String.valueOf(s.getReorderLevel()));
            table.addCell(format(s.getCostPriceUSD()));
            table.addCell(format(s.getCostPriceZWL()));
            table.addCell(format(s.getSellingPriceUSD()));
            table.addCell(format(s.getSellingPriceZWL()));
            table.addCell(format(s.getTotalValueAtCostUSD()));
            table.addCell(format(s.getTotalValueAtCostZWL()));
        }
        doc.add(table);
    }

    private void buildExpenseTable(Document doc, List<ExpenseReportDTO> list) {
        String[] headers = {"Expense ID", "Shop", "Category", "Description", "USD", "ZWL", "Date"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        for (ExpenseReportDTO e : list) {
            table.addCell(String.valueOf(e.getExpenseId()));
            table.addCell(e.getShopName());
            table.addCell(e.getCategory());
            table.addCell(e.getDescription());
            table.addCell(format(e.getAmountUSD()));
            table.addCell(format(e.getAmountZWL()));
            table.addCell(formatDate(e.getDate()));
        }
        doc.add(table);
    }

    private void buildPurchaseTable(Document doc, List<PurchaseReportDTO> list) {
        String[] headers = {"PO ID", "Shop", "Supplier", "USD", "ZWL", "Order Date", "Received Date", "Status", "Items"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        for (PurchaseReportDTO p : list) {
            table.addCell(String.valueOf(p.getPurchaseOrderId()));
            table.addCell(p.getShopName());
            table.addCell(p.getSupplierName());
            table.addCell(format(p.getTotalCostUSD()));
            table.addCell(format(p.getTotalCostZWL()));
            table.addCell(formatDate(p.getOrderDate()));
            table.addCell(formatDate(p.getReceivedDate()));
            table.addCell(p.getStatus());
            table.addCell(String.valueOf(p.getTotalItems()));
        }
        doc.add(table);
    }

    private void buildProfitTable(Document doc, ProfitReportDTO p) {
        String[] headers = {"Shop", "Sales USD", "Sales ZWL", "COGS USD", "COGS ZWL", "Expenses USD", "Expenses ZWL", "Gross USD", "Gross ZWL", "Net USD", "Net ZWL", "Period"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        table.addCell(p.getShopName());
        table.addCell(format(p.getTotalSalesUSD()));
        table.addCell(format(p.getTotalSalesZWL()));
        table.addCell(format(p.getTotalCostOfGoodsUSD()));
        table.addCell(format(p.getTotalCostOfGoodsZWL()));
        table.addCell(format(p.getTotalExpensesUSD()));
        table.addCell(format(p.getTotalExpensesZWL()));
        table.addCell(format(p.getGrossProfitUSD()));
        table.addCell(format(p.getGrossProfitZWL()));
        table.addCell(format(p.getNetProfitUSD()));
        table.addCell(format(p.getNetProfitZWL()));
        table.addCell(p.getPeriod());

        doc.add(table);
    }

    private void buildCashFlowTable(Document doc, CashflowReportDTO c) {
        String[] headers = {"Shop", "Opening USD", "Inflows USD", "Outflows USD", "Closing USD", "Net USD",
                "Opening ZWL", "Inflows ZWL", "Outflows ZWL", "Closing ZWL", "Net ZWL", "Period"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        table.addCell(c.getShopName());
        table.addCell(format(c.getOpeningBalanceUSD()));
        table.addCell(format(c.getInflowsUSD()));
        table.addCell(format(c.getOutflowsUSD()));
        table.addCell(format(c.getClosingBalanceUSD()));
        table.addCell(format(c.getNetCashFlowUSD()));
        table.addCell(format(c.getOpeningBalanceZWL()));
        table.addCell(format(c.getInflowsZWL()));
        table.addCell(format(c.getOutflowsZWL()));
        table.addCell(format(c.getClosingBalanceZWL()));
        table.addCell(format(c.getNetCashFlowZWL()));
        table.addCell(c.getPeriod());

        doc.add(table);
    }

    private void buildDashboardTable(Document doc, DashboardSummaryDTO d) {
        String[] headers = {"Sales USD", "Sales ZWL", "Profit USD", "Profit ZWL", "Expenses USD", "Expenses ZWL",
                "Cash Flow USD", "Cash Flow ZWL", "Transactions", "Low Stock", "Top Product", "Units"};
        Table table = createTable(headers.length);
        addHeaderRow(table, headers);

        table.addCell(format(d.getTotalSalesUSD()));
        table.addCell(format(d.getTotalSalesZWL()));
        table.addCell(format(d.getTotalProfitUSD()));
        table.addCell(format(d.getTotalProfitZWL()));
        table.addCell(format(d.getTotalExpensesUSD()));
        table.addCell(format(d.getTotalExpensesZWL()));
        table.addCell(format(d.getTotalCashFlowUSD()));
        table.addCell(format(d.getTotalCashFlowZWL()));
        table.addCell(String.valueOf(d.getTotalTransactions()));
        table.addCell(String.valueOf(d.getLowStockItems()));
        table.addCell(d.getTopProductName() == null ? "-" : d.getTopProductName());
        table.addCell(String.valueOf(d.getTopProductSold()));

        doc.add(table);
    }

    private Table createTable(int columns) {
        Table table = new Table(UnitValue.createPercentArray(columns));
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private void addHeaderRow(Table table, String[] headers) {
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setBold();
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }
    }

    private String format(Object o) {
        if (o == null) return "-";
        if (o instanceof Double d) return String.format("%.2f", d);
        return o.toString();
    }

    private String formatDate(Object date) {
        if (date == null) return "-";
        if (date instanceof java.time.LocalDateTime dt) return dt.format(FORMATTER);
        return date.toString();
    }
}
