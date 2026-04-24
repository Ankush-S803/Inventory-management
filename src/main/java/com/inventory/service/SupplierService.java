package com.inventory.service;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business logic layer for Supplier operations.
 */
public class SupplierService {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Supplier createSupplier(Supplier supplier) throws SQLException {
        validateSupplier(supplier);
        return supplierDAO.insert(supplier);
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.findAll();
    }

    public Supplier getSupplierById(long id) throws SQLException {
        Supplier s = supplierDAO.findById(id);
        if (s == null) throw new IllegalArgumentException("Supplier with id " + id + " not found");
        return s;
    }

    public boolean updateSupplier(Supplier supplier) throws SQLException {
        if (supplier.getId() == null) throw new IllegalArgumentException("Supplier ID is required for update");
        Supplier existing = supplierDAO.findById(supplier.getId());
        if (existing == null) throw new IllegalArgumentException("Supplier with id " + supplier.getId() + " not found");
        validateSupplier(supplier);
        return supplierDAO.update(supplier);
    }

    public boolean deleteSupplier(long id) throws SQLException {
        Supplier existing = supplierDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Supplier with id " + id + " not found");
        return supplierDAO.delete(id);
    }

    public int getSupplierCount() throws SQLException {
        return supplierDAO.count();
    }

    // ----- Validation -----

    private void validateSupplier(Supplier s) {
        if (s.getName() == null || s.getName().trim().isEmpty())
            throw new IllegalArgumentException("Supplier name is required");
        if (s.getEmail() == null || s.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email is required");
        if (!EMAIL_PATTERN.matcher(s.getEmail()).matches())
            throw new IllegalArgumentException("Invalid email format");
        if (s.getPhoneNumber() == null || s.getPhoneNumber().trim().isEmpty())
            throw new IllegalArgumentException("Phone number is required");
    }
}
