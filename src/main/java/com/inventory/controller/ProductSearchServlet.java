package com.inventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inventory.model.Product;
import com.inventory.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet handling product search, filter, and low-stock queries.
 * URL patterns: /products/search, /products/filter, /products/low-stock
 */
@WebServlet(urlPatterns = {"/products/search", "/products/filter", "/products/low-stock"})
public class ProductSearchServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String path = req.getServletPath();

        try {
            switch (path) {
                case "/products/search" -> {
                    String name = req.getParameter("name");
                    if (name == null || name.trim().isEmpty()) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(wrapError("Search parameter 'name' is required")));
                        return;
                    }
                    List<Product> results = productService.searchByName(name);
                    out.print(gson.toJson(wrapSuccess(results)));
                }
                case "/products/filter" -> {
                    String minStr = req.getParameter("minPrice");
                    String maxStr = req.getParameter("maxPrice");
                    if (minStr == null || maxStr == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(wrapError("Both 'minPrice' and 'maxPrice' are required")));
                        return;
                    }
                    double min = Double.parseDouble(minStr);
                    double max = Double.parseDouble(maxStr);
                    List<Product> results = productService.filterByPrice(min, max);
                    out.print(gson.toJson(wrapSuccess(results)));
                }
                case "/products/low-stock" -> {
                    List<Product> results = productService.getLowStockProducts();
                    out.print(gson.toJson(wrapSuccess(results)));
                }
                default -> {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(wrapError("Unknown endpoint")));
                }
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError("Invalid number format")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    private JsonObject wrapSuccess(Object data) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "success");
        json.add("data", gson.toJsonTree(data));
        return json;
    }

    private JsonObject wrapError(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", message);
        return json;
    }
}
