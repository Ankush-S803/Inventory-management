package com.inventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inventory.service.ProductService;
import com.inventory.service.StockService;
import com.inventory.service.SupplierService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dashboard API — returns aggregate counts for the home page.
 * Mapped to: /api/dashboard
 */
@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ProductService  productService  = new ProductService();
    private final StockService    stockService    = new StockService();
    private final SupplierService supplierService = new SupplierService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            JsonObject data = new JsonObject();
            data.addProperty("totalProducts",      productService.getProductCount());
            data.addProperty("totalTransactions",   stockService.getTransactionCount());
            data.addProperty("totalSuppliers",      supplierService.getSupplierCount());
            data.addProperty("lowStockCount",       productService.getLowStockProducts().size());

            JsonObject result = new JsonObject();
            result.addProperty("status", "success");
            result.add("data", data);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Server error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
}
