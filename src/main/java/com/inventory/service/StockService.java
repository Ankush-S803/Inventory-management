package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.StockTransactionDAO;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.model.StockTransaction;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Business logic layer for Stock Transaction operations.
 * Handles atomic stock IN/OUT and cancellation with JDBC transactions.
 */
public class StockService {

    private final StockTransactionDAO txnDAO     = new StockTransactionDAO();
    private final ProductDAO          productDAO = new ProductDAO();

    /**
     * Performs a Stock-IN transaction: increases both totalStock and availableStock.
     */
    public StockTransaction stockIn(long productId, int quantity) throws SQLException {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Product product = productDAO.findById(productId);
            if (product == null) throw new ProductNotFoundException("Product with id " + productId + " not found");

            // Update stock
            int newTotal     = product.getTotalStock()     + quantity;
            int newAvailable = product.getAvailableStock() + quantity;
            productDAO.updateStock(conn, productId, newTotal, newAvailable);

            // Record transaction
            StockTransaction txn = new StockTransaction(productId, quantity, "IN");
            txn.setReferenceCode(generateRefCode());
            txn.setStatus("COMPLETED");
            txnDAO.insert(conn, txn);

            conn.commit();

            txn.setProductName(product.getName());
            return txn;

        } catch (Exception e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            DBConnection.closeQuietly(conn);
        }
    }

    /**
     * Performs a Stock-OUT transaction: decreases availableStock.
     * Throws InsufficientStockException if not enough stock.
     */
    public StockTransaction stockOut(long productId, int quantity) throws SQLException {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Product product = productDAO.findById(productId);
            if (product == null) throw new ProductNotFoundException("Product with id " + productId + " not found");

            if (product.getAvailableStock() < quantity) {
                throw new InsufficientStockException(
                    "Insufficient stock. Available: " + product.getAvailableStock() + ", Requested: " + quantity);
            }

            // Update stock
            int newTotal     = product.getTotalStock()     - quantity;
            int newAvailable = product.getAvailableStock() - quantity;
            productDAO.updateStock(conn, productId, newTotal, newAvailable);

            // Record transaction
            StockTransaction txn = new StockTransaction(productId, quantity, "OUT");
            txn.setReferenceCode(generateRefCode());
            txn.setStatus("COMPLETED");
            txnDAO.insert(conn, txn);

            conn.commit();

            txn.setProductName(product.getName());
            return txn;

        } catch (Exception e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            DBConnection.closeQuietly(conn);
        }
    }

    /**
     * Cancels a completed transaction and reverses the stock change.
     */
    public StockTransaction cancelTransaction(long txnId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            StockTransaction txn = txnDAO.findById(txnId);
            if (txn == null) throw new IllegalArgumentException("Transaction with id " + txnId + " not found");

            if ("CANCELLED".equals(txn.getStatus())) {
                throw new IllegalStateException("Transaction is already cancelled");
            }

            Product product = productDAO.findById(txn.getProductId());
            if (product == null) throw new ProductNotFoundException("Associated product not found");

            // Reverse stock effect
            int newTotal, newAvailable;
            if ("IN".equals(txn.getTransactionType())) {
                newTotal     = product.getTotalStock()     - txn.getQuantity();
                newAvailable = product.getAvailableStock() - txn.getQuantity();
                if (newAvailable < 0) {
                    throw new InsufficientStockException(
                        "Cannot cancel IN transaction — stock has already been issued out");
                }
            } else { // OUT
                newTotal     = product.getTotalStock()     + txn.getQuantity();
                newAvailable = product.getAvailableStock() + txn.getQuantity();
            }

            productDAO.updateStock(conn, txn.getProductId(), newTotal, newAvailable);
            txnDAO.updateStatus(conn, txnId, "CANCELLED");

            conn.commit();

            txn.setStatus("CANCELLED");
            return txn;

        } catch (Exception e) {
            DBConnection.rollbackQuietly(conn);
            throw e;
        } finally {
            DBConnection.closeQuietly(conn);
        }
    }

    public List<StockTransaction> getAllTransactions() throws SQLException {
        return txnDAO.findAll();
    }

    public StockTransaction getTransactionById(long id) throws SQLException {
        StockTransaction txn = txnDAO.findById(id);
        if (txn == null) throw new IllegalArgumentException("Transaction with id " + id + " not found");
        return txn;
    }

    public int getTransactionCount() throws SQLException {
        return txnDAO.count();
    }

    // --------------------------------------------------

    private String generateRefCode() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
