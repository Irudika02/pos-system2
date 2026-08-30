let state = {
  products: [],
  customers: [],
  orders: [],
  cart: [],
  stats: null,
  currentReceipt: null
};

// Helper: Safe number parsing for mobile & decimal keyboards
function parseNum(val) {
  if (val === null || val === undefined) return 0;
  const clean = String(val).replace(/,/g, '.').replace(/[^0-9.]/g, '');
  const num = parseFloat(clean);
  return isNaN(num) ? 0 : num;
}

document.addEventListener('DOMContentLoaded', () => {
  fetchStats();
  fetchProducts();
  fetchCustomers();
  fetchOrders();
});

// Tab Switching
function switchTab(tabId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  const screen = document.getElementById(`screen-${tabId}`);
  const nav = document.getElementById(`nav-${tabId}`);

  if (screen) screen.classList.add('active');
  if (nav) nav.classList.add('active');

  if (tabId === 'pos') renderPosProducts();
  if (tabId === 'customers') renderCustomers();
  if (tabId === 'products') renderProducts();
  if (tabId === 'history') renderHistory();
  if (tabId === 'dashboard') fetchStats();
}

// API Calls
async function fetchStats() {
  try {
    const res = await fetch('/api/stats');
    const data = await res.json();
    state.stats = data;
    updateReportStats();
  } catch (e) {
    console.error('Stats error:', e);
  }
}

function updateReportStats() {
  if (!state.stats) return;

  const select = document.getElementById('reportPeriodSelect');
  const period = select ? select.value : 'today';
  let data = state.stats.today;

  if (period === 'month') {
    data = state.stats.month;
    document.getElementById('labelRevenue').innerText = 'This Month Sales';
    document.getElementById('labelProfit').innerText = 'Monthly Net Profit';
  } else if (period === 'allTime') {
    data = state.stats.allTime;
    document.getElementById('labelRevenue').innerText = 'All Time Sales';
    document.getElementById('labelProfit').innerText = 'All Time Net Profit';
  } else {
    document.getElementById('labelRevenue').innerText = 'Today Sales';
    document.getElementById('labelProfit').innerText = 'Today Net Profit';
  }

  document.getElementById('statRevenue').innerText = `LKR ${data.revenue ? data.revenue.toFixed(2) : '0.00'}`;
  document.getElementById('statProfit').innerText = `LKR ${data.profit ? data.profit.toFixed(2) : '0.00'}`;
  document.getElementById('statOrders').innerText = data.bills || 0;
  document.getElementById('statStockVal').innerText = `LKR ${state.stats.totalStockValue ? state.stats.totalStockValue.toFixed(2) : '0.00'}`;
}

async function fetchProducts() {
  try {
    const res = await fetch('/api/products');
    state.products = await res.json();
    renderPosProducts();
    renderProducts();
  } catch (e) {
    console.error('Products error:', e);
  }
}

async function fetchCustomers() {
  try {
    const res = await fetch('/api/customers');
    state.customers = await res.json();
    renderCustomerDropdown();
    renderCustomers();
  } catch (e) {
    console.error('Customers error:', e);
  }
}

async function fetchOrders() {
  try {
    const res = await fetch('/api/orders');
    state.orders = await res.json();
    renderHistory();
  } catch (e) {
    console.error('Orders error:', e);
  }
}

// Render Functions
function renderCustomerDropdown() {
  const select = document.getElementById('posCustomerSelect');
  select.innerHTML = `<option value="C-WALKIN">Walk-in Customer</option>`;
  state.customers.forEach(c => {
    select.innerHTML += `<option value="${c.id}">${c.name} (${c.id})</option>`;
  });
}

function renderPosProducts() {
  const grid = document.getElementById('posProductGrid');
  grid.innerHTML = '';

  const search = (document.getElementById('posSearch').value || '').toLowerCase();
  const filtered = state.products.filter(p => 
    p.description.toLowerCase().includes(search) || p.code.toLowerCase().includes(search)
  );

  if (filtered.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #9ca3af; padding: 20px;">No products found</div>`;
    return;
  }

  filtered.forEach(p => {
    grid.innerHTML += `
      <div class="product-card">
        <div>
          <span class="product-code">${p.code}</span>
          <div class="product-desc">${p.description}</div>
          <div class="product-price">LKR ${p.unitPrice.toFixed(2)}</div>
          <div class="product-stock">Stock: ${p.qtyOnHand} pcs</div>
        </div>
        <button class="btn-add-cart" onclick="addToCart('${p.code}')">
          + Add to Cart
        </button>
      </div>
    `;
  });
}

