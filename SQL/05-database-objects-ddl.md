# 05 — Database Objects (DDL)

> **Goal:** Learn how to create, alter, and remove database structures using Data Definition Language (DDL).

---

## 📖 What is DDL?

**DDL (Data Definition Language)** is the subset of SQL used to **define and manage database structures** — not the data itself.

```
SQL Commands
├── DDL  — CREATE, ALTER, DROP, TRUNCATE, RENAME
├── DML  — INSERT, UPDATE, DELETE, MERGE
├── DQL  — SELECT
├── TCL  — BEGIN, COMMIT, ROLLBACK, SAVEPOINT
└── DCL  — GRANT, REVOKE
```

> ⚠️ DDL statements in most RDBMS are **auto-committed** (except in PostgreSQL where they participate in transactions).

---

## 🏗️ Databases & Schemas

```sql
-- Create a database
CREATE DATABASE myapp;

-- Switch to it (MySQL/psql CLI)
USE myapp;          -- MySQL
\c myapp            -- PostgreSQL psql

-- Create a schema (namespace inside a database)
CREATE SCHEMA sales;
CREATE SCHEMA hr;

-- Use schema-qualified names
CREATE TABLE sales.orders ( ... );
CREATE TABLE hr.employees ( ... );

-- Drop database
DROP DATABASE myapp;
```

---

## 📋 CREATE TABLE

### Basic Syntax

```sql
CREATE TABLE table_name (
    column1  datatype  [constraints],
    column2  datatype  [constraints],
    ...
    [table_constraints]
);
```

### Comprehensive Example

```sql
CREATE TABLE employees (
    -- Column definitions
    id          SERIAL              PRIMARY KEY,
    first_name  VARCHAR(50)         NOT NULL,
    last_name   VARCHAR(50)         NOT NULL,
    email       VARCHAR(100)        NOT NULL UNIQUE,
    phone       VARCHAR(20),
    salary      DECIMAL(10, 2)      NOT NULL CHECK (salary > 0),
    hire_date   DATE                NOT NULL DEFAULT CURRENT_DATE,
    is_active   BOOLEAN             NOT NULL DEFAULT TRUE,
    dept_id     INT,
    manager_id  INT,
    created_at  TIMESTAMP           DEFAULT NOW(),
    updated_at  TIMESTAMP           DEFAULT NOW(),

    -- Table-level constraints
    CONSTRAINT fk_dept    FOREIGN KEY (dept_id)    REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employees(id)   ON DELETE SET NULL,
    CONSTRAINT check_hire CHECK (hire_date >= '2000-01-01')
);
```

### CREATE TABLE … AS SELECT

Create a new table from a query result (copies structure + data):

```sql
-- Copy structure AND data
CREATE TABLE employees_backup AS
SELECT * FROM employees;

-- Copy structure only (no rows)
CREATE TABLE employees_archive AS
SELECT * FROM employees WHERE 1 = 0;
```

### IF NOT EXISTS

```sql
CREATE TABLE IF NOT EXISTS products (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100)
);
-- Creates table only if it doesn't exist — no error if it does
```

---

## ✏️ ALTER TABLE

Modify an existing table's structure.

### Add a Column

```sql
ALTER TABLE employees
ADD COLUMN middle_name VARCHAR(50);

-- With default value
ALTER TABLE employees
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';
```

### Drop a Column

```sql
ALTER TABLE employees
DROP COLUMN middle_name;

-- PostgreSQL: avoid errors if column doesn't exist
ALTER TABLE employees
DROP COLUMN IF EXISTS middle_name;
```

### Rename a Column

```sql
-- PostgreSQL / MySQL 8+
ALTER TABLE employees
RENAME COLUMN first_name TO fname;
```

### Modify Column Data Type

```sql
-- PostgreSQL
ALTER TABLE employees
ALTER COLUMN phone TYPE VARCHAR(30);

-- MySQL
ALTER TABLE employees
MODIFY COLUMN phone VARCHAR(30);

-- SQL Server
ALTER TABLE employees
ALTER COLUMN phone VARCHAR(30);
```

### Set / Drop Column Default

```sql
-- PostgreSQL: set default
ALTER TABLE employees
ALTER COLUMN status SET DEFAULT 'active';

-- PostgreSQL: remove default
ALTER TABLE employees
ALTER COLUMN status DROP DEFAULT;
```

### Set / Drop NOT NULL

