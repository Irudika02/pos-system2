let state = {
  products: [],
  customers: [],
  orders: [],
  cart: [],
  stats: { customers: 0, products: 0, orders: 0, revenue: 0, profit: 0 },
  currentReceipt: null
};

// ============================================================
// TOAST NOTIFICATION SYSTEM (replaces alert())
// ============================================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const id = 'toast-' + Date.now();
  const colors = {
    success: { bg: 'rgba(16,185,129,0.95)', icon: '✅' },
    error:   { bg: 'rgba(239,68,68,0.95)',  icon: '❌' },
    info:    { bg: 'rgba(99,102,241,0.95)', icon: 'ℹ️' },
    warn:    { bg: 'rgba(245,158,11,0.95)', icon: '⚠️' }
  };
  const c = colors[type] || colors.info;

  const toast = document.createElement('div');
  toast.id = id;
  toast.style.cssText = `
    background: ${c.bg};
    color: #fff;
    padding: 12px 16px;
    border-radius: 12px;
    font-size: 14px;
    font-family: 'Outfit', sans-serif;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 10px;
    pointer-events: all;
    box-shadow: 0 4px 20px rgba(0,0,0,0.4);
    animation: slideInToast 0.3s ease;
    cursor: pointer;
    backdrop-filter: blur(10px);
  `;
  toast.innerHTML = `<span style="font-size:18px">${c.icon}</span><span style="flex:1">${message}</span>`;
  toast.onclick = () => removeToast(id);
  container.appendChild(toast);

  // Auto-remove after 4 seconds
  setTimeout(() => removeToast(id), 4000);
}

function removeToast(id) {
  const el = document.getElementById(id);
  if (el) {
    el.style.opacity = '0';
    el.style.transform = 'translateY(-10px)';
    el.style.transition = 'all 0.3s ease';
    setTimeout(() => el.remove(), 300);
  }
}

// ============================================================
// CUSTOM CONFIRM DIALOG (replaces confirm())
// ============================================================
let _confirmResolver = null;

function showConfirm(message, title = 'Are you sure?') {
  document.getElementById('confirmTitle').innerText = title;
  document.getElementById('confirmMessage').innerText = message;
  document.getElementById('confirmModal').classList.add('active');
  return new Promise(resolve => { _confirmResolver = resolve; });
}

function resolveConfirm(result) {
  document.getElementById('confirmModal').classList.remove('active');
  if (_confirmResolver) { _confirmResolver(result); _confirmResolver = null; }
}

// ============================================================
// SAFE NUMBER PARSER (handles Sinhala input / comma / dot)
// ============================================================
function parseNum(val) {
  if (val === null || val === undefined) return 0;
  const clean = String(val).replace(/,/g, '.').replace(/[^0-9.]/g, '');
  const num = parseFloat(clean);
  return isNaN(num) ? 0 : num;
}

// Add toast slide-in animation to page
const style = document.createElement('style');
style.textContent = `
  @keyframes slideInToast {
    from { opacity: 0; transform: translateY(-20px); }
    to   { opacity: 1; transform: translateY(0); }
  }
`;
document.head.appendChild(style);

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  fetchStats();
  fetchProducts();
  fetchCustomers();
  fetchOrders();
});

// ============================================================
// TAB SWITCHING
// ============================================================
function switchTab(tabId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const screen = document.getElementById(`screen-${tabId}`);
  const nav    = document.getElementById(`nav-${tabId}`);
  if (screen) screen.classList.add('active');
  if (nav)    nav.classList.add('active');
  if (tabId === 'pos')       renderPosProducts();
  if (tabId === 'customers') renderCustomers();
  if (tabId === 'products')  renderProducts();
  if (tabId === 'history')   renderHistory();
  if (tabId === 'dashboard') fetchStats();
}

// ============================================================
// API FETCH HELPERS
// ============================================================
async function safeJson(res) {
  const text = await res.text();
  try { return JSON.parse(text); }
  catch { return { success: false, msg: 'Server returned invalid response. Please check connection.' }; }
}

