package com.inventory.model;

/**
 * StockTransaction entity representing a row in the stock_transactions table.
 */
public class StockTransaction {

    private Long   id;
    private String referenceCode;
    private Long   productId;
    private Integer quantity;
    private String transactionType;   // "IN" or "OUT"
    private String transactionDate;
    private String status;            // "COMPLETED" or "CANCELLED"

    /* Transient field – populated when joining with products */
    private String productName;

    public StockTransaction() { }

    public StockTransaction(Long productId, Integer quantity, String transactionType) {
        this.productId       = productId;
        this.quantity        = quantity;
        this.transactionType = transactionType;
        this.status          = "COMPLETED";
    }

    // ---------- Getters & Setters ----------

    public Long    getId()                              { return id; }
    public void    setId(Long id)                       { this.id = id; }

    public String  getReferenceCode()                   { return referenceCode; }
    public void    setReferenceCode(String code)        { this.referenceCode = code; }

    public Long    getProductId()                       { return productId; }
    public void    setProductId(Long productId)         { this.productId = productId; }

    public Integer getQuantity()                        { return quantity; }
    public void    setQuantity(Integer quantity)        { this.quantity = quantity; }

    public String  getTransactionType()                 { return transactionType; }
    public void    setTransactionType(String type)      { this.transactionType = type; }

    public String  getTransactionDate()                 { return transactionDate; }
    public void    setTransactionDate(String date)      { this.transactionDate = date; }

    public String  getStatus()                          { return status; }
    public void    setStatus(String status)             { this.status = status; }

    public String  getProductName()                     { return productName; }
    public void    setProductName(String productName)   { this.productName = productName; }
}
