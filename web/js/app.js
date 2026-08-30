let state = {
  products: [],
  customers: [],
  orders: [],
  cart: [],
  stats: { customers: 0, products: 0, orders: 0, revenue: 0 }
};

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
    document.getElementById('statRevenue').innerText = `LKR ${data.revenue ? data.revenue.toFixed(2) : '0.00'}`;
    document.getElementById('statOrders').innerText = data.orders || 0;
    document.getElementById('statProducts').innerText = data.products || 0;
    document.getElementById('statCustomers').innerText = data.customers || 0;
  } catch (e) {
    console.error('Stats error:', e);
  }
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
    container.innerHTML += `
      <div class="list-card">
        <div class="list-card-info">
          <h4>${p.description}</h4>
          <p>Code: ${p.code} | Stock: ${p.qtyOnHand} pcs</p>
        </div>
        <div class="list-card-value">LKR ${p.unitPrice.toFixed(2)}</div>
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
      <div class="list-card">
        <div class="list-card-info">
          <h4>Order #${o.orderId}</h4>
          <p>Customer: ${o.customerId} | Date: ${o.date}</p>
        </div>
        <div class="list-card-value">LKR ${o.totalCost.toFixed(2)}</div>
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

  const customerId = document.getElementById('posCustomerSelect').value;
  const itemsString = state.cart.map(i => `${i.code}|${i.description}|${i.unitPrice}|${i.qty}`).join(';');

  try {
    const res = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerId: customerId,
        userEmail: 'iphone-pos@pos.com',
        items: itemsString
      })
    });

    const result = await res.json();
    if (result.success) {
      alert(`🎉 Order Placed Successfully!\nOrder ID: ${result.orderId}\nTotal: LKR ${result.total.toFixed(2)}`);
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

// Modal Handlers
function showCustomerModal() { document.getElementById('customerModal').classList.add('active'); }
function closeCustomerModal() { document.getElementById('customerModal').classList.remove('active'); }

async function saveCustomer() {
  const name = document.getElementById('custName').value.trim();
  const address = document.getElementById('custAddress').value.trim();
  const salary = document.getElementById('custSalary').value.trim();

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

function showProductModal() { document.getElementById('productModal').classList.add('active'); }
function closeProductModal() { document.getElementById('productModal').classList.remove('active'); }

async function saveProduct() {
  const description = document.getElementById('prodDesc').value.trim();
  const unitPrice = document.getElementById('prodPrice').value.trim();
  const qtyOnHand = document.getElementById('prodQty').value.trim();

  if (!description || !unitPrice || !qtyOnHand) {
    alert('Please fill product details');
    return;
  }

  try {
    const res = await fetch('/api/products', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description, unitPrice, qtyOnHand })
    });
    const data = await res.json();
    if (data.success) {
      alert('Product saved successfully!');
      closeProductModal();
      document.getElementById('prodDesc').value = '';
      document.getElementById('prodPrice').value = '';
      document.getElementById('prodQty').value = '';
      fetchProducts();
    }
  } catch (e) {
    alert('Error saving product');
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