function filterPosProducts() {
  renderPosProducts();
}

function renderProducts() {
  const container = document.getElementById('productList');
  container.innerHTML = '';
  if (state.products.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: #9ca3af; padding: 20px;">No products added yet</div>`;
    return;
  }
  state.products.forEach(p => {
    const margin = (p.unitPrice - (p.buyingPrice || 0));
    const stockBadge = p.qtyOnHand <= 5 ? `<span style="background: rgba(239,68,68,0.2); color: #ef4444; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 700;">LOW STOCK (${p.qtyOnHand})</span>` : `<span style="color: #9ca3af;">Stock: <strong>${p.qtyOnHand} pcs</strong></span>`;

    container.innerHTML += `
      <div class="list-card">
        <div class="list-card-info" style="flex: 1;">
          <h4>${p.description}</h4>
          <p>Code: <strong>${p.code}</strong> | ${stockBadge}</p>
          <p style="color: #10b981; font-size: 11px; margin-top: 2px;">
            Buy: LKR ${(p.buyingPrice || 0).toFixed(2)} | Profit/item: LKR ${margin.toFixed(2)}
          </p>
        </div>
        <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 6px;">
          <div class="list-card-value">LKR ${p.unitPrice.toFixed(2)}</div>
          <div style="display: flex; gap: 6px;">
            <button style="background: rgba(99,102,241,0.2); border: 1px solid #6366f1; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 11px; cursor: pointer;" onclick="editProduct('${p.code}')">✏️ Edit</button>
            <button style="background: rgba(239,68,68,0.2); border: 1px solid #ef4444; color: #ef4444; padding: 4px 8px; border-radius: 6px; font-size: 11px; cursor: pointer;" onclick="deleteProduct('${p.code}')">🗑️ Delete</button>
          </div>
        </div>
      </div>
    `;
  });
}

function renderCustomers() {
  const container = document.getElementById('customerList');
  container.innerHTML = '';
  if (state.customers.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: #9ca3af; padding: 20px;">No customers added yet</div>`;
    return;
  }
  state.customers.forEach(c => {
    container.innerHTML += `
      <div class="list-card">
        <div class="list-card-info">
          <h4>${c.name}</h4>
          <p>ID: ${c.id} | Address: ${c.address}</p>
        </div>
        <div class="list-card-value">LKR ${c.salary.toFixed(2)}</div>
      </div>
    `;
  });
}

function renderHistory() {
  const container = document.getElementById('historyList');
  container.innerHTML = '';
  if (state.orders.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: #9ca3af; padding: 20px;">No order history found</div>`;
    return;
  }
  state.orders.forEach(o => {
    container.innerHTML += `
      <div class="list-card" onclick="viewPastOrderReceipt('${o.orderId}')" style="cursor: pointer;">
        <div class="list-card-info">
          <h4>Order #${o.orderId}</h4>
          <p>Customer: ${o.customerId} | Date: ${o.date}</p>
          <p style="color: #10b981; font-size: 11px; margin-top: 2px;">Profit: LKR ${(o.totalProfit || 0).toFixed(2)}</p>
        </div>
        <div class="list-card-value">
          LKR ${o.totalCost.toFixed(2)}
          <div style="font-size: 11px; color: #6366f1; text-align: right; margin-top: 2px;">📄 View Bill</div>
        </div>
      </div>
    `;
  });
}

// Cart Management
function addToCart(code) {
  const product = state.products.find(p => p.code === code);
  if (!product) return;

  const existing = state.cart.find(item => item.code === code);
  if (existing) {
    if (existing.qty + 1 > product.qtyOnHand) {
      alert(`Only ${product.qtyOnHand} units available in stock!`);
      return;
    }
    existing.qty += 1;
  } else {
    state.cart.push({
      code: product.code,
      description: product.description,
      unitPrice: product.unitPrice,
      qty: 1
    });
  }

  updateCartUI();
  toggleCartDrawer(true);
}

