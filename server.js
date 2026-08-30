const express = require('express');
const cors = require('cors');
const path = require('path');
const mysql = require('mysql2/promise');
const sqlite3 = require('sqlite3').verbose();
const { open } = require('sqlite');
const bcrypt = require('bcryptjs');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'web')));

let db;

// Initialize Database (Cloud MySQL or local SQLite fallback)
async function initDb() {
  if (process.env.MYSQL_URL || process.env.MYSQLHOST) {
    try {
      db = await mysql.createPool({
        host: process.env.MYSQLHOST || 'localhost',
        user: process.env.MYSQLUSER || 'root',
        password: process.env.MYSQLPASSWORD || '1234',
        database: process.env.MYSQLDATABASE || 'pos_system_dsmp6',
        port: process.env.MYSQLPORT || 3306,
        waitForConnections: true,
        connectionLimit: 10,
        queueLimit: 0
      });
      console.log('✅ Connected to Cloud MySQL Database');
    } catch (e) {
      console.error('MySQL connection failed, falling back to SQLite:', e.message);
      await initSqlite();
    }
  } else {
    await initSqlite();
  }
}

async function initSqlite() {
  db = await open({
    filename: './pos_database.db',
    driver: sqlite3.Database
  });
  console.log('✅ Connected to SQLite Cloud Database');

  await db.exec(`
    CREATE TABLE IF NOT EXISTS user (
      user_id TEXT PRIMARY KEY,
      email TEXT UNIQUE NOT NULL,
      display_name TEXT NOT NULL,
      contact_number TEXT NOT NULL,
      password TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS customer (
      customer_id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      address TEXT NOT NULL,
      salary REAL DEFAULT 0.0
    );
    CREATE TABLE IF NOT EXISTS product (
      code TEXT PRIMARY KEY,
      description TEXT NOT NULL,
      unit_price REAL NOT NULL,
      qty_on_hand INTEGER NOT NULL,
      qr_code TEXT
    );
    CREATE TABLE IF NOT EXISTS orders (
      order_id TEXT PRIMARY KEY,
      date TEXT NOT NULL,
      total_cost REAL NOT NULL,
      customer_id TEXT,
      user_email TEXT
    );
  `);
}

// REST API Endpoints
app.post('/api/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    let user;
    if (db.query) {
      const [rows] = await db.query('SELECT * FROM user WHERE email = ?', [email]);
      user = rows[0];
    } else {
      user = await db.get('SELECT * FROM user WHERE email = ?', [email]);
    }

    if (!user) {
      return res.status(401).json({ success: false, msg: 'User not found' });
    }

    const match = await bcrypt.compare(password, user.password || '');
    if (match || password === user.password) {
      return res.json({ success: true, msg: 'Login successful', name: user.display_name });
    } else {
      return res.status(401).json({ success: false, msg: 'Wrong password' });
    }
  } catch (e) {
    res.status(500).json({ success: false, msg: e.message });
  }
});

app.post('/api/register', async (req, res) => {
  try {
    const { email, displayName, contactNumber, password } = req.body;
    const userId = 'U-' + Math.random().toString(36).substr(2, 6).toUpperCase();
    const hash = await bcrypt.hash(password, 10);

    if (db.query) {
      await db.query('INSERT INTO user (user_id, email, display_name, contact_number, password) VALUES (?, ?, ?, ?, ?)',
        [userId, email, displayName, contactNumber, hash]);
    } else {
      await db.run('INSERT INTO user (user_id, email, display_name, contact_number, password) VALUES (?, ?, ?, ?, ?)',
        [userId, email, displayName, contactNumber, hash]);
    }
    res.json({ success: true, msg: 'User registered successfully' });
  } catch (e) {
    res.status(500).json({ success: false, msg: e.message });
  }
});

