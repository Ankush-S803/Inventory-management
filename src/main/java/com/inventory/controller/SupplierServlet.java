package com.inventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inventory.model.Supplier;
import com.inventory.service.SupplierService;

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
 * Servlet handling CRUD operations for Suppliers.
 * Mapped to: /suppliers
 */
@WebServlet("/suppliers")
public class SupplierServlet extends HttpServlet {

    private final SupplierService supplierService = new SupplierService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isEmpty()) {
                long id = Long.parseLong(idParam);
                Supplier supplier = supplierService.getSupplierById(id);
                out.print(gson.toJson(wrapSuccess(supplier)));
            } else {
                List<Supplier> suppliers = supplierService.getAllSuppliers();
                out.print(gson.toJson(wrapSuccess(suppliers)));
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Supplier supplier = gson.fromJson(readBody(req), Supplier.class);
            Supplier created = supplierService.createSupplier(supplier);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(wrapSuccess(created, "Supplier created successfully")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Supplier supplier = gson.fromJson(readBody(req), Supplier.class);
            supplierService.updateSupplier(supplier);
            Supplier updated = supplierService.getSupplierById(supplier.getId());
            out.print(gson.toJson(wrapSuccess(updated, "Supplier updated successfully")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(wrapError("Supplier ID is required")));
                return;
            }
            long id = Long.parseLong(idParam);
            supplierService.deleteSupplier(id);
            out.print(gson.toJson(wrapSuccess(null, "Supplier deleted successfully")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

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
