package com.inventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.StockTransaction;
import com.inventory.service.StockService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet handling stock transactions (IN / OUT / cancel).
 * URL patterns: /stock, /stock/in, /stock/out, /stock/cancel
 */
@WebServlet(urlPatterns = {"/stock", "/stock/in", "/stock/out", "/stock/cancel"})
public class StockServlet extends HttpServlet {

    private final StockService stockService = new StockService();
    private final Gson gson = new Gson();

    // ----- GET: List transactions or by ID -----
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isEmpty()) {
                long id = Long.parseLong(idParam);
                StockTransaction txn = stockService.getTransactionById(id);
                out.print(gson.toJson(wrapSuccess(txn)));
            } else {
                List<StockTransaction> list = stockService.getAllTransactions();
                out.print(gson.toJson(wrapSuccess(list)));
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    // ----- POST: Stock IN or OUT -----
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String path = req.getServletPath();

        try {
            JsonObject body = gson.fromJson(readBody(req), JsonObject.class);
            long productId = body.get("productId").getAsLong();
            int quantity   = body.get("quantity").getAsInt();

            StockTransaction txn;

            if ("/stock/in".equals(path)) {
                txn = stockService.stockIn(productId, quantity);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(wrapSuccess(txn, "Stock IN recorded successfully")));
            } else if ("/stock/out".equals(path)) {
                txn = stockService.stockOut(productId, quantity);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(wrapSuccess(txn, "Stock OUT recorded successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(wrapError("Use /stock/in or /stock/out")));
            }

        } catch (InsufficientStockException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (ProductNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    // ----- PUT: Cancel transaction -----
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String path = req.getServletPath();

        try {
            if ("/stock/cancel".equals(path)) {
                JsonObject body = gson.fromJson(readBody(req), JsonObject.class);
                long txnId = body.get("id").getAsLong();
                StockTransaction txn = stockService.cancelTransaction(txnId);
                out.print(gson.toJson(wrapSuccess(txn, "Transaction cancelled successfully")));
            } else {
                // Generic PUT on /stock — also treat as cancel
                JsonObject body = gson.fromJson(readBody(req), JsonObject.class);
                long txnId = body.get("id").getAsLong();
                StockTransaction txn = stockService.cancelTransaction(txnId);
                out.print(gson.toJson(wrapSuccess(txn, "Transaction cancelled successfully")));
            }
        } catch (InsufficientStockException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (IllegalStateException | IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    // ----- Utility Methods -----

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private JsonObject wrapSuccess(Object data) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "success");
        json.add("data", gson.toJsonTree(data));
        return json;
    }

    private JsonObject wrapSuccess(Object data, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "success");
        json.addProperty("message", message);
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
