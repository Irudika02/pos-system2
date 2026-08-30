const express = require('express');
const cors = require('cors');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();
const { open } = require('sqlite');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'web')));

// ─── DB: lazy singleton ───────────────────────────────────────
let _db = null;

async function getDb() {
  if (_db) return _db;

  // /tmp is writable on Vercel; use local path when running locally
  const dbPath = process.env.VERCEL
    ? '/tmp/pos_database.db'
    : path.join(__dirname, 'pos_database.db');

  _db = await open({ filename: dbPath, driver: sqlite3.Database });

  await _db.exec(`
    PRAGMA journal_mode = WAL;
    PRAGMA encoding = "UTF-8";

    CREATE TABLE IF NOT EXISTS user (
      user_id      TEXT PRIMARY KEY,
      email        TEXT UNIQUE NOT NULL,
      display_name TEXT NOT NULL,
      contact_number TEXT NOT NULL,
      password     TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS customer (
      customer_id TEXT PRIMARY KEY,
      name        TEXT NOT NULL,
      address     TEXT NOT NULL,
      salary      REAL DEFAULT 0.0
    );
    CREATE TABLE IF NOT EXISTS product (
      code         TEXT PRIMARY KEY,
      description  TEXT NOT NULL,
      buying_price REAL DEFAULT 0.0,
      unit_price   REAL NOT NULL,
      qty_on_hand  INTEGER NOT NULL,
      qr_code      TEXT
    );
    CREATE TABLE IF NOT EXISTS orders (
      order_id     TEXT PRIMARY KEY,
      date         TEXT NOT NULL,
      total_cost   REAL NOT NULL,
      total_profit REAL DEFAULT 0.0,
      customer_id  TEXT,
      user_email   TEXT
    );
  `);

  console.log('✅ SQLite DB ready:', dbPath);
  return _db;
}

// ─── Middleware: ensure DB is ready ──────────────────────────
app.use(async (req, res, next) => {
  try {
    await getDb();
    next();
  } catch (e) {
    console.error('DB init error:', e.message);
    res.status(500).json({ error: 'Database initialization failed', detail: e.message });
  }
});

// ─── Helper ──────────────────────────────────────────────────
function uid(prefix) {
  return prefix + '-' + Math.random().toString(36).substr(2, 6).toUpperCase();
}

