# 📦 Product Inventory & Stock Management System

A complete Java Servlet-based web application for managing products, stock levels, and inventory transactions.  
Built with MVC architecture using JDBC + MySQL.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Backend      | Java 17+, Jakarta Servlets 6.0      |
| Database     | MySQL 8.0+                          |
| ORM          | JDBC (PreparedStatement, manual)    |
| JSON         | Gson 2.10                           |
| Server       | Apache Tomcat 10.1+                 |
| Frontend     | HTML5, CSS3, Vanilla JavaScript     |
| Architecture | MVC (Servlet → Service → DAO)       |

---

## Project Structure

```
InventoryManagementSystem/
├── sql/
│   └── schema.sql                          # MySQL schema
├── src/main/
│   ├── java/com/inventory/
│   │   ├── controller/                     # Servlets (Controllers)
│   │   │   ├── ProductServlet.java
│   │   │   ├── ProductSearchServlet.java
│   │   │   ├── StockServlet.java
│   │   │   ├── SupplierServlet.java
│   │   │   ├── DashboardServlet.java
│   │   │   └── CORSFilter.java
│   │   ├── service/                        # Business Logic
│   │   │   ├── ProductService.java
│   │   │   ├── StockService.java
│   │   │   └── SupplierService.java
│   │   ├── dao/                            # Data Access (JDBC)
│   │   │   ├── ProductDAO.java
│   │   │   ├── StockTransactionDAO.java
│   │   │   └── SupplierDAO.java
│   │   ├── model/                          # Entity POJOs
│   │   │   ├── Product.java
│   │   │   ├── Supplier.java
│   │   │   └── StockTransaction.java
│   │   ├── exception/                      # Custom Exceptions
│   │   │   ├── ProductNotFoundException.java
│   │   │   └── InsufficientStockException.java
│   │   └── util/
│   │       └── DBConnection.java           # JDBC Utility
│   └── webapp/
│       ├── WEB-INF/web.xml
│       ├── css/style.css
│       ├── js/app.js
│       ├── index.html                      # Dashboard
│       ├── products.html                   # Product CRUD
│       ├── stock.html                      # Stock Transactions
│       ├── suppliers.html                  # Supplier CRUD
│       └── low-stock.html                  # Low Stock Alerts
└── README.md
```

---

## Setup & Deployment

### Prerequisites
1. **Java JDK 17+** installed
2. **Apache Tomcat 10.1+** installed
3. **MySQL 8.0+** installed and running

### Step 1: Database Setup

```sql
-- Run the schema file in MySQL
mysql -u root -p < sql/schema.sql
```

Or open MySQL Workbench and execute `sql/schema.sql`.

### Step 2: Configure Database Connection

Set environment variables (or edit `DBConnection.java` defaults):

```bash
# Windows (Command Prompt)
set DB_URL=jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USER=root
set DB_PASSWORD=your_password

# Windows (PowerShell)
$env:DB_URL = "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_password"
```

### Step 3: Add Required JARs

Download and place these JARs in Tomcat's `lib/` folder OR in `WEB-INF/lib/`:

| JAR                   | Download                                                            |
|-----------------------|---------------------------------------------------------------------|
| `mysql-connector-j`   | https://dev.mysql.com/downloads/connector/j/                        |
| `gson-2.10.1.jar`     | https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/   |

### Step 4: Compile & Deploy

#### Option A: Manual Compilation

```bash
# Compile all Java files
javac -cp "path/to/tomcat/lib/*;WEB-INF/lib/*" -d WEB-INF/classes src/main/java/com/inventory/**/*.java

# Copy compiled classes to webapp
# Copy the entire webapp folder as a Tomcat webapp
```

#### Option B: Use IDE (Recommended)

1. Open project in **IntelliJ IDEA** or **Eclipse**
2. Configure Tomcat as the server
3. Add Servlet API, Gson, and MySQL Connector as dependencies
4. Run on server

