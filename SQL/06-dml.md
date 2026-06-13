# 06 — DML: Insert, Update, Delete, Merge

> **Goal:** Master how to add, modify, and remove data using Data Manipulation Language (DML).

---

## 📖 What is DML?

**DML (Data Manipulation Language)** operates on the **data inside tables** — not the structure.

| Statement | Purpose |
|-----------|---------|
| `INSERT` | Add new rows |
| `UPDATE` | Modify existing rows |
| `DELETE` | Remove rows |
| `MERGE` / `UPSERT` | Insert or update in one statement |

---

## ➕ INSERT

### Single Row Insert

```sql
-- Full column list (best practice — explicit)
INSERT INTO employees (first_name, last_name, email, salary, dept_id)
VALUES ('Alice', 'Smith', 'alice@company.com', 75000.00, 10);

-- Without specifying columns (must match ALL columns in order)
INSERT INTO employees
VALUES (DEFAULT, 'Alice', 'Smith', 'alice@company.com', NULL, 75000.00, CURRENT_DATE, TRUE, 10, NULL, NOW(), NOW());
-- ⚠️ Risky — breaks if schema changes. Always prefer named columns.
```

### Multiple Row Insert

More efficient than multiple single inserts (fewer round trips):

```sql
INSERT INTO employees (first_name, last_name, email, salary, dept_id)
VALUES
    ('Bob',   'Jones',   'bob@company.com',   82000.00, 20),
    ('Carol', 'Lee',     'carol@company.com', 91000.00, 10),
    ('David', 'Chen',    'david@company.com', 68000.00, 30),
    ('Eve',   'Johnson', 'eve@company.com',   77000.00, 20);
```

### Insert from SELECT

Copy data from another table or query:

```sql
-- Insert from another table
INSERT INTO employees_archive (first_name, last_name, email, salary)
SELECT first_name, last_name, email, salary
FROM employees
WHERE is_active = FALSE;

-- Insert with computed values
INSERT INTO order_totals (order_id, total)
SELECT order_id, SUM(qty * unit_price)
FROM order_items
GROUP BY order_id;
```

### INSERT with RETURNING (PostgreSQL)

Get the auto-generated ID after insert:

```sql
INSERT INTO employees (first_name, last_name, email)
VALUES ('Frank', 'Brown', 'frank@company.com')
RETURNING id, created_at;

-- Result:
--  id  | created_at
-- -----+---------------------
--   7  | 2025-01-15 10:30:00
```

---

## ✏️ UPDATE

### Update Single Row

```sql
UPDATE employees
SET salary = 80000.00
WHERE id = 1;
```

### Update Multiple Columns

```sql
UPDATE employees
SET
    salary     = 90000.00,
    dept_id    = 20,
    updated_at = NOW()
WHERE id = 1;
```

### Update Multiple Rows (Condition)

```sql
-- Give all Finance dept employees a 10% raise
UPDATE employees
SET salary = salary * 1.10
WHERE dept_id = 10;

-- Deactivate employees not logged in for 1 year
UPDATE users
SET is_active = FALSE
WHERE last_login < NOW() - INTERVAL '1 year';
```

### Update All Rows

```sql
-- ⚠️ No WHERE = affects EVERY row!
UPDATE employees
SET updated_at = NOW();    -- stamps all employees
```

### Update with JOIN (PostgreSQL)

```sql
-- Update employees with their department's budget bonus
UPDATE employees e
SET salary = e.salary + d.bonus
FROM departments d
WHERE e.dept_id = d.id
AND d.name = 'Engineering';
```

```sql
-- MySQL equivalent (JOIN syntax)
UPDATE employees e
JOIN departments d ON e.dept_id = d.id
SET e.salary = e.salary + d.bonus
WHERE d.name = 'Engineering';
```

### Update with Subquery

```sql
-- Set each employee's salary to the avg of their department
UPDATE employees e
SET salary = (
    SELECT AVG(salary)
    FROM employees e2
    WHERE e2.dept_id = e.dept_id
)
WHERE dept_id IS NOT NULL;
```

### UPDATE with RETURNING (PostgreSQL)

```sql
UPDATE employees
SET salary = salary * 1.10
WHERE dept_id = 10
RETURNING id, first_name, salary;
-- Shows updated rows with new salary values
```

---

## 🗑️ DELETE

### Delete Specific Rows

```sql
-- Delete one employee
DELETE FROM employees
WHERE id = 5;

-- Delete all inactive users
DELETE FROM users
WHERE is_active = FALSE;

-- Delete old records
DELETE FROM logs
WHERE created_at < NOW() - INTERVAL '90 days';
```

### Delete All Rows

```sql
-- ⚠️ Deletes ALL rows — no WHERE clause!
DELETE FROM temp_data;
-- (Use TRUNCATE instead for better performance)
```

### Delete with Subquery

```sql
-- Delete orders for customers who have been removed
DELETE FROM orders
WHERE customer_id NOT IN (
    SELECT id FROM customers
);

-- Or with EXISTS (better performance)
DELETE FROM orders o
WHERE NOT EXISTS (
    SELECT 1 FROM customers c
    WHERE c.id = o.customer_id
);
```

### Delete with JOIN (MySQL)

```sql
-- MySQL: delete with JOIN
DELETE o
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE c.status = 'banned';
```

### DELETE with RETURNING (PostgreSQL)

```sql
DELETE FROM logs
WHERE created_at < NOW() - INTERVAL '90 days'
RETURNING id, action, created_at;
-- Returns the deleted rows for audit purposes
```

---

## 🔄 MERGE / UPSERT

**Upsert** = INSERT if not exists, UPDATE if exists.

### PostgreSQL — ON CONFLICT

```sql
-- Insert; if conflict on email, update salary
INSERT INTO employees (email, first_name, last_name, salary)
VALUES ('alice@company.com', 'Alice', 'Smith', 85000.00)
ON CONFLICT (email)
DO UPDATE SET
    salary     = EXCLUDED.salary,    -- EXCLUDED = the values that conflicted
    updated_at = NOW();

-- Insert; if conflict, do nothing (ignore duplicate)
INSERT INTO employees (email, first_name, salary)
VALUES ('alice@company.com', 'Alice', 75000.00)
ON CONFLICT (email)
DO NOTHING;
```

```
Flow diagram for ON CONFLICT:

INSERT INTO employees (email, salary) VALUES ('alice@co.com', 85000)
        │
        ▼
Does email 'alice@co.com' already exist?
        │
   ┌────┴────┐
   NO        YES
   │         │
   ▼         ▼
INSERT    ON CONFLICT clause:
 new row  DO NOTHING → skip
          DO UPDATE  → modify existing row
```

### MySQL — INSERT ... ON DUPLICATE KEY UPDATE

```sql
INSERT INTO employees (email, first_name, last_name, salary)
VALUES ('alice@company.com', 'Alice', 'Smith', 85000.00)
ON DUPLICATE KEY UPDATE
    salary     = VALUES(salary),
    updated_at = NOW();
```

### MySQL — REPLACE INTO

Deletes the conflicting row then inserts the new one (resets auto-increment!):

```sql
REPLACE INTO employees (id, email, first_name, salary)
VALUES (1, 'alice@company.com', 'Alice', 85000.00);
-- ⚠️ Deletes old row first — loses any columns not specified
```

### MySQL — INSERT IGNORE

Silently skips insert if it would violate a unique constraint:

```sql
INSERT IGNORE INTO employees (email, first_name, salary)
VALUES ('alice@company.com', 'Alice', 75000.00);
-- If email exists → silently skip (no error, no update)
```

### SQL Server — MERGE

Full ANSI MERGE syntax (also supported in Oracle):

```sql
MERGE INTO employees AS target
USING (
    SELECT 'alice@company.com' AS email,
           'Alice'             AS first_name,
           85000.00            AS salary
) AS source
ON target.email = source.email

WHEN MATCHED THEN
    UPDATE SET
        target.salary     = source.salary,
        target.updated_at = GETDATE()

WHEN NOT MATCHED THEN
    INSERT (email, first_name, salary)
    VALUES (source.email, source.first_name, source.salary);
```

---

## 🗺️ DML Safety Checklist

```
Before running UPDATE or DELETE:
──────────────────────────────────────────────────────────
1. Run a SELECT first with the same WHERE clause:
   SELECT * FROM employees WHERE dept_id = 10;
   (make sure you're targeting the right rows)

2. Wrap in a transaction to review before committing:
   BEGIN;
   DELETE FROM employees WHERE dept_id = 10;
   -- check: SELECT count(*) FROM employees;
   ROLLBACK;   -- or COMMIT if satisfied

3. Use RETURNING to see what was affected (PostgreSQL):
   DELETE FROM employees WHERE dept_id = 10 RETURNING *;

4. Test in a staging environment first.
```

---

## ⚠️ Common Mistakes

```sql
-- ❌ Forgetting WHERE on UPDATE/DELETE
UPDATE employees SET salary = 0;     -- zeros out EVERYONE's salary!
DELETE FROM orders;                  -- deletes ALL orders!

-- ✅ Always verify your WHERE clause first
SELECT * FROM employees WHERE dept_id = 99;  -- 0 rows? That's fine
UPDATE employees SET salary = 0 WHERE dept_id = 99;  -- safe
```

---

## 🔑 Key Takeaways

| Statement | Key Points |
|-----------|-----------|
| `INSERT INTO ... VALUES` | Single/multi row, explicit columns |
| `INSERT INTO ... SELECT` | Bulk insert from query |
| `ON CONFLICT DO UPDATE` | PostgreSQL upsert |
| `ON DUPLICATE KEY UPDATE` | MySQL upsert |
| `UPDATE ... SET ... WHERE` | Always use WHERE! |
| `UPDATE ... FROM` | PostgreSQL join-update |
| `DELETE FROM ... WHERE` | Always use WHERE! |
| `MERGE` | Full ANSI upsert (SQL Server/Oracle) |
| `RETURNING` | Get affected rows back (PostgreSQL) |

---

**← Previous:** [05 — Database Objects (DDL)](./05-database-objects-ddl.md)
**Next →** [07 — DQL: SELECT & Filtering](./07-dql-select.md)