// ─── AUTH ─────────────────────────────────────────────────────
app.post('/api/login', async (req, res) => {
  try {
    const db = await getDb();
    const { email, password } = req.body;
    const user = await db.get('SELECT * FROM user WHERE email = ?', [email]);
    if (!user) return res.status(401).json({ success: false, msg: 'User not found' });
    const match = await bcrypt.compare(password, user.password || '').catch(() => false);
    if (match || password === user.password) {
      return res.json({ success: true, msg: 'Login successful', name: user.display_name });
    }
    res.status(401).json({ success: false, msg: 'Wrong password' });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

app.post('/api/register', async (req, res) => {
  try {
    const db = await getDb();
    const { email, displayName, contactNumber, password } = req.body;
    const hash = await bcrypt.hash(password, 10);
    await db.run(
      'INSERT INTO user (user_id, email, display_name, contact_number, password) VALUES (?, ?, ?, ?, ?)',
      [uid('U'), email, displayName, contactNumber, hash]
    );
    res.json({ success: true, msg: 'Registered' });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

// ─── CUSTOMERS ────────────────────────────────────────────────
app.get('/api/customers', async (_req, res) => {
  try {
    const db = await getDb();
    const rows = await db.all('SELECT * FROM customer');
    res.json(rows.map(r => ({
      id: r.customer_id, name: r.name, address: r.address,
      salary: parseFloat(r.salary || 0)
    })));
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/customers', async (req, res) => {
  try {
    const db = await getDb();
    const { name, address, salary } = req.body;
    const id = uid('C');
    await db.run(
      'INSERT INTO customer (customer_id, name, address, salary) VALUES (?, ?, ?, ?)',
      [id, String(name), String(address), parseFloat(salary || 0)]
    );
    res.json({ success: true, id });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

// ─── PRODUCTS ─────────────────────────────────────────────────
app.get('/api/products', async (_req, res) => {
  try {
    const db = await getDb();
    const rows = await db.all('SELECT * FROM product');
    res.json(rows.map(r => ({
      code:        r.code,
      description: r.description,
      buyingPrice: parseFloat(r.buying_price || 0),
      unitPrice:   parseFloat(r.unit_price || 0),
      qtyOnHand:   parseInt(r.qty_on_hand || 0),
      qrCode:      r.qr_code || ('QR-' + r.code)
    })));
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/products', async (req, res) => {
  try {
    const db = await getDb();
    const { description, buyingPrice, unitPrice, qtyOnHand } = req.body;

    if (!description || description.toString().trim() === '') {
      return res.status(400).json({ success: false, msg: 'Description is required' });
    }

    const code = uid('P');
    const buy  = parseFloat(buyingPrice || 0);
    const sell = parseFloat(unitPrice   || 0);
    const qty  = parseInt(qtyOnHand    || 0);

    await db.run(
      'INSERT INTO product (code, description, buying_price, unit_price, qty_on_hand, qr_code) VALUES (?, ?, ?, ?, ?, ?)',
      [code, String(description).trim(), buy, sell, qty, 'QR-' + code]
    );
    res.json({ success: true, msg: 'Product saved', code });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

app.put('/api/products/:code', async (req, res) => {
  try {
    const db = await getDb();
    const { code } = req.params;
    const { description, buyingPrice, unitPrice, qtyOnHand } = req.body;
    await db.run(
      'UPDATE product SET description = ?, buying_price = ?, unit_price = ?, qty_on_hand = ? WHERE code = ?',
      [String(description).trim(), parseFloat(buyingPrice || 0), parseFloat(unitPrice || 0), parseInt(qtyOnHand || 0), code]
    );
    res.json({ success: true, msg: 'Updated' });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

app.delete('/api/products/:code', async (req, res) => {
  try {
    const db = await getDb();
    await db.run('DELETE FROM product WHERE code = ?', [req.params.code]);
    res.json({ success: true, msg: 'Deleted' });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

// ─── ORDERS ───────────────────────────────────────────────────
app.get('/api/orders', async (_req, res) => {
  try {
    const db = await getDb();
    const rows = await db.all('SELECT * FROM orders ORDER BY rowid DESC');
    res.json(rows.map(r => ({
      orderId:     r.order_id,
      date:        r.date,
      totalCost:   parseFloat(r.total_cost   || 0),
      totalProfit: parseFloat(r.total_profit || 0),
      customerId:  r.customer_id,
      userEmail:   r.user_email
    })));
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/orders', async (req, res) => {
  try {
    const db = await getDb();
    const { customerId, userEmail, items } = req.body;
    const orderId = 'ORD-' + Math.floor(100 + Math.random() * 900);
    const date    = new Date().toISOString().replace('T', ' ').substring(0, 19);
    let totalCost = 0, totalProfit = 0;

    if (items) {
      for (const itemStr of String(items).split(';')) {
        const parts = itemStr.split('|');
        if (parts.length >= 4) {
          const code  = parts[0];
          const price = parseFloat(parts[2]) || 0;
          const qty   = parseInt(parts[3])   || 0;
          totalCost += price * qty;
          const prod = await db.get('SELECT buying_price FROM product WHERE code = ?', [code]);
          const buyP = prod ? parseFloat(prod.buying_price || 0) : 0;
          totalProfit += (price - buyP) * qty;
          await db.run('UPDATE product SET qty_on_hand = MAX(0, qty_on_hand - ?) WHERE code = ?', [qty, code]);
        }
      }
    }

    await db.run(
      'INSERT INTO orders (order_id, date, total_cost, total_profit, customer_id, user_email) VALUES (?, ?, ?, ?, ?, ?)',
      [orderId, date, totalCost, totalProfit, customerId, userEmail || 'mobile@pos.com']
    );
    res.json({ success: true, orderId, total: totalCost, profit: totalProfit });
  } catch (e) { res.status(500).json({ success: false, msg: e.message }); }
});

// ─── STATS ────────────────────────────────────────────────────
app.get('/api/stats', async (_req, res) => {
  try {
    const db = await getDb();
    const c = await db.get('SELECT COUNT(*) as cnt FROM customer');
    const p = await db.get('SELECT COUNT(*) as cnt FROM product');
    const o = await db.get('SELECT COUNT(*) as cnt, SUM(total_cost) as rev, SUM(total_profit) as prof FROM orders');
    res.json({
      customers: c.cnt || 0,
      products:  p.cnt || 0,
      orders:    o.cnt || 0,
      revenue:   parseFloat(o.rev  || 0),
      profit:    parseFloat(o.prof || 0)
    });
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// ─── CATCH-ALL: serve frontend ────────────────────────────────
app.get('*', (_req, res) => {
  res.sendFile(path.join(__dirname, 'web', 'index.html'));
});

// ─── START (local dev) / EXPORT (Vercel) ─────────────────────
if (require.main === module) {
  // Running directly with `node server.js` - start HTTP server
  getDb().then(() => {
    app.listen(PORT, () => console.log(`🌐 POS running on http://localhost:${PORT}`));
  });
}

// Vercel needs the Express app exported as a module
module.exports = app;