### Step 5: Access Application

```
http://localhost:8080/InventoryManagementSystem/
```

---

## API Endpoints

### Products

| Method | URL                                      | Description        |
|--------|------------------------------------------|--------------------|
| GET    | `/products`                              | Get all products   |
| GET    | `/products?id=1`                         | Get product by ID  |
| POST   | `/products`                              | Create product     |
| PUT    | `/products`                              | Update product     |
| DELETE | `/products?id=1`                         | Delete product     |
| GET    | `/products/search?name=mouse`            | Search by name     |
| GET    | `/products/filter?minPrice=100&maxPrice=500` | Filter by price|
| GET    | `/products/low-stock`                    | Get low-stock items|

### Stock Transactions

| Method | URL             | Description              |
|--------|-----------------|--------------------------|
| GET    | `/stock`        | Get all transactions     |
| GET    | `/stock?id=1`   | Get transaction by ID    |
| POST   | `/stock/in`     | Record stock IN          |
| POST   | `/stock/out`    | Record stock OUT         |
| PUT    | `/stock/cancel` | Cancel a transaction     |

### Suppliers

| Method | URL                | Description        |
|--------|--------------------|--------------------|
| GET    | `/suppliers`       | Get all suppliers  |
| GET    | `/suppliers?id=1`  | Get by ID          |
| POST   | `/suppliers`       | Create supplier    |
| PUT    | `/suppliers`       | Update supplier    |
| DELETE | `/suppliers?id=1`  | Delete supplier    |

### Dashboard

| Method | URL              | Description       |
|--------|------------------|--------------------|
| GET    | `/api/dashboard` | Get aggregate stats|

---

## Sample JSON Payloads (for Postman)

### Create Product
```json
POST /products
Content-Type: application/json

{
    "productCode": "PRD-001",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with USB receiver",
    "price": 599.99,
    "lowStockThreshold": 15
}
```

### Update Product
```json
PUT /products
Content-Type: application/json

{
    "id": 1,
    "productCode": "PRD-001",
    "name": "Wireless Mouse Pro",
    "description": "Updated description",
    "price": 749.99,
    "lowStockThreshold": 20
}
```

### Stock IN
```json
POST /stock/in
Content-Type: application/json

{
    "productId": 1,
    "quantity": 100
}
```

### Stock OUT
```json
POST /stock/out
Content-Type: application/json

{
    "productId": 1,
    "quantity": 25
}
```

### Cancel Transaction
```json
PUT /stock/cancel
Content-Type: application/json

{
    "id": 1
}
```

### Create Supplier
```json
POST /suppliers
Content-Type: application/json

{
    "name": "TechParts India",
    "email": "contact@techparts.in",
    "phoneNumber": "+91 98765 43210"
}
```

---

## Business Rules

1. **Stock IN** → Increases `totalStock` and `availableStock`
2. **Stock OUT** → Decreases both; throws `InsufficientStockException` if not enough
3. **Cancel Transaction** → Reverses the stock change atomically
4. **Delete Product** → Blocked if `totalStock > 0` or `availableStock > 0`
5. **Low Stock Alert** → Triggered when `availableStock <= lowStockThreshold`
6. **Reference Code** → Auto-generated as `TXN-<timestamp>-<random6>`

---

## Response Format

### Success
```json
{
    "status": "success",
    "message": "Product created successfully",
    "data": { ... }
}
```

### Error
```json
{
    "status": "error",
    "message": "Stock not sufficient"
}
```

---

## Color Theme

| Color          | Hex        | Usage                   |
|----------------|------------|-------------------------|
| Midnight Blue  | `#191970`  | Primary / Sidebar       |
| Mist Gray      | `#C9CCD5`  | Borders / Subtle text   |
| Accent Blue    | `#4FC3F7`  | Buttons / Highlights    |
| Background     | `#0E0E30`  | Body background         |

---

## License

Academic project — for educational purposes only.
