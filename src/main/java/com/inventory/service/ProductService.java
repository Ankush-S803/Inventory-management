package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer for Product operations.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Creates a new product after validation.
     */
    public Product createProduct(Product product) throws SQLException {
        validateProduct(product);

        // Check for duplicate product code
        Product existing = productDAO.findByCode(product.getProductCode());
        if (existing != null) {
            throw new IllegalArgumentException("Product code '" + product.getProductCode() + "' already exists");
        }

        // Default stock values for new product
        if (product.getTotalStock() == null)     product.setTotalStock(0);
        if (product.getAvailableStock() == null) product.setAvailableStock(0);
        if (product.getLowStockThreshold() == null) product.setLowStockThreshold(10);

        return productDAO.insert(product);
    }

    /**
     * Returns all products.
     */
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    /**
     * Returns a product by ID, throws if not found.
     */
    public Product getProductById(long id) throws SQLException {
        Product p = productDAO.findById(id);
        if (p == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        return p;
    }

    /**
     * Updates an existing product (does NOT allow changing stock via this method).
     */
    public boolean updateProduct(Product product) throws SQLException {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for update");
        }
        Product existing = productDAO.findById(product.getId());
        if (existing == null) {
            throw new ProductNotFoundException("Product with id " + product.getId() + " not found");
        }

        validateProduct(product);

        // Check if new code conflicts with another product
        Product byCode = productDAO.findByCode(product.getProductCode());
        if (byCode != null && !byCode.getId().equals(product.getId())) {
            throw new IllegalArgumentException("Product code '" + product.getProductCode() + "' is already in use");
        }

        return productDAO.update(product);
    }

    /**
     * Deletes a product only if it has zero stock.
     */
    public boolean deleteProduct(long id) throws SQLException {
        Product existing = productDAO.findById(id);
        if (existing == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        if (existing.getTotalStock() > 0 || existing.getAvailableStock() > 0) {
            throw new IllegalStateException("Cannot delete product with active stock (total: "
                + existing.getTotalStock() + ", available: " + existing.getAvailableStock() + ")");
        }
        return productDAO.delete(id);
    }

    // ----- Search / Filter -----

    public List<Product> searchByName(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        return productDAO.searchByName(keyword.trim());
    }

    public List<Product> filterByPrice(double min, double max) throws SQLException {
        if (min < 0 || max < 0) throw new IllegalArgumentException("Price values must be positive");
        if (min > max) throw new IllegalArgumentException("Min price cannot be greater than max price");
        return productDAO.filterByPrice(min, max);
    }

    public List<Product> getLowStockProducts() throws SQLException {
        return productDAO.findLowStock();
    }

    public int getProductCount() throws SQLException {
        return productDAO.count();
    }

    // ----- Validation -----

    private void validateProduct(Product p) {
        if (p.getProductCode() == null || p.getProductCode().trim().isEmpty())
            throw new IllegalArgumentException("Product code is required");
        if (p.getName() == null || p.getName().trim().isEmpty())
            throw new IllegalArgumentException("Product name is required");
        if (p.getPrice() == null || p.getPrice() < 0)
            throw new IllegalArgumentException("Price must be a non-negative value");
        if (p.getLowStockThreshold() != null && p.getLowStockThreshold() < 0)
            throw new IllegalArgumentException("Low stock threshold must be non-negative");
    }
}
