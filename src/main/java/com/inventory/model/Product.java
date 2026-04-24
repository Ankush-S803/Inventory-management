package com.inventory.model;

/**
 * Product entity representing a row in the products table.
 */
public class Product {

    private Long    id;
    private String  productCode;
    private String  name;
    private String  description;
    private Double  price;
    private Integer totalStock;
    private Integer availableStock;
    private Integer lowStockThreshold;

    public Product() { }

    public Product(String productCode, String name, String description,
                   Double price, Integer lowStockThreshold) {
        this.productCode       = productCode;
        this.name              = name;
        this.description       = description;
        this.price             = price;
        this.totalStock        = 0;
        this.availableStock    = 0;
        this.lowStockThreshold = lowStockThreshold;
    }

    // ---------- Getters & Setters ----------

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getProductCode()              { return productCode; }
    public void setProductCode(String code)     { this.productCode = code; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public Double getPrice()                    { return price; }
    public void setPrice(Double price)          { this.price = price; }

    public Integer getTotalStock()              { return totalStock; }
    public void setTotalStock(Integer ts)       { this.totalStock = ts; }

    public Integer getAvailableStock()          { return availableStock; }
    public void setAvailableStock(Integer as)   { this.availableStock = as; }

    public Integer getLowStockThreshold()       { return lowStockThreshold; }
    public void setLowStockThreshold(Integer t) { this.lowStockThreshold = t; }
}