```sql
-- PostgreSQL
ALTER TABLE employees ALTER COLUMN phone SET NOT NULL;
ALTER TABLE employees ALTER COLUMN phone DROP NOT NULL;
```

### Rename Table

```sql
-- PostgreSQL / MySQL
ALTER TABLE employees RENAME TO staff;
```

### Add Constraint

```sql
-- Add PRIMARY KEY (if not set at create time)
ALTER TABLE orders ADD PRIMARY KEY (id);

-- Add UNIQUE constraint
ALTER TABLE employees ADD CONSTRAINT uq_email UNIQUE (email);

-- Add CHECK constraint
ALTER TABLE products ADD CONSTRAINT chk_price CHECK (price > 0);

-- Add FOREIGN KEY
ALTER TABLE orders
ADD CONSTRAINT fk_customer
FOREIGN KEY (customer_id) REFERENCES customers(id);
```

### Drop Constraint

```sql
-- PostgreSQL / SQL Server (use constraint name)
ALTER TABLE employees DROP CONSTRAINT uq_email;
ALTER TABLE orders    DROP CONSTRAINT fk_customer;

-- MySQL
ALTER TABLE employees DROP INDEX uq_email;       -- for UNIQUE
ALTER TABLE orders    DROP FOREIGN KEY fk_customer;
```

### Modify Constraint

Constraints can't be modified directly — drop and re-add:

```sql
-- Step 1: Drop old constraint
ALTER TABLE orders DROP CONSTRAINT fk_customer;

-- Step 2: Add new constraint with different rules
ALTER TABLE orders
ADD CONSTRAINT fk_customer
FOREIGN KEY (customer_id) REFERENCES customers(id)
ON DELETE CASCADE;   -- changed from default to CASCADE
```

---

## 🗑️ DROP TABLE

Permanently **delete a table** and all its data.

```sql
-- Drop a table
DROP TABLE employees;

-- Drop only if it exists (no error otherwise)
DROP TABLE IF EXISTS employees;

-- Drop multiple tables
DROP TABLE IF EXISTS orders, order_items, products;
```

### CASCADE vs RESTRICT on DROP

```sql
-- RESTRICT (default): fail if other tables depend on this one
DROP TABLE customers;
-- ERROR: table "orders" depends on table "customers" (foreign key)

-- CASCADE: also drop dependent objects (views, FK constraints)
DROP TABLE customers CASCADE;
-- Drops customers AND removes FK constraints in orders
-- (doesn't delete orders rows — just the constraint)
```

---

## 🧹 TRUNCATE TABLE

**Removes all rows** from a table but **keeps the structure**. Faster than `DELETE FROM table` because it doesn't log individual row deletions.

```sql
-- Remove all rows
TRUNCATE TABLE employees;

-- With RESTART IDENTITY (resets SERIAL/AUTO_INCREMENT counter)
TRUNCATE TABLE employees RESTART IDENTITY;

-- Without resetting identity
TRUNCATE TABLE employees CONTINUE IDENTITY;

-- Truncate multiple tables
TRUNCATE TABLE orders, order_items RESTART IDENTITY CASCADE;
```

### DELETE vs TRUNCATE vs DROP

```
┌─────────────┬──────────┬─────────────┬────────────┬───────────────┐
│ Operation   │ Removes  │ Keeps       │ WHERE      │ Speed         │
│             │ Data     │ Structure   │ Clause     │               │
├─────────────┼──────────┼─────────────┼────────────┼───────────────┤
│ DELETE      │ ✅ rows  │ ✅ table   │ ✅ yes    │ Slow (logged) │
│ TRUNCATE    │ ✅ rows  │ ✅ table   │ ❌ no     │ Fast          │
│ DROP        │ ✅ rows  │ ❌ table   │ ❌ no     │ Fast          │
└─────────────┴──────────┴─────────────┴────────────┴───────────────┘
```

---

## 📊 Indexes (DDL)

Covered in detail in [Chapter 18](./18-indexing.md), but index creation is DDL:

```sql
-- Create index
CREATE INDEX idx_employees_email   ON employees (email);
CREATE UNIQUE INDEX idx_uq_email   ON employees (email);
CREATE INDEX idx_emp_dept          ON employees (dept_id, hire_date);

-- Drop index
DROP INDEX idx_employees_email;           -- PostgreSQL
DROP INDEX idx_employees_email ON employees;  -- MySQL
```

---

## 🔭 Views (DDL)

