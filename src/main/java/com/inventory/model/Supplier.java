package com.inventory.model;

/**
 * Supplier entity representing a row in the suppliers table.
 */
public class Supplier {

    private Long   id;
    private String name;
    private String email;
    private String phoneNumber;

    public Supplier() { }

    public Supplier(String name, String email, String phoneNumber) {
        this.name        = name;
        this.email       = email;
        this.phoneNumber = phoneNumber;
    }

    // ---------- Getters & Setters ----------

    public Long   getId()                          { return id; }
    public void   setId(Long id)                   { this.id = id; }

    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }

    public String getEmail()                       { return email; }
    public void   setEmail(String email)           { this.email = email; }

    public String getPhoneNumber()                 { return phoneNumber; }
    public void   setPhoneNumber(String phone)     { this.phoneNumber = phone; }
}
