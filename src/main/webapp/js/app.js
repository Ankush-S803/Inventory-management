/* ============================================================
   INVENTORY MANAGEMENT SYSTEM — Main JavaScript
   Handles all Fetch API calls, DOM manipulation, modals, toasts
   ============================================================ */

/* Auto-detect context path so the app works regardless of deployment name */
const CTX = (() => {
    const path = window.location.pathname;
    const idx  = path.indexOf('/', 1);          // find second '/'
    return idx > 0 ? path.substring(0, idx) : '';
})();

const API = {
    PRODUCTS:      CTX + '/products',
    SEARCH:        CTX + '/products/search',
    FILTER:        CTX + '/products/filter',
    LOW_STOCK:     CTX + '/products/low-stock',
    STOCK:         CTX + '/stock',
    STOCK_IN:      CTX + '/stock/in',
    STOCK_OUT:     CTX + '/stock/out',
    STOCK_CANCEL:  CTX + '/stock/cancel',
    SUPPLIERS:     CTX + '/suppliers',
    DASHBOARD:     CTX + '/api/dashboard'
};

/* ============================================================
   UTILITY FUNCTIONS
   ============================================================ */

async function apiCall(url, method = 'GET', body = null) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(url, opts);
    const data = await res.json();

    if (data.status === 'error') {
        throw new Error(data.message || 'Unknown error');
    }
    return data;
}

function formatCurrency(val) {
    return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2 });
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) +
        ' ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

/* ============================================================
   TOAST NOTIFICATIONS
   ============================================================ */

function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${icons[type] || 'ℹ'}</span>
        <span>${message}</span>
        <button class="toast-close" onclick="this.parentElement.remove()">×</button>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4500);
}

/* ============================================================
   MODAL HELPERS
   ============================================================ */

function openModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('show');
}

function closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('show');
}

function closeAllModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('show'));
}

// close modal on overlay click
document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('show');
    }
});

/* ============================================================
   SIDEBAR MOBILE TOGGLE
   ============================================================ */

function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) sidebar.classList.toggle('open');
}

/* ============================================================
   DASHBOARD PAGE
   ============================================================ */

async function loadDashboard() {
    try {
        const res = await apiCall(API.DASHBOARD);
        const d = res.data;

        setTextIfExists('stat-products',     d.totalProducts);
        setTextIfExists('stat-transactions', d.totalTransactions);
        setTextIfExists('stat-suppliers',    d.totalSuppliers);
        setTextIfExists('stat-lowstock',     d.lowStockCount);

        // Load recent transactions for dashboard
        const txnRes = await apiCall(API.STOCK);
        const txns = (txnRes.data || []).slice(0, 8);
        const tbody = document.getElementById('recent-txn-body');
        if (tbody) {
            tbody.innerHTML = txns.length ? txns.map(t => `
                <tr>
                    <td><code style="color: var(--accent);">${t.referenceCode}</code></td>
                    <td>${t.productName || '—'}</td>
                    <td><span class="status-badge ${t.transactionType.toLowerCase()}">${t.transactionType}</span></td>
                    <td>${t.quantity}</td>
                    <td><span class="status-badge ${t.status.toLowerCase()}">${t.status}</span></td>
                    <td>${formatDate(t.transactionDate)}</td>
                </tr>
            `).join('') : `<tr><td colspan="6" class="empty-state"><p>No transactions yet</p></td></tr>`;
        }

        // Load low stock alerts on dashboard
        const lowRes = await apiCall(API.LOW_STOCK);
        const lowItems = lowRes.data || [];
        const alertEl = document.getElementById('dashboard-alerts');
        if (alertEl) {
            if (lowItems.length === 0) {
                alertEl.innerHTML = '<p style="color:var(--text-muted); text-align:center; padding:20px;">All stock levels are healthy ✓</p>';
            } else {
                alertEl.innerHTML = lowItems.slice(0, 4).map(p => `
                    <div class="alert-card">
                        <h4>⚠ ${p.name}</h4>
                        <div class="alert-detail"><span>Available</span><span class="danger-value">${p.availableStock}</span></div>
                        <div class="alert-detail"><span>Threshold</span><span>${p.lowStockThreshold}</span></div>
                    </div>
                `).join('');
            }
        }
    } catch (err) {
        showToast('Failed to load dashboard: ' + err.message, 'error');
    }
}