async function fetchStats() {
  try {
    const res  = await fetch('/api/stats');
    const data = await safeJson(res);
    if (!data || data.error) return;
    state.stats = data;
    setText('statRevenue',   `LKR ${(data.revenue || 0).toFixed(2)}`);
    setText('statProfit',    `LKR ${(data.profit  || 0).toFixed(2)}`);
    setText('statOrders',    data.orders    || 0);
    setText('statProducts',  data.products  || 0);
    setText('statCustomers', data.customers || 0);
  } catch (e) { console.error('Stats error:', e.message); }
}

async function fetchProducts() {
  try {
    const res = await fetch('/api/products');
    const data = await safeJson(res);
    if (Array.isArray(data)) { state.products = data; renderPosProducts(); renderProducts(); }
  } catch (e) { console.error('Products error:', e.message); }
}

async function fetchCustomers() {
  try {
    const res = await fetch('/api/customers');
    const data = await safeJson(res);
    if (Array.isArray(data)) { state.customers = data; renderCustomerDropdown(); renderCustomers(); }
  } catch (e) { console.error('Customers error:', e.message); }
}

async function fetchOrders() {
  try {
    const res = await fetch('/api/orders');
    const data = await safeJson(res);
    if (Array.isArray(data)) { state.orders = data; renderHistory(); }
  } catch (e) { console.error('Orders error:', e.message); }
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.innerText = val;
}

// ============================================================
// RENDER FUNCTIONS
// ============================================================
function renderCustomerDropdown() {
  const select = document.getElementById('posCustomerSelect');
  select.innerHTML = `<option value="C-WALKIN">Walk-in Customer</option>`;
  state.customers.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id;
    opt.textContent = `${c.name} (${c.id})`;
    select.appendChild(opt);
  });
}

function renderPosProducts() {
  const grid   = document.getElementById('posProductGrid');
  const search = (document.getElementById('posSearch').value || '').toLowerCase();
  const filtered = state.products.filter(p =>
    p.description.toLowerCase().includes(search) || p.code.toLowerCase().includes(search)
  );
  grid.innerHTML = filtered.length === 0
    ? `<div style="grid-column:1/-1;text-align:center;color:#9ca3af;padding:20px;">No products found</div>`
    : '';
  filtered.forEach(p => {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.innerHTML = `
      <div>
        <span class="product-code">${p.code}</span>
        <div class="product-desc">${p.description}</div>
        <div class="product-price">LKR ${p.unitPrice.toFixed(2)}</div>
        <div class="product-stock">Stock: ${p.qtyOnHand} pcs</div>
      </div>
      <button class="btn-add-cart">+ Add to Cart</button>
    `;
    card.querySelector('.btn-add-cart').onclick = () => addToCart(p.code);
    grid.appendChild(card);
  });
}

function filterPosProducts() { renderPosProducts(); }

function renderProducts() {
  const container = document.getElementById('productList');
  container.innerHTML = state.products.length === 0
    ? `<div style="text-align:center;color:#9ca3af;padding:20px;">No products added yet</div>`
    : '';
  state.products.forEach(p => {
    const margin = p.unitPrice - (p.buyingPrice || 0);
    const card = document.createElement('div');
    card.className = 'list-card';
    card.innerHTML = `
      <div class="list-card-info" style="flex:1">
        <h4>${p.description}</h4>
        <p>Code: <strong>${p.code}</strong> | Stock: <strong>${p.qtyOnHand} pcs</strong></p>
        <p style="color:#10b981;font-size:11px;margin-top:2px">
          Buy: LKR ${(p.buyingPrice||0).toFixed(2)} | Profit/item: LKR ${margin.toFixed(2)}
        </p>
      </div>
      <div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px">
        <div class="list-card-value">LKR ${p.unitPrice.toFixed(2)}</div>
        <div style="display:flex;gap:6px">
          <button class="btn-edit-prod" style="background:rgba(99,102,241,0.2);border:1px solid #6366f1;color:#fff;padding:4px 8px;border-radius:6px;font-size:11px;cursor:pointer">✏️ Edit</button>
          <button class="btn-del-prod" style="background:rgba(239,68,68,0.2);border:1px solid #ef4444;color:#ef4444;padding:4px 8px;border-radius:6px;font-size:11px;cursor:pointer">🗑️ Delete</button>
        </div>
      </div>
    `;
    card.querySelector('.btn-edit-prod').onclick = () => editProduct(p.code);
    card.querySelector('.btn-del-prod').onclick  = () => deleteProduct(p.code);
    container.appendChild(card);
  });
}