Covered in [Chapter 15](./15-views.md):

```sql
CREATE VIEW active_employees AS
SELECT id, first_name, last_name, email
FROM employees
WHERE is_active = TRUE;

DROP VIEW active_employees;
```

---

## 🗺️ DDL Workflow Diagram

```
Planning Phase
      │
      ▼
Design ER Diagram
      │
      ▼
CREATE DATABASE / SCHEMA
      │
      ▼
CREATE TABLE (with constraints)
      │
      ├── Add columns     →  ALTER TABLE ADD COLUMN
      ├── Remove columns  →  ALTER TABLE DROP COLUMN
      ├── Change types    →  ALTER TABLE ALTER/MODIFY COLUMN
      ├── Add constraints →  ALTER TABLE ADD CONSTRAINT
      └── Remove table    →  DROP TABLE
```

---

## ✅ Complete Schema Example: E-Commerce

```sql
-- 1. Schema
CREATE SCHEMA ecommerce;

-- 2. Categories
CREATE TABLE ecommerce.categories (
    id      SERIAL      PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    slug    VARCHAR(100) NOT NULL UNIQUE
);

-- 3. Products
CREATE TABLE ecommerce.products (
    id           SERIAL          PRIMARY KEY,
    category_id  INT             NOT NULL,
    name         VARCHAR(200)    NOT NULL,
    slug         VARCHAR(200)    NOT NULL UNIQUE,
    description  TEXT,
    price        DECIMAL(10, 2)  NOT NULL CHECK (price >= 0),
    stock        INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
    is_active    BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP       DEFAULT NOW(),
    FOREIGN KEY (category_id) REFERENCES ecommerce.categories(id)
);

-- 4. Customers
CREATE TABLE ecommerce.customers (
    id         SERIAL          PRIMARY KEY,
    email      VARCHAR(150)    NOT NULL UNIQUE,
    name       VARCHAR(100)    NOT NULL,
    phone      VARCHAR(20),
    created_at TIMESTAMP       DEFAULT NOW()
);

-- 5. Orders
CREATE TABLE ecommerce.orders (
    id          SERIAL          PRIMARY KEY,
    customer_id INT             NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'pending'
                                CHECK (status IN ('pending','paid','shipped','delivered','cancelled')),
    total       DECIMAL(12, 2)  NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       DEFAULT NOW(),
    FOREIGN KEY (customer_id) REFERENCES ecommerce.customers(id)
);

-- 6. Order Items (junction between orders and products)
CREATE TABLE ecommerce.order_items (
    id          SERIAL          PRIMARY KEY,
    order_id    INT             NOT NULL,
    product_id  INT             NOT NULL,
    qty         INT             NOT NULL CHECK (qty > 0),
    unit_price  DECIMAL(10, 2)  NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES ecommerce.orders(id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ecommerce.products(id)
);

-- 7. Indexes
CREATE INDEX idx_products_category ON ecommerce.products(category_id);
CREATE INDEX idx_orders_customer   ON ecommerce.orders(customer_id);
CREATE INDEX idx_orders_status     ON ecommerce.orders(status);
CREATE INDEX idx_items_order       ON ecommerce.order_items(order_id);
```

---

## 🔑 Key Takeaways

| Statement | Purpose |
|-----------|---------|
| `CREATE TABLE` | Define new table with columns and constraints |
| `CREATE TABLE AS SELECT` | Create + populate from query |
| `ALTER TABLE ADD COLUMN` | Add new column |
| `ALTER TABLE DROP COLUMN` | Remove column |
| `ALTER TABLE ALTER COLUMN` | Change type/default/nullability |
| `ALTER TABLE ADD CONSTRAINT` | Add a new constraint |
| `ALTER TABLE DROP CONSTRAINT` | Remove a constraint |
| `DROP TABLE` | Permanently delete table and data |
| `TRUNCATE TABLE` | Delete all rows, keep structure, fast |

**Best Practices:**
- Always name your constraints (`CONSTRAINT fk_...`) — makes them easier to drop
- Use `IF NOT EXISTS` / `IF EXISTS` in scripts to make them idempotent
- Add indexes when creating FKs (most RDBMS don't do it automatically)
- `TRUNCATE` is faster than `DELETE` for wiping all data

---

**← Previous:** [04 — Data Types](./04-data-types.md)
**Next →** [06 — DML (Insert, Update, Delete)](./06-dml.md)