function setTextIfExists(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

/* ============================================================
   PRODUCTS PAGE
   ============================================================ */

let allProducts = [];

async function loadProducts() {
    try {
        const res = await apiCall(API.PRODUCTS);
        allProducts = res.data || [];
        renderProductTable(allProducts);
    } catch (err) {
        showToast('Failed to load products: ' + err.message, 'error');
    }
}

function renderProductTable(products) {
    const tbody = document.getElementById('product-tbody');
    if (!tbody) return;

    if (products.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8"><div class="empty-state"><div class="icon">📦</div><h3>No products found</h3><p>Add your first product to get started</p></div></td></tr>`;
        return;
    }

    tbody.innerHTML = products.map(p => {
        const stockStatus = p.availableStock <= p.lowStockThreshold ? 'low' : 'ok';
        const stockLabel  = p.availableStock <= p.lowStockThreshold ? 'LOW' : 'OK';
        return `
        <tr>
            <td><code style="color: var(--accent);">${p.productCode}</code></td>
            <td><strong>${p.name}</strong></td>
            <td>${formatCurrency(p.price)}</td>
            <td>${p.totalStock}</td>
            <td>${p.availableStock}</td>
            <td>${p.lowStockThreshold}</td>
            <td><span class="status-badge ${stockStatus}">${stockLabel}</span></td>
            <td class="table-actions">
                <button class="btn btn-ghost btn-sm" onclick="editProduct(${p.id})" title="Edit">✏️</button>
                <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id})" title="Delete">🗑️</button>
            </td>
        </tr>`;
    }).join('');
}

function searchProducts() {
    const q = document.getElementById('product-search')?.value?.toLowerCase() || '';
    const filtered = allProducts.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.productCode.toLowerCase().includes(q)
    );
    renderProductTable(filtered);
}

function openAddProductModal() {
    document.getElementById('product-form')?.reset();
    document.getElementById('product-modal-title').textContent = 'Add New Product';
    document.getElementById('product-id').value = '';
    openModal('product-modal');
}

function editProduct(id) {
    const p = allProducts.find(x => x.id === id);
    if (!p) return;

    document.getElementById('product-modal-title').textContent = 'Edit Product';
    document.getElementById('product-id').value           = p.id;
    document.getElementById('product-code').value         = p.productCode;
    document.getElementById('product-name').value         = p.name;
    document.getElementById('product-description').value  = p.description || '';
    document.getElementById('product-price').value        = p.price;
    document.getElementById('product-threshold').value    = p.lowStockThreshold;
    openModal('product-modal');
}