function updateCartUI() {
  const badge = document.getElementById('cartBadge');
  const itemsContainer = document.getElementById('cartItemsList');
  const subtotalEl = document.getElementById('cartSubtotal');
  const totalEl = document.getElementById('cartGrandTotal');

  let totalQty = 0;
  let grandTotal = 0;
  itemsContainer.innerHTML = '';

  state.cart.forEach((item, index) => {
    totalQty += item.qty;
    const itemTotal = item.unitPrice * item.qty;
    grandTotal += itemTotal;

    itemsContainer.innerHTML += `
      <div class="cart-item">
        <div>
          <div class="cart-item-title">${item.description}</div>
          <div class="cart-item-sub">LKR ${item.unitPrice.toFixed(2)} x ${item.qty}</div>
        </div>
        <div style="display: flex; align-items: center; gap: 8px;">
          <strong style="color: #10b981;">LKR ${itemTotal.toFixed(2)}</strong>
          <button style="background: rgba(239,68,68,0.2); border: none; color: #ef4444; width: 26px; height: 26px; border-radius: 6px; cursor: pointer;" onclick="removeFromCart(${index})">✕</button>
        </div>
      </div>
    `;
  });

  badge.innerText = totalQty;
  subtotalEl.innerText = `LKR ${grandTotal.toFixed(2)}`;
  totalEl.innerText = `LKR ${grandTotal.toFixed(2)}`;
}

function removeFromCart(index) {
  state.cart.splice(index, 1);
  updateCartUI();
}

function toggleCartDrawer(forceOpen) {
  const drawer = document.getElementById('cartDrawer');
  const overlay = document.getElementById('cartOverlay');

  if (forceOpen === true) {
    drawer.classList.add('active');
    overlay.classList.add('active');
  } else {
    drawer.classList.toggle('active');
    overlay.classList.toggle('active');
  }
}

async function checkoutOrder() {
  if (state.cart.length === 0) {
    alert('Cart is empty!');
    return;
  }

  const customerSelect = document.getElementById('posCustomerSelect');
  const customerId = customerSelect.value;
  const customerName = customerSelect.options[customerSelect.selectedIndex].text;
  const itemsString = state.cart.map(i => `${i.code}|${i.description}|${i.unitPrice}|${i.qty}`).join(';');

  const checkoutItems = [...state.cart];

  try {
    const res = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerId: customerId,
        userEmail: 'irudika@wijenayake-stores.com',
        items: itemsString
      })
    });

    const result = await res.json();
    if (result.success) {
      const nowStr = new Date().toLocaleString();
      showReceiptModal({
        orderId: result.orderId,
        date: nowStr,
        customerName: customerName,
        items: checkoutItems,
        totalCost: result.total
      });

      state.cart = [];
      updateCartUI();
      toggleCartDrawer(false);
      fetchProducts();
      fetchOrders();
      fetchStats();
    } else {
      alert('Error: ' + result.msg);
    }
  } catch (e) {
    alert('Checkout failed: ' + e.message);
  }
}

// Receipt Modal Handlers
function showReceiptModal(receiptData) {
  state.currentReceipt = receiptData;

  document.getElementById('recOrderId').innerText = '#' + receiptData.orderId;
  document.getElementById('recDate').innerText = receiptData.date;
  document.getElementById('recCustomer').innerText = receiptData.customerName;

  const body = document.getElementById('recItemsBody');
  body.innerHTML = '';

  receiptData.items.forEach(item => {
    const itemTotal = item.unitPrice * item.qty;
    body.innerHTML += `
      <tr>
        <td>${item.description}</td>
        <td>${item.qty}</td>
        <td>${item.unitPrice.toFixed(2)}</td>
        <td style="text-align: right;">${itemTotal.toFixed(2)}</td>
      </tr>
    `;
  });

  document.getElementById('recGrandTotal').innerText = `LKR ${receiptData.totalCost.toFixed(2)}`;
  document.getElementById('receiptModal').classList.add('active');
}

function closeReceiptModal() {
  document.getElementById('receiptModal').classList.remove('active');
}

function printReceipt() {
  window.print();
}

function viewPastOrderReceipt(orderId) {
  const order = state.orders.find(o => o.orderId === orderId);
  if (!order) return;

  const cust = state.customers.find(c => c.id === order.customerId);
  const custName = cust ? cust.name : (order.customerId || 'Walk-in Customer');

  showReceiptModal({
    orderId: order.orderId,
    date: order.date,
    customerName: custName,
    items: [
      { description: 'Order Total Bill Summary', qty: 1, unitPrice: order.totalCost }
    ],
    totalCost: order.totalCost
  });
}

