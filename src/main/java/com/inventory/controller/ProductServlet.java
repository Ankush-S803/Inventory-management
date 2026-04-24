package com.inventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.service.ProductService;

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
 * Servlet handling CRUD operations for Products.
 * Mapped to: /products
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final Gson gson = new Gson();

    // ----- GET: Fetch all or by ID -----
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");

            if (idParam != null && !idParam.isEmpty()) {
                long id = Long.parseLong(idParam);
                Product product = productService.getProductById(id);
                out.print(gson.toJson(wrapSuccess(product)));
            } else {
                List<Product> products = productService.getAllProducts();
                out.print(gson.toJson(wrapSuccess(products)));
            }
        } catch (ProductNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError("Invalid product ID format")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    // ----- POST: Create product -----
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Product product = gson.fromJson(readBody(req), Product.class);
            Product created = productService.createProduct(product);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(wrapSuccess(created, "Product created successfully")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(wrapError("Server error: " + e.getMessage())));
        }
    }

    // ----- PUT: Update product -----
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Product product = gson.fromJson(readBody(req), Product.class);
            productService.updateProduct(product);
            Product updated = productService.getProductById(product.getId());
            out.print(gson.toJson(wrapSuccess(updated, "Product updated successfully")));
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

    // ----- DELETE: Delete product -----
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(wrapError("Product ID is required")));
                return;
            }
            long id = Long.parseLong(idParam);
            productService.deleteProduct(id);
            out.print(gson.toJson(wrapSuccess(null, "Product deleted successfully")));
        } catch (ProductNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(wrapError(e.getMessage())));
        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
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
