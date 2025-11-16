package com.company.retail.report;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.expense.ExpenseModel;
import com.company.retail.expense.ExpenseRepository;
import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.purchaseorder.PurchaseOrderRepository;
import com.company.retail.report.dto.*;
import com.company.retail.saleItem.SaleItemModel;
import com.company.retail.saleItem.SaleItemRepository;
import com.company.retail.sales.SalesModel;
import com.company.retail.sales.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ShopStockRepository shopStockRepository;

    private final ReportExcelExporter excelExporter;
    private final ReportPdfExporter pdfExporter;

    // ============================================================
    // 🧾 SALES REPORT
    // ============================================================
    public List<SalesReportDTO> generateSalesReport(LocalDate start, LocalDate end, Long shopId) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

        return saleRepository.findAll().stream()
                .filter(s -> (shopId == null || s.getShop().getId().equals(shopId)) &&
                        !s.getSaleDate().isBefore(startTime) && !s.getSaleDate().isAfter(endTime))
                .map(sale -> {
                    List<SaleItemModel> items = saleItemRepository.findBySale_SaleId(sale.getSaleId());
                    double profitUSD = items.stream().mapToDouble(SaleItemModel::getProfitUSD).sum();
                    double profitZWL = items.stream().mapToDouble(SaleItemModel::getProfitZWL).sum();
                    int totalItems = items.stream().mapToInt(SaleItemModel::getQuantity).sum();

                    return SalesReportDTO.builder()
                            .saleId(sale.getSaleId())
                            .shopName(sale.getShop().getShopName())
                            .cashierName(sale.getCashier().getUsername())
                            .totalAmountUSD(sale.getTotalAmountUSD())
                            .totalAmountZWL(sale.getTotalAmountZWL())
                            .totalProfitUSD(profitUSD)
                            .totalProfitZWL(profitZWL)
                            .totalItemsSold(totalItems)
                            .saleDate(sale.getSaleDate())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // 📦 STOCK REPORT (ShopStock-based)
    // ============================================================
    public List<StockReportDTO> generateStockReport(Long shopId) {
        List<ShopStockModel> stocks = (shopId == null)
                ? shopStockRepository.findAll()
                : shopStockRepository.findByShop_Id(shopId);

        return stocks.stream()
                .map(s -> {
                    ProductModel p = s.getProduct();

                    double qty = Optional.ofNullable(s.getQuantityInStock()).orElse(0);
                    double costUSD = Optional.ofNullable(s.getAvgLandingCostUSD()).orElse(0.0);
                    double costZWL = Optional.ofNullable(s.getAvgLandingCostZWL()).orElse(0.0);
                    double sellUSD = Optional.ofNullable(p.getSellingPriceUSD()).orElse(0.0);
                    double sellZWL = Optional.ofNullable(p.getSellingPriceZWL()).orElse(0.0);

                    double costValUSD = qty * costUSD;
                    double costValZWL = qty * costZWL;
                    double sellValUSD = qty * sellUSD;
                    double sellValZWL = qty * sellZWL;

                    return StockReportDTO.builder()
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .category(p.getCategory())
                            .shopName(s.getShop().getShopName())
                            .currentQuantity((int) qty)
                            .reorderLevel(p.getReorderLevel())
                            .costPriceUSD(costUSD)
                            .costPriceZWL(costZWL)
                            .sellingPriceUSD(sellUSD)
                            .sellingPriceZWL(sellZWL)
                            .totalValueAtCostUSD(costValUSD)
                            .totalValueAtCostZWL(costValZWL)
                            .totalValueAtSellingUSD(sellValUSD)
                            .totalValueAtSellingZWL(sellValZWL)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // 💰 EXPENSE REPORT
    // ============================================================
    public List<ExpenseReportDTO> generateExpenseReport(LocalDate start, LocalDate end, Long shopId) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

        return expenseRepository.findAll().stream()
                .filter(e -> (shopId == null || e.getShop().getId().equals(shopId)) &&
                        !e.getDate().isBefore(startTime) && !e.getDate().isAfter(endTime))
                .map(e -> ExpenseReportDTO.builder()
                        .expenseId(e.getExpenseId())
                        .shopName(e.getShop().getShopName())
                        .category(e.getCategory().toString())
                        .description(e.getDescription())
                        .amountUSD(e.getAmountUSD())
                        .amountZWL(e.getAmountZWL())
                        .date(e.getDate())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================================
    // 🧾 PURCHASE REPORT
    // ============================================================
    public List<PurchaseReportDTO> generatePurchaseReport(LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

        return purchaseOrderRepository.findAll().stream()
                .filter(o -> !o.getOrderDate().isBefore(startTime) && !o.getOrderDate().isAfter(endTime))
                .map(o -> PurchaseReportDTO.builder()
                        .purchaseOrderId(o.getPurchaseOrderId())
                        .shopName(o.getShop().getShopName())
                        .supplierName(o.getSupplierName())
                        .totalCostUSD(o.getTotalCostUSD())
                        .totalCostZWL(o.getTotalCostZWL())
                        .orderDate(o.getOrderDate())
                        .receivedDate(o.getReceivedDate())
                        .status(o.getStatus().toString())
                        .totalItems(o.getItems().size())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================================
    // 🟣 PROFIT REPORT
    // ============================================================
    public ProfitReportDTO generateProfitReport(LocalDate start, LocalDate end, Long shopId) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

        var sales = saleRepository.findAll().stream()
                .filter(s -> (shopId == null || s.getShop().getId().equals(shopId)) &&
                        !s.getSaleDate().isBefore(startTime) && !s.getSaleDate().isAfter(endTime))
                .toList();

        double totalSalesUSD = sales.stream().mapToDouble(SalesModel::getTotalAmountUSD).sum();
        double totalSalesZWL = sales.stream().mapToDouble(SalesModel::getTotalAmountZWL).sum();

        double totalCostUSD = saleItemRepository.findAll().stream()
                .filter(i -> (shopId == null || i.getSale().getShop().getId().equals(shopId)) &&
                        !i.getSale().getSaleDate().isBefore(startTime) && !i.getSale().getSaleDate().isAfter(endTime))
                .mapToDouble(i -> i.getCostPriceUSD() * i.getQuantity()).sum();

        double totalCostZWL = saleItemRepository.findAll().stream()
                .filter(i -> (shopId == null || i.getSale().getShop().getId().equals(shopId)) &&
                        !i.getSale().getSaleDate().isBefore(startTime) && !i.getSale().getSaleDate().isAfter(endTime))
                .mapToDouble(i -> i.getCostPriceZWL() * i.getQuantity()).sum();

        double totalExpensesUSD = expenseRepository.findAll().stream()
                .filter(e -> (shopId == null || e.getShop().getId().equals(shopId)) &&
                        !e.getDate().isBefore(startTime) && !e.getDate().isAfter(endTime))
                .mapToDouble(ExpenseModel::getAmountUSD).sum();

        double totalExpensesZWL = expenseRepository.findAll().stream()
                .filter(e -> (shopId == null || e.getShop().getId().equals(shopId)) &&
                        !e.getDate().isBefore(startTime) && !e.getDate().isAfter(endTime))
                .mapToDouble(ExpenseModel::getAmountZWL).sum();

        double grossProfitUSD = totalSalesUSD - totalCostUSD;
        double grossProfitZWL = totalSalesZWL - totalCostZWL;
        double netProfitUSD = grossProfitUSD - totalExpensesUSD;
        double netProfitZWL = grossProfitZWL - totalExpensesZWL;

        String shopName = (shopId == null)
                ? "All Shops"
                : (sales.isEmpty() ? "Unknown Shop" : sales.get(0).getShop().getShopName());

        return ProfitReportDTO.builder()
                .shopName(shopName)
                .totalSalesUSD(totalSalesUSD)
                .totalSalesZWL(totalSalesZWL)
                .totalCostOfGoodsUSD(totalCostUSD)
                .totalCostOfGoodsZWL(totalCostZWL)
                .totalExpensesUSD(totalExpensesUSD)
                .totalExpensesZWL(totalExpensesZWL)
                .grossProfitUSD(grossProfitUSD)
                .grossProfitZWL(grossProfitZWL)
                .netProfitUSD(netProfitUSD)
                .netProfitZWL(netProfitZWL)
                .period(start + " to " + end)
                .build();
    }

    // ============================================================
    // 🟤 CASH FLOW REPORT
    // ============================================================
    public CashflowReportDTO generateCashflowReport(LocalDate start, LocalDate end, Long shopId) {
        ProfitReportDTO profit = generateProfitReport(start, end, shopId);

        double inflowsUSD = profit.getTotalSalesUSD();
        double inflowsZWL = profit.getTotalSalesZWL();
        double outflowsUSD = profit.getTotalExpensesUSD() + profit.getTotalCostOfGoodsUSD();
        double outflowsZWL = profit.getTotalExpensesZWL() + profit.getTotalCostOfGoodsZWL();

        return CashflowReportDTO.builder()
                .shopName(profit.getShopName())
                .period(profit.getPeriod())
                .inflowsUSD(inflowsUSD)
                .inflowsZWL(inflowsZWL)
                .outflowsUSD(outflowsUSD)
                .outflowsZWL(outflowsZWL)
                .netCashFlowUSD(inflowsUSD - outflowsUSD)
                .netCashFlowZWL(inflowsZWL - outflowsZWL)
                .openingBalanceUSD(0.0)
                .openingBalanceZWL(0.0)
                .closingBalanceUSD(inflowsUSD - outflowsUSD)
                .closingBalanceZWL(inflowsZWL - outflowsZWL)
                .build();
    }

    // ============================================================
    // 📊 DASHBOARD SUMMARY
    // ============================================================
    public DashboardSummaryDTO generateDashboardSummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        var sales = saleRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .toList();

        double totalSalesUSD = sales.stream().mapToDouble(SalesModel::getTotalAmountUSD).sum();
        double totalSalesZWL = sales.stream().mapToDouble(SalesModel::getTotalAmountZWL).sum();

        double totalProfitUSD = saleItemRepository.findAll().stream()
                .filter(i -> !i.getSale().getSaleDate().isBefore(start) && !i.getSale().getSaleDate().isAfter(end))
                .mapToDouble(SaleItemModel::getProfitUSD).sum();

        double totalProfitZWL = saleItemRepository.findAll().stream()
                .filter(i -> !i.getSale().getSaleDate().isBefore(start) && !i.getSale().getSaleDate().isAfter(end))
                .mapToDouble(SaleItemModel::getProfitZWL).sum();

        double totalExpensesUSD = expenseRepository.findAll().stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .mapToDouble(ExpenseModel::getAmountUSD).sum();

        double totalExpensesZWL = expenseRepository.findAll().stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .mapToDouble(ExpenseModel::getAmountZWL).sum();

        long lowStockItems = shopStockRepository.findAll().stream()
                .filter(s -> s.getQuantityInStock() <= Optional.ofNullable(s.getProduct().getReorderLevel()).orElse(0))
                .count();

        Optional<Map.Entry<String, Integer>> topProduct = saleItemRepository.findAll().stream()
                .filter(i -> !i.getSale().getSaleDate().isBefore(start) && !i.getSale().getSaleDate().isAfter(end))
                .collect(Collectors.groupingBy(i -> i.getProduct().getProductName(),
                        Collectors.summingInt(SaleItemModel::getQuantity)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());

        return DashboardSummaryDTO.builder()
                .totalSalesUSD(totalSalesUSD)
                .totalSalesZWL(totalSalesZWL)
                .totalProfitUSD(totalProfitUSD)
                .totalProfitZWL(totalProfitZWL)
                .totalExpensesUSD(totalExpensesUSD)
                .totalExpensesZWL(totalExpensesZWL)
                .totalCashFlowUSD(totalSalesUSD - totalExpensesUSD)
                .totalCashFlowZWL(totalSalesZWL - totalExpensesZWL)
                .totalTransactions(sales.size())
                .lowStockItems((int) lowStockItems)
                .topProductName(topProduct.map(Map.Entry::getKey).orElse(null))
                .topProductSold(topProduct.map(Map.Entry::getValue).orElse(0))
                .build();
    }

    // ============================================================
    // 📤 EXPORT
    // ============================================================
    public byte[] exportToExcel(String type, LocalDate start, LocalDate end) {
        List<?> data = switch (type.toLowerCase()) {
            case "sales" -> generateSalesReport(start, end, null);
            case "stock" -> generateStockReport(null);
            case "expenses" -> generateExpenseReport(start, end, null);
            case "purchases" -> generatePurchaseReport(start, end);
            case "profit" -> List.of(generateProfitReport(start, end, null));
            case "cashflow" -> List.of(generateCashflowReport(start, end, null));
            case "dashboard" -> List.of(generateDashboardSummary(start));
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        };
        return excelExporter.export(type, data);
    }

    public byte[] exportToPdf(String type, LocalDate start, LocalDate end) {
        List<?> data = switch (type.toLowerCase()) {
            case "sales" -> generateSalesReport(start, end, null);
            case "stock" -> generateStockReport(null);
            case "expenses" -> generateExpenseReport(start, end, null);
            case "purchases" -> generatePurchaseReport(start, end);
            case "profit" -> List.of(generateProfitReport(start, end, null));
            case "cashflow" -> List.of(generateCashflowReport(start, end, null));
            case "dashboard" -> List.of(generateDashboardSummary(start));
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        };
        return pdfExporter.export(type, data);
    }
}