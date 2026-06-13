# 15 — Views

> **Goal:** Create virtual tables (views) to simplify complex queries, enforce security, and provide stable interfaces.

---

## 📖 What is a View?

A **view** is a **named, stored SELECT query** that acts like a virtual table. It does not store data itself — it queries the underlying tables each time it's accessed.

```
Without view:                          With view:
──────────────────────────────────     ────────────────────────────────────
SELECT e.name, d.name AS dept,         CREATE VIEW employee_details AS
       e.salary                        SELECT e.name, d.name AS dept,
FROM employees e                              e.salary
JOIN departments d                     FROM employees e
  ON e.dept_id = d.id                  JOIN departments d ON e.dept_id = d.id;
WHERE e.is_active = TRUE;
                                       -- Now simply:
-- Repeat this complex query           SELECT * FROM employee_details;
-- everywhere you need it
```

---

## 🏗️ Creating Views

### Basic CREATE VIEW

```sql
CREATE VIEW active_employees AS
SELECT
    id,
    first_name,
    last_name,
    email,
    dept,
    salary
FROM employees
WHERE is_active = TRUE;

-- Use it like a table
SELECT * FROM active_employees;
SELECT name, salary FROM active_employees WHERE dept = 'Engineering';
SELECT COUNT(*) FROM active_employees;
```

### View with JOIN

```sql
CREATE VIEW order_summary AS
SELECT
    o.id           AS order_id,
    c.name         AS customer_name,
    c.email        AS customer_email,
    o.total,
    o.status,
    o.created_at
FROM orders o
JOIN customers c ON o.customer_id = c.id;

-- Now query easily
SELECT * FROM order_summary WHERE status = 'pending';
SELECT customer_name, SUM(total) FROM order_summary GROUP BY customer_name;
```

### View with Aggregation

```sql
CREATE VIEW dept_salary_stats AS
SELECT
    dept,
    COUNT(*)                    AS headcount,
    ROUND(AVG(salary), 2)      AS avg_salary,
    MIN(salary)                 AS min_salary,
    MAX(salary)                 AS max_salary,
    SUM(salary)                 AS total_payroll
FROM employees
WHERE is_active = TRUE
GROUP BY dept;

SELECT * FROM dept_salary_stats ORDER BY avg_salary DESC;
```

---

## 🔒 Benefits of Views

```
1. Simplification    — Hide JOIN complexity behind a simple name
2. Security          — Show only allowed columns (hide salary, SSN, etc.)
3. Consistency       — One definition, used everywhere — change once, works everywhere
4. Abstraction       — Apps query the view; underlying schema can change
5. Reusability       — Common queries defined once, reused everywhere
```

### Security with Views

```sql
-- Original table has sensitive data
-- employees: id, name, email, salary, ssn, bank_account

-- View for HR (sees salary, not bank details)
CREATE VIEW hr_employee_view AS
SELECT id, name, email, salary, dept
FROM employees;

-- View for public dashboard (no salary or PII)
CREATE VIEW public_employee_view AS
SELECT id, first_name, dept, title
FROM employees
WHERE is_active = TRUE;

-- Grant access to view, not underlying table
GRANT SELECT ON public_employee_view TO app_user;
-- REVOKE access to employees directly
REVOKE SELECT ON employees FROM app_user;
```

---

## 🔄 Updating Views

Views can be **updated** (INSERT/UPDATE/DELETE through them) if they meet these conditions:
1. Based on a **single table**
2. **No DISTINCT**, GROUP BY, HAVING, aggregates, or UNION
3. **No subqueries** in WHERE referencing the same table
4. **All NOT NULL columns** without DEFAULT must be in the view

```sql
-- Simple, updatable view
CREATE VIEW active_staff AS
SELECT id, name, salary, dept
FROM employees
WHERE is_active = TRUE;

-- UPDATE through the view
UPDATE active_staff SET salary = salary * 1.05 WHERE dept = 'Engineering';
-- This updates the underlying employees table!

-- INSERT through the view
INSERT INTO active_staff (name, salary, dept) VALUES ('Alice', 80000, 'HR');
-- is_active defaults to TRUE in the underlying table
```

### WITH CHECK OPTION

Ensures that INSERT/UPDATE through the view still satisfies the view's WHERE clause:

```sql
CREATE VIEW active_staff AS
SELECT id, name, salary, is_active
FROM employees
WHERE is_active = TRUE
WITH CHECK OPTION;

-- ❌ This would be rejected:
UPDATE active_staff SET is_active = FALSE WHERE id = 1;
-- ERROR: new row violates check option for view "active_staff"
-- (updating to FALSE would make the row invisible to the view)

-- ✅ This is allowed:
UPDATE active_staff SET salary = 90000 WHERE id = 1;
```

---

## 🔄 Replacing / Altering Views

```sql
-- Replace entire view definition (PostgreSQL / MySQL)
CREATE OR REPLACE VIEW active_employees AS
SELECT id, first_name, last_name, email, dept, salary, hire_date
FROM employees
WHERE is_active = TRUE;
-- Replaces the old definition if view exists, creates if not

-- SQL Server: must use ALTER VIEW
ALTER VIEW active_employees AS
SELECT id, first_name, last_name, email, dept
FROM employees
WHERE is_active = TRUE;
```

---

## 🗑️ Dropping Views

```sql
DROP VIEW active_employees;
DROP VIEW IF EXISTS active_employees;    -- no error if doesn't exist
DROP VIEW IF EXISTS v1, v2, v3;         -- drop multiple
DROP VIEW active_employees CASCADE;     -- also drop dependent objects
```

---

## 🚀 Materialized Views (PostgreSQL, Oracle)

A **Materialized View** is a view whose result is **physically stored** on disk. Unlike a regular view, it doesn't recompute every time — it stores a snapshot.

```
Regular View:                           Materialized View:
─────────────────────────────────       ──────────────────────────────────
Query runs every time you read it       Data is stored physically
Always up to date                       May be stale (until refreshed)
No extra storage                        Uses disk space
Slow for expensive queries              Fast (reads cached data)
```

```sql
-- PostgreSQL: Create materialized view
CREATE MATERIALIZED VIEW monthly_revenue AS
SELECT
    DATE_TRUNC('month', created_at)  AS month,
    SUM(total)                        AS revenue,
    COUNT(*)                          AS order_count
FROM orders
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month;

-- Query the materialized view (instant!)
SELECT * FROM monthly_revenue;

-- Refresh the data (brings it up to date)
REFRESH MATERIALIZED VIEW monthly_revenue;

-- Refresh without blocking reads (PostgreSQL 9.4+)
REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_revenue;
-- CONCURRENTLY requires a UNIQUE index on the view

-- Add index for even faster queries
CREATE UNIQUE INDEX idx_monthly_revenue_month ON monthly_revenue(month);

-- Drop materialized view
DROP MATERIALIZED VIEW monthly_revenue;
```

### When to Use Materialized Views

```
Use Materialized Views when:
✅ Query is very slow (complex joins, aggregations over millions of rows)
✅ Data doesn't change frequently (daily reports, monthly summaries)
✅ Staleness is acceptable (dashboard updated once per hour)

Don't use when:
❌ Data must be real-time
❌ Underlying data changes very frequently
❌ Storage is very constrained
```

---

## 🗺️ Views in Practice

```sql
-- E-commerce dashboard views

-- 1. Customer order summary
CREATE VIEW customer_order_summary AS
SELECT
    c.id,
    c.name,
    c.email,
    COUNT(o.id)         AS total_orders,
    COALESCE(SUM(o.total), 0) AS lifetime_value,
    MAX(o.created_at)   AS last_order_date
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.name, c.email;

-- 2. Low stock products
CREATE VIEW low_stock_products AS
SELECT id, name, stock, category_id
FROM products
WHERE stock < 10 AND is_active = TRUE;

-- 3. Today's orders
CREATE VIEW todays_orders AS
SELECT * FROM orders
WHERE created_at::DATE = CURRENT_DATE;

-- 4. Revenue by category (materialized — refreshed nightly)
CREATE MATERIALIZED VIEW category_revenue AS
SELECT
    c.name AS category,
    SUM(oi.qty * oi.unit_price) AS total_revenue
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN categories c ON p.category_id = c.id
GROUP BY c.name;

CREATE UNIQUE INDEX ON category_revenue(category);
```

---

## 🔑 Key Takeaways

| Concept | Detail |
|---------|--------|
| View | Virtual table (stored query, not data) |
| Regular View | Always fresh, no extra storage, may be slow |
| Updatable View | Single table, no aggregates, no DISTINCT |
| WITH CHECK OPTION | Enforces view's WHERE on INSERT/UPDATE |
| CREATE OR REPLACE | Update view definition without DROP |
| Materialized View | Physically stored snapshot — must REFRESH |
| REFRESH CONCURRENTLY | Refresh without locking reads (needs unique index) |

**Best Practices:**
- Name views clearly: `vw_active_employees` or `active_employees` (descriptive)
- Don't use `SELECT *` in views — list columns explicitly
- Use materialized views for heavy analytics queries
- Refresh materialized views on a schedule (cron job, pg_cron)
- Views are great for security — expose only what's needed

---

**← Previous:** [14 — Subqueries](./14-subqueries.md)
**Next →** [16 — Set Operators](./16-set-operators.md)
