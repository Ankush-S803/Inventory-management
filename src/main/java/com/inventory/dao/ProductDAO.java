package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the products table.
 */
public class ProductDAO {

    // ----- SQL Constants -----
    private static final String INSERT_SQL =
        "INSERT INTO products (product_code, name, description, price, total_stock, available_stock, low_stock_threshold) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_SQL =
        "SELECT * FROM products ORDER BY id DESC";

    private static final String SELECT_BY_ID_SQL =
        "SELECT * FROM products WHERE id = ?";

    private static final String SELECT_BY_CODE_SQL =
        "SELECT * FROM products WHERE product_code = ?";

    private static final String UPDATE_SQL =
        "UPDATE products SET product_code = ?, name = ?, description = ?, price = ?, low_stock_threshold = ? WHERE id = ?";

    private static final String UPDATE_STOCK_SQL =
        "UPDATE products SET total_stock = ?, available_stock = ? WHERE id = ?";

    private static final String DELETE_SQL =
        "DELETE FROM products WHERE id = ?";

    private static final String SEARCH_BY_NAME_SQL =
        "SELECT * FROM products WHERE name LIKE ? ORDER BY name";

    private static final String FILTER_BY_PRICE_SQL =
        "SELECT * FROM products WHERE price BETWEEN ? AND ? ORDER BY price";

    private static final String LOW_STOCK_SQL =
        "SELECT * FROM products WHERE available_stock <= low_stock_threshold ORDER BY available_stock ASC";

    private static final String COUNT_SQL =
        "SELECT COUNT(*) FROM products";

    // --------------------------------------------------
    // CRUD Operations
    // --------------------------------------------------

    public Product insert(Product p) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getTotalStock()     != null ? p.getTotalStock()     : 0);
            ps.setInt(6, p.getAvailableStock() != null ? p.getAvailableStock() : 0);
            ps.setInt(7, p.getLowStockThreshold() != null ? p.getLowStockThreshold() : 10);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getLong(1));
                }
            }
            return p;
        }
    }

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Product findById(long id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Product findByCode(String code) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CODE_SQL)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean update(Product p) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getLowStockThreshold());
            ps.setLong(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates stock quantities. Should be called within an existing transaction.
     */
    public boolean updateStock(Connection conn, long productId, int totalStock, int availableStock) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STOCK_SQL)) {
            ps.setInt(1, totalStock);
            ps.setInt(2, availableStock);
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // --------------------------------------------------
    // Search / Filter
    // --------------------------------------------------

    public List<Product> searchByName(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_BY_NAME_SQL)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Product> filterByPrice(double min, double max) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FILTER_BY_PRICE_SQL)) {
            ps.setDouble(1, min);
            ps.setDouble(2, max);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Product> findLowStock() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(LOW_STOCK_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // --------------------------------------------------
    // Row Mapper
    // --------------------------------------------------

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setProductCode(rs.getString("product_code"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setTotalStock(rs.getInt("total_stock"));
        p.setAvailableStock(rs.getInt("available_stock"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        return p;
    }
}