function renderCustomers() {
  const container = document.getElementById('customerList');
  container.innerHTML = state.customers.length === 0
    ? `<div style="text-align:center;color:#9ca3af;padding:20px;">No customers added yet</div>`
    : '';
  state.customers.forEach(c => {
    const card = document.createElement('div');
    card.className = 'list-card';
    card.innerHTML = `
      <div class="list-card-info">
        <h4>${c.name}</h4>
        <p>ID: ${c.id} | Address: ${c.address}</p>
      </div>
      <div class="list-card-value">LKR ${c.salary.toFixed(2)}</div>
    `;
    container.appendChild(card);
  });
}

function renderHistory() {
  const container = document.getElementById('historyList');
  container.innerHTML = state.orders.length === 0
    ? `<div style="text-align:center;color:#9ca3af;padding:20px;">No order history found</div>`
    : '';
  state.orders.forEach(o => {
    const card = document.createElement('div');
    card.className = 'list-card';
    card.style.cursor = 'pointer';
    card.innerHTML = `
      <div class="list-card-info">
        <h4>Order #${o.orderId}</h4>
        <p>Customer: ${o.customerId} | Date: ${o.date}</p>
        <p style="color:#10b981;font-size:11px;margin-top:2px">Profit: LKR ${(o.totalProfit||0).toFixed(2)}</p>
      </div>
      <div class="list-card-value">
        LKR ${o.totalCost.toFixed(2)}
        <div style="font-size:11px;color:#6366f1;text-align:right;margin-top:2px">📄 View Bill</div>
      </div>
    `;
    card.onclick = () => viewPastOrderReceipt(o.orderId);
    container.appendChild(card);
  });
}

// ============================================================
// CART MANAGEMENT
// ============================================================
function addToCart(code) {
  const product = state.products.find(p => p.code === code);
  if (!product) return;
  const existing = state.cart.find(i => i.code === code);
  if (existing) {
    if (existing.qty + 1 > product.qtyOnHand) {
      showToast(`Only ${product.qtyOnHand} units in stock!`, 'warn'); return;
    }
    existing.qty += 1;
  } else {
    state.cart.push({ code: product.code, description: product.description, unitPrice: product.unitPrice, qty: 1 });
  }
  updateCartUI();
  toggleCartDrawer(true);
}

function updateCartUI() {
  const badge    = document.getElementById('cartBadge');
  const container= document.getElementById('cartItemsList');
  let totalQty = 0, grandTotal = 0;
  container.innerHTML = '';
  state.cart.forEach((item, index) => {
    totalQty   += item.qty;
    const itemTotal = item.unitPrice * item.qty;
    grandTotal += itemTotal;
    const row = document.createElement('div');
    row.className = 'cart-item';
    row.innerHTML = `
      <div>
        <div class="cart-item-title">${item.description}</div>
        <div class="cart-item-sub">LKR ${item.unitPrice.toFixed(2)} × ${item.qty}</div>
      </div>
      <div style="display:flex;align-items:center;gap:8px">
        <strong style="color:#10b981">LKR ${itemTotal.toFixed(2)}</strong>
        <button style="background:rgba(239,68,68,0.2);border:none;color:#ef4444;width:26px;height:26px;border-radius:6px;cursor:pointer">✕</button>
      </div>
    `;
    row.querySelector('button').onclick = () => { removeFromCart(index); };
    container.appendChild(row);
  });
  badge.innerText = totalQty;
  setText('cartSubtotal',  `LKR ${grandTotal.toFixed(2)}`);
  setText('cartGrandTotal',`LKR ${grandTotal.toFixed(2)}`);
}

