package com.inventory.dao;

import com.inventory.model.StockTransaction;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the stock_transactions table.
 */
public class StockTransactionDAO {

    private static final String INSERT_SQL =
        "INSERT INTO stock_transactions (reference_code, product_id, quantity, transaction_type, status) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_SQL =
        "SELECT st.*, p.name AS product_name FROM stock_transactions st " +
        "JOIN products p ON st.product_id = p.id ORDER BY st.id DESC";

    private static final String SELECT_BY_ID_SQL =
        "SELECT st.*, p.name AS product_name FROM stock_transactions st " +
        "JOIN products p ON st.product_id = p.id WHERE st.id = ?";

    private static final String SELECT_BY_PRODUCT_SQL =
        "SELECT st.*, p.name AS product_name FROM stock_transactions st " +
        "JOIN products p ON st.product_id = p.id WHERE st.product_id = ? ORDER BY st.id DESC";

    private static final String UPDATE_STATUS_SQL =
        "UPDATE stock_transactions SET status = ? WHERE id = ?";

    private static final String COUNT_SQL =
        "SELECT COUNT(*) FROM stock_transactions";

    // --------------------------------------------------

    /**
     * Inserts a transaction within an existing connection/transaction.
     */
    public StockTransaction insert(Connection conn, StockTransaction txn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, txn.getReferenceCode());
            ps.setLong(2, txn.getProductId());
            ps.setInt(3, txn.getQuantity());
            ps.setString(4, txn.getTransactionType());
            ps.setString(5, txn.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    txn.setId(keys.getLong(1));
                }
            }
            return txn;
        }
    }

    public List<StockTransaction> findAll() throws SQLException {
        List<StockTransaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public StockTransaction findById(long id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<StockTransaction> findByProduct(long productId) throws SQLException {
        List<StockTransaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PRODUCT_SQL)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Updates transaction status within an existing connection/transaction.
     */
    public boolean updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // --------------------------------------------------

    private StockTransaction mapRow(ResultSet rs) throws SQLException {
        StockTransaction t = new StockTransaction();
        t.setId(rs.getLong("id"));
        t.setReferenceCode(rs.getString("reference_code"));
        t.setProductId(rs.getLong("product_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setTransactionDate(rs.getString("transaction_date"));
        t.setStatus(rs.getString("status"));
        t.setProductName(rs.getString("product_name"));
        return t;
    }
}
