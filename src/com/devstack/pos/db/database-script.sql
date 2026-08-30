CREATE DATABASE IF NOT EXISTS pos_system_dsmp6;
USE pos_system_dsmp6;

CREATE TABLE IF NOT EXISTS user(
  user_id VARCHAR(80) PRIMARY KEY,
  email VARCHAR(100) UNIQUE NOT NULL,
  display_name VARCHAR(45) NOT NULL,
  contact_number VARCHAR(20) NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS customer(
  customer_id VARCHAR(80) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  salary DOUBLE DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS product(
  code VARCHAR(80) PRIMARY KEY,
  description VARCHAR(255) NOT NULL,
  unit_price DOUBLE NOT NULL,
  qty_on_hand INT NOT NULL,
  qr_code VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders(
  order_id VARCHAR(80) PRIMARY KEY,
  date VARCHAR(50) NOT NULL,
  total_cost DOUBLE NOT NULL,
  customer_id VARCHAR(80),
  user_email VARCHAR(100),
  CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS order_detail(
  order_id VARCHAR(80) NOT NULL,
  product_code VARCHAR(80) NOT NULL,
  unit_price DOUBLE NOT NULL,
  qty INT NOT NULL,
  discount DOUBLE DEFAULT 0.0,
  PRIMARY KEY (order_id, product_code),
  CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_product FOREIGN KEY (product_code) REFERENCES product(code) ON DELETE CASCADE ON UPDATE CASCADE
);