app.get('/api/customers', async (req, res) => {
  try {
    let rows;
    if (db.query) {
      const [result] = await db.query('SELECT * FROM customer');
      rows = result;
    } else {
      rows = await db.all('SELECT * FROM customer');
    }
    const customers = rows.map(r => ({
      id: r.customer_id || r.id,
      name: r.name,
      address: r.address,
      salary: parseFloat(r.salary || 0)
    }));
    res.json(customers);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.post('/api/customers', async (req, res) => {
  try {
    const { name, address, salary } = req.body;
    const id = 'C-' + Math.random().toString(36).substr(2, 6).toUpperCase();
    const sal = parseFloat(salary || 0);

    if (db.query) {
      await db.query('INSERT INTO customer (customer_id, name, address, salary) VALUES (?, ?, ?, ?)',
        [id, name, address, sal]);
    } else {
      await db.run('INSERT INTO customer (customer_id, name, address, salary) VALUES (?, ?, ?, ?)',
        [id, name, address, sal]);
    }
    res.json({ success: true, msg: 'Customer saved', id });
  } catch (e) {
    res.status(500).json({ success: false, msg: e.message });
  }
});

app.get('/api/products', async (req, res) => {
  try {
    let rows;
    if (db.query) {
      const [result] = await db.query('SELECT * FROM product');
      rows = result;
    } else {
      rows = await db.all('SELECT * FROM product');
    }
    const products = rows.map(r => ({
      code: r.code,
      description: r.description,
      unitPrice: parseFloat(r.unit_price || 0),
      qtyOnHand: parseInt(r.qty_on_hand || 0),
      qrCode: r.qr_code || ('QR-' + r.code)
    }));
    res.json(products);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.post('/api/products', async (req, res) => {
  try {
    const { description, unitPrice, qtyOnHand } = req.body;
    const code = 'P-' + Math.random().toString(36).substr(2, 6).toUpperCase();
    const price = parseFloat(unitPrice || 0);
    const qty = parseInt(qtyOnHand || 0);

    if (db.query) {
      await db.query('INSERT INTO product (code, description, unit_price, qty_on_hand, qr_code) VALUES (?, ?, ?, ?, ?)',
        [code, description, price, qty, 'QR-' + code]);
    } else {
      await db.run('INSERT INTO product (code, description, unit_price, qty_on_hand, qr_code) VALUES (?, ?, ?, ?, ?)',
        [code, description, price, qty, 'QR-' + code]);
    }
    res.json({ success: true, msg: 'Product saved', code });
  } catch (e) {
    res.status(500).json({ success: false, msg: e.message });
  }
});

app.get('/api/orders', async (req, res) => {
  try {
    let rows;
    if (db.query) {
      const [result] = await db.query('SELECT * FROM orders ORDER BY order_id DESC');
      rows = result;
    } else {
      rows = await db.all('SELECT * FROM orders ORDER BY order_id DESC');
    }
    const orders = rows.map(r => ({
      orderId: r.order_id,
      date: r.date,
      totalCost: parseFloat(r.total_cost || 0),
      customerId: r.customer_id,
      userEmail: r.user_email
    }));
    res.json(orders);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.post('/api/orders', async (req, res) => {
  try {
    const { customerId, userEmail, items } = req.body;
    const orderId = 'ORD-' + Math.floor(100 + Math.random() * 900);
    const date = new Date().toISOString().replace('T', ' ').substring(0, 19);

    let totalCost = 0;
    if (items) {
      const itemArr = items.split(';');
      for (const itemStr of itemArr) {
        const parts = itemStr.split('|');
        if (parts.length >= 4) {
          const price = parseFloat(parts[2]);
          const qty = parseInt(parts[3]);
          totalCost += (price * qty);

          // Update product stock
          if (db.query) {
            await db.query('UPDATE product SET qty_on_hand = qty_on_hand - ? WHERE code = ?', [qty, parts[0]]);
          } else {
            await db.run('UPDATE product SET qty_on_hand = qty_on_hand - ? WHERE code = ?', [qty, parts[0]]);
          }
        }
      }
    }

    if (db.query) {
      await db.query('INSERT INTO orders (order_id, date, total_cost, customer_id, user_email) VALUES (?, ?, ?, ?, ?)',
        [orderId, date, totalCost, customerId, userEmail || 'mobile@pos.com']);
    } else {
      await db.run('INSERT INTO orders (order_id, date, total_cost, customer_id, user_email) VALUES (?, ?, ?, ?, ?)',
        [orderId, date, totalCost, customerId, userEmail || 'mobile@pos.com']);
    }

    res.json({ success: true, msg: 'Order placed successfully', orderId, total: totalCost });
  } catch (e) {
    res.status(500).json({ success: false, msg: e.message });
  }
});

app.get('/api/stats', async (req, res) => {
  try {
    let custCount = 0, prodCount = 0, orderCount = 0, revenue = 0;
    if (db.query) {
      const [[c]] = await db.query('SELECT COUNT(*) as cnt FROM customer');
      const [[p]] = await db.query('SELECT COUNT(*) as cnt FROM product');
      const [[o]] = await db.query('SELECT COUNT(*) as cnt, SUM(total_cost) as rev FROM orders');
      custCount = c.cnt;
      prodCount = p.cnt;
      orderCount = o.cnt;
      revenue = o.rev || 0;
    } else {
      const c = await db.get('SELECT COUNT(*) as cnt FROM customer');
      const p = await db.get('SELECT COUNT(*) as cnt FROM product');
      const o = await db.get('SELECT COUNT(*) as cnt, SUM(total_cost) as rev FROM orders');
      custCount = c.cnt;
      prodCount = p.cnt;
      orderCount = o.cnt;
      revenue = o.rev || 0;
    }
    res.json({ customers: custCount, products: prodCount, orders: orderCount, revenue: parseFloat(revenue || 0) });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// Serve frontend for all SPA routes
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'web', 'index.html'));
});

initDb().then(() => {
  app.listen(PORT, () => {
    console.log(`🌐 POS Cloud Web Application is running on port ${PORT}`);
  });
});