// Product Management (ADD, EDIT, DELETE)
function showProductModal() {
  document.getElementById('prodModalTitle').innerText = 'Add New Product';
  document.getElementById('prodCodeEdit').value = '';
  document.getElementById('prodDesc').value = '';
  document.getElementById('prodBuyingPrice').value = '';
  document.getElementById('prodPrice').value = '';
  document.getElementById('prodQty').value = '';
  document.getElementById('btnSaveProd').innerText = 'Save Product';
  document.getElementById('productModal').classList.add('active');
}

function editProduct(code) {
  const prod = state.products.find(p => p.code === code);
  if (!prod) return;

  document.getElementById('prodModalTitle').innerText = 'Edit Product (' + code + ')';
  document.getElementById('prodCodeEdit').value = code;
  document.getElementById('prodDesc').value = prod.description;
  document.getElementById('prodBuyingPrice').value = prod.buyingPrice || '';
  document.getElementById('prodPrice').value = prod.unitPrice;
  document.getElementById('prodQty').value = prod.qtyOnHand;
  document.getElementById('btnSaveProd').innerText = 'Update Product';

  document.getElementById('productModal').classList.add('active');
}

function closeProductModal() {
  document.getElementById('productModal').classList.remove('active');
}

async function saveProduct() {
  const code = document.getElementById('prodCodeEdit').value;
  const description = document.getElementById('prodDesc').value.trim();
  const buyingPriceRaw = document.getElementById('prodBuyingPrice').value;
  const unitPriceRaw = document.getElementById('prodPrice').value;
  const qtyRaw = document.getElementById('prodQty').value;

  if (!description) {
    alert('Please enter Item Description / Name (සිංහල / English)');
    return;
  }

  const buyingPrice = parseNum(buyingPriceRaw);
  const unitPrice = parseNum(unitPriceRaw);
  const qtyOnHand = parseNum(qtyRaw);

  if (unitPrice <= 0) {
    alert('Please enter a valid Selling Unit Price!');
    return;
  }

  try {
    let res;
    if (code) {
      // EDIT existing product
      res = await fetch(`/api/products/${code}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description, buyingPrice, unitPrice, qtyOnHand })
      });
    } else {
      // ADD new product
      res = await fetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description, buyingPrice, unitPrice, qtyOnHand })
      });
    }

    const data = await res.json();
    if (data.success) {
      alert(code ? 'Product updated successfully!' : 'Product saved successfully!');
      closeProductModal();
      fetchProducts();
      fetchStats();
    } else {
      alert('Error: ' + data.msg);
    }
  } catch (e) {
    alert('Error saving product: ' + e.message);
  }
}

async function deleteProduct(code) {
  if (!confirm(`Are you sure you want to delete product ${code}?`)) return;

  try {
    const res = await fetch(`/api/products/${code}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      alert('Product deleted successfully!');
      fetchProducts();
      fetchStats();
    }
  } catch (e) {
    alert('Error deleting product');
  }
}

// Customer Management
function showCustomerModal() { document.getElementById('customerModal').classList.add('active'); }
function closeCustomerModal() { document.getElementById('customerModal').classList.remove('active'); }

async function saveCustomer() {
  const name = document.getElementById('custName').value.trim();
  const address = document.getElementById('custAddress').value.trim();
  const salaryRaw = document.getElementById('custSalary').value;
  const salary = parseNum(salaryRaw);

  if (!name || !address) {
    alert('Please fill customer name and address');
    return;
  }

  try {
    const res = await fetch('/api/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, address, salary })
    });
    const data = await res.json();
    if (data.success) {
      alert('Customer saved successfully!');
      closeCustomerModal();
      document.getElementById('custName').value = '';
      document.getElementById('custAddress').value = '';
      document.getElementById('custSalary').value = '';
      fetchCustomers();
    }
  } catch (e) {
    alert('Error saving customer');
  }
}

// QR Code Scanner
let html5QrcodeScanner = null;
function openScanner() {
  document.getElementById('qrModal').classList.add('active');
  if (!html5QrcodeScanner) {
    html5QrcodeScanner = new Html5Qrcode("reader");
  }
  html5QrcodeScanner.start(
    { facingMode: "environment" },
    { fps: 10, qrbox: { width: 220, height: 220 } },
    (decodedText) => {
      closeScanner();
      addToCart(decodedText.replace("QR-", ""));
    },
    (errorMessage) => {}
  ).catch(err => console.log('Camera error:', err));
}

function closeScanner() {
  document.getElementById('qrModal').classList.remove('active');
  if (html5QrcodeScanner) {
    html5QrcodeScanner.stop().catch(err => {});
  }
}