async function saveProduct(e) {
    e.preventDefault();

    const id = document.getElementById('product-id').value;
    const body = {
        productCode:       document.getElementById('product-code').value,
        name:              document.getElementById('product-name').value,
        description:       document.getElementById('product-description').value,
        price:             parseFloat(document.getElementById('product-price').value),
        lowStockThreshold: parseInt(document.getElementById('product-threshold').value) || 10
    };

    try {
        if (id) {
            body.id = parseInt(id);
            await apiCall(API.PRODUCTS, 'PUT', body);
            showToast('Product updated successfully', 'success');
        } else {
            await apiCall(API.PRODUCTS, 'POST', body);
            showToast('Product created successfully', 'success');
        }
        closeModal('product-modal');
        loadProducts();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
        await apiCall(`${API.PRODUCTS}?id=${id}`, 'DELETE');
        showToast('Product deleted successfully', 'success');
        loadProducts();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/* ============================================================
   STOCK PAGE
   ============================================================ */

let allTransactions = [];

async function loadStock() {
    try {
        // Load products for select dropdown
        const prodRes = await apiCall(API.PRODUCTS);
        const products = prodRes.data || [];
        const sel = document.getElementById('stock-product');
        if (sel) {
            sel.innerHTML = '<option value="">Select Product</option>' +
                products.map(p => `<option value="${p.id}">${p.name} (${p.productCode})</option>`).join('');
        }

        // Load transactions
        const res = await apiCall(API.STOCK);
        allTransactions = res.data || [];
        renderStockTable(allTransactions);
    } catch (err) {
        showToast('Failed to load stock data: ' + err.message, 'error');
    }
}

function renderStockTable(txns) {
    const tbody = document.getElementById('stock-tbody');
    if (!tbody) return;

    if (txns.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state"><div class="icon">📊</div><h3>No transactions found</h3><p>Record a stock IN/OUT to get started</p></div></td></tr>`;
        return;
    }

    tbody.innerHTML = txns.map(t => `
        <tr>
            <td><code style="color: var(--accent);">${t.referenceCode}</code></td>
            <td>${t.productName || '—'}</td>
            <td><span class="status-badge ${t.transactionType.toLowerCase()}">${t.transactionType}</span></td>
            <td>${t.quantity}</td>
            <td><span class="status-badge ${t.status.toLowerCase()}">${t.status}</span></td>
            <td>${formatDate(t.transactionDate)}</td>
            <td class="table-actions">
                ${t.status === 'COMPLETED' ? `<button class="btn btn-warning btn-sm" onclick="cancelTxn(${t.id})" title="Cancel">Cancel</button>` : '—'}
            </td>
        </tr>
    `).join('');
}

function filterTransactions() {
    const typeFilter = document.getElementById('txn-type-filter')?.value || '';
    const filtered = allTransactions.filter(t =>
        !typeFilter || t.transactionType === typeFilter
    );
    renderStockTable(filtered);
}

async function stockIn(e) {
    e.preventDefault();
    const productId = parseInt(document.getElementById('stock-product').value);
    const quantity  = parseInt(document.getElementById('stock-quantity').value);

    if (!productId) { showToast('Please select a product', 'warning'); return; }
    if (!quantity || quantity <= 0) { showToast('Quantity must be positive', 'warning'); return; }

    try {
        await apiCall(API.STOCK_IN, 'POST', { productId, quantity });
        showToast('Stock IN recorded successfully', 'success');
        closeModal('stock-modal');
        loadStock();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function stockOut(e) {
    e.preventDefault();
    const productId = parseInt(document.getElementById('stock-product').value);
    const quantity  = parseInt(document.getElementById('stock-quantity').value);

    if (!productId) { showToast('Please select a product', 'warning'); return; }
    if (!quantity || quantity <= 0) { showToast('Quantity must be positive', 'warning'); return; }

    try {
        await apiCall(API.STOCK_OUT, 'POST', { productId, quantity });
        showToast('Stock OUT recorded successfully', 'success');
        closeModal('stock-modal');
        loadStock();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function cancelTxn(id) {
    if (!confirm('Cancel this transaction? This will reverse the stock change.')) return;
    try {
        await apiCall(API.STOCK_CANCEL, 'PUT', { id });
        showToast('Transaction cancelled successfully', 'success');
        loadStock();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function openStockModal() {
    document.getElementById('stock-form')?.reset();
    openModal('stock-modal');
}

/* ============================================================
   SUPPLIERS PAGE
   ============================================================ */

let allSuppliers = [];

async function loadSuppliers() {
    try {
        const res = await apiCall(API.SUPPLIERS);
        allSuppliers = res.data || [];
        renderSupplierTable(allSuppliers);
    } catch (err) {
        showToast('Failed to load suppliers: ' + err.message, 'error');
    }
}

function renderSupplierTable(suppliers) {
    const tbody = document.getElementById('supplier-tbody');
    if (!tbody) return;

    if (suppliers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="icon">🏢</div><h3>No suppliers found</h3><p>Add your first supplier</p></div></td></tr>`;
        return;
    }

    tbody.innerHTML = suppliers.map(s => `
        <tr>
            <td>${s.id}</td>
            <td><strong>${s.name}</strong></td>
            <td>${s.email}</td>
            <td>${s.phoneNumber}</td>
            <td class="table-actions">
                <button class="btn btn-ghost btn-sm" onclick="editSupplier(${s.id})" title="Edit">✏️</button>
                <button class="btn btn-danger btn-sm" onclick="deleteSupplier(${s.id})" title="Delete">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function openAddSupplierModal() {
    document.getElementById('supplier-form')?.reset();
    document.getElementById('supplier-modal-title').textContent = 'Add New Supplier';
    document.getElementById('supplier-id').value = '';
    openModal('supplier-modal');
}

function editSupplier(id) {
    const s = allSuppliers.find(x => x.id === id);
    if (!s) return;

    document.getElementById('supplier-modal-title').textContent = 'Edit Supplier';
    document.getElementById('supplier-id').value    = s.id;
    document.getElementById('supplier-name').value  = s.name;
    document.getElementById('supplier-email').value = s.email;
    document.getElementById('supplier-phone').value = s.phoneNumber;
    openModal('supplier-modal');
}

async function saveSupplier(e) {
    e.preventDefault();

    const id = document.getElementById('supplier-id').value;
    const body = {
        name:        document.getElementById('supplier-name').value,
        email:       document.getElementById('supplier-email').value,
        phoneNumber: document.getElementById('supplier-phone').value
    };

    try {
        if (id) {
            body.id = parseInt(id);
            await apiCall(API.SUPPLIERS, 'PUT', body);
            showToast('Supplier updated successfully', 'success');
        } else {
            await apiCall(API.SUPPLIERS, 'POST', body);
            showToast('Supplier created successfully', 'success');
        }
        closeModal('supplier-modal');
        loadSuppliers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteSupplier(id) {
    if (!confirm('Are you sure you want to delete this supplier?')) return;
    try {
        await apiCall(`${API.SUPPLIERS}?id=${id}`, 'DELETE');
        showToast('Supplier deleted successfully', 'success');
        loadSuppliers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/* ============================================================
   LOW STOCK PAGE
   ============================================================ */

async function loadLowStock() {
    try {
        const res = await apiCall(API.LOW_STOCK);
        const items = res.data || [];

        const container = document.getElementById('low-stock-grid');
        if (!container) return;

        if (items.length === 0) {
            container.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><div class="icon">✅</div><h3>All stock levels are healthy</h3><p>No products are below their low-stock threshold</p></div>`;
            return;
        }

        container.innerHTML = items.map(p => `
            <div class="alert-card">
                <h4>⚠ ${p.name}</h4>
                <div class="alert-detail"><span>Product Code</span><span>${p.productCode}</span></div>
                <div class="alert-detail"><span>Available Stock</span><span class="danger-value">${p.availableStock}</span></div>
                <div class="alert-detail"><span>Total Stock</span><span>${p.totalStock}</span></div>
                <div class="alert-detail"><span>Threshold</span><span>${p.lowStockThreshold}</span></div>
                <div class="alert-detail"><span>Price</span><span>${formatCurrency(p.price)}</span></div>
            </div>
        `).join('');

        const countEl = document.getElementById('low-stock-count');
        if (countEl) countEl.textContent = items.length;

    } catch (err) {
        showToast('Failed to load low stock data: ' + err.message, 'error');
    }
}