function removeFromCart(index) { state.cart.splice(index, 1); updateCartUI(); }

function toggleCartDrawer(forceOpen) {
  const drawer  = document.getElementById('cartDrawer');
  const overlay = document.getElementById('cartOverlay');
  if (forceOpen === true) { drawer.classList.add('active'); overlay.classList.add('active'); }
  else { drawer.classList.toggle('active'); overlay.classList.toggle('active'); }
}

async function checkoutOrder() {
  if (state.cart.length === 0) { showToast('Cart is empty! Add items first.', 'warn'); return; }
  const customerSelect = document.getElementById('posCustomerSelect');
  const customerId   = customerSelect.value;
  const customerName = customerSelect.options[customerSelect.selectedIndex].text;
  const itemsString  = state.cart.map(i => `${i.code}|${i.description}|${i.unitPrice}|${i.qty}`).join(';');
  const checkoutItems = [...state.cart];
  try {
    const res    = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId, userEmail: 'irudika@wijenayake-stores.com', items: itemsString })
    });
    const result = await safeJson(res);
    if (result.success) {
      showReceiptModal({ orderId: result.orderId, date: new Date().toLocaleString(), customerName, items: checkoutItems, totalCost: result.total });
      state.cart = []; updateCartUI(); toggleCartDrawer(false);
      fetchProducts(); fetchOrders(); fetchStats();
    } else { showToast('Error: ' + result.msg, 'error'); }
  } catch (e) { showToast('Checkout failed: ' + e.message, 'error'); }
}

// ============================================================
// RECEIPT MODAL
// ============================================================
function showReceiptModal(receiptData) {
  state.currentReceipt = receiptData;
  setText('recOrderId',  '#' + receiptData.orderId);
  setText('recDate',     receiptData.date);
  setText('recCustomer', receiptData.customerName);
  const body = document.getElementById('recItemsBody');
  body.innerHTML = '';
  receiptData.items.forEach(item => {
    const itemTotal = item.unitPrice * item.qty;
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.description}</td>
      <td>${item.qty}</td>
      <td>${item.unitPrice.toFixed(2)}</td>
      <td style="text-align:right">${itemTotal.toFixed(2)}</td>
    `;
    body.appendChild(tr);
  });
  setText('recGrandTotal', `LKR ${receiptData.totalCost.toFixed(2)}`);
  document.getElementById('receiptModal').classList.add('active');
}
function closeReceiptModal() { document.getElementById('receiptModal').classList.remove('active'); }
function printReceipt() { window.print(); }

function viewPastOrderReceipt(orderId) {
  const order = state.orders.find(o => o.orderId === orderId);
  if (!order) return;
  const cust = state.customers.find(c => c.id === order.customerId);
  showReceiptModal({
    orderId: order.orderId, date: order.date,
    customerName: cust ? cust.name : (order.customerId || 'Walk-in Customer'),
    items: [{ description: 'Order Total', qty: 1, unitPrice: order.totalCost }],
    totalCost: order.totalCost
  });
}

// ============================================================
// PRODUCT MANAGEMENT (ADD / EDIT / DELETE) - NO alert() used
// ============================================================
function showProductModal() {
  setText('prodModalTitle', 'Add New Product');
  document.getElementById('prodCodeEdit').value = '';
  document.getElementById('prodDesc').value = '';
  document.getElementById('prodBuyingPrice').value = '';
  document.getElementById('prodPrice').value = '';
  document.getElementById('prodQty').value = '';
  setText('btnSaveProd', 'Save Product');
  document.getElementById('productModal').classList.add('active');
}

function editProduct(code) {
  const prod = state.products.find(p => p.code === code);
  if (!prod) return;
  setText('prodModalTitle', `Edit Product (${code})`);
  document.getElementById('prodCodeEdit').value    = code;
  document.getElementById('prodDesc').value        = prod.description;
  document.getElementById('prodBuyingPrice').value = prod.buyingPrice || '';
  document.getElementById('prodPrice').value       = prod.unitPrice;
  document.getElementById('prodQty').value         = prod.qtyOnHand;
  setText('btnSaveProd', 'Update Product');
  document.getElementById('productModal').classList.add('active');
}
function closeProductModal() { document.getElementById('productModal').classList.remove('active'); }

async function saveProduct() {
  const code          = document.getElementById('prodCodeEdit').value.trim();
  const description   = document.getElementById('prodDesc').value.trim();
  const buyingPrice   = parseNum(document.getElementById('prodBuyingPrice').value);
  const unitPrice     = parseNum(document.getElementById('prodPrice').value);
  const qtyOnHand     = parseNum(document.getElementById('prodQty').value);

  if (!description) { showToast('Item name / description is required!', 'warn'); return; }
  if (unitPrice <= 0) { showToast('Please enter a valid Selling Price!', 'warn'); return; }

  const payload = { description, buyingPrice, unitPrice, qtyOnHand };

  try {
    const url    = code ? `/api/products/${encodeURIComponent(code)}` : '/api/products';
    const method = code ? 'PUT' : 'POST';
    const res    = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await safeJson(res);
    if (data.success) {
      showToast(code ? '✅ Product updated successfully!' : '✅ Product saved successfully!', 'success');
      closeProductModal();
      await fetchProducts();
      await fetchStats();
    } else {
      showToast('Save failed: ' + (data.msg || 'Unknown error'), 'error');
    }
  } catch (e) {
    showToast('Network error: ' + e.message, 'error');
  }
}

async function deleteProduct(code) {
  const confirmed = await showConfirm(`Delete product "${code}"? This cannot be undone.`, 'Delete Product?');
  if (!confirmed) return;
  try {
    const res  = await fetch(`/api/products/${encodeURIComponent(code)}`, { method: 'DELETE' });
    const data = await safeJson(res);
    if (data.success) { showToast('Product deleted.', 'success'); fetchProducts(); fetchStats(); }
    else { showToast('Delete failed: ' + data.msg, 'error'); }
  } catch (e) { showToast('Delete error: ' + e.message, 'error'); }
}

// ============================================================
// CUSTOMER MANAGEMENT
// ============================================================
function showCustomerModal() { document.getElementById('customerModal').classList.add('active'); }
function closeCustomerModal() { document.getElementById('customerModal').classList.remove('active'); }

async function saveCustomer() {
  const name    = document.getElementById('custName').value.trim();
  const address = document.getElementById('custAddress').value.trim();
  const salary  = parseNum(document.getElementById('custSalary').value);
  if (!name || !address) { showToast('Name and address are required!', 'warn'); return; }
  try {
    const res  = await fetch('/api/customers', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, address, salary })
    });
    const data = await safeJson(res);
    if (data.success) {
      showToast('Customer saved!', 'success');
      closeCustomerModal();
      document.getElementById('custName').value = '';
      document.getElementById('custAddress').value = '';
      document.getElementById('custSalary').value = '';
      fetchCustomers();
    } else { showToast('Error: ' + data.msg, 'error'); }
  } catch (e) { showToast('Error saving customer', 'error'); }
}

// ============================================================
// QR SCANNER
// ============================================================
let html5QrcodeScanner = null;
function openScanner() {
  document.getElementById('qrModal').classList.add('active');
  if (!html5QrcodeScanner) html5QrcodeScanner = new Html5Qrcode('reader');
  html5QrcodeScanner.start(
    { facingMode: 'environment' },
    { fps: 10, qrbox: { width: 220, height: 220 } },
    (decoded) => { closeScanner(); addToCart(decoded.replace('QR-', '')); },
    () => {}
  ).catch(err => console.log('Camera:', err));
}
function closeScanner() {
  document.getElementById('qrModal').classList.remove('active');
  if (html5QrcodeScanner) html5QrcodeScanner.stop().catch(() => {});
}
