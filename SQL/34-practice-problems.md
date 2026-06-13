# 34 — Must-Practice SQL Problems

> **Goal:** Build SQL fluency through structured practice from beginner to advanced.

Use these with the sample schema below, or on [DB Fiddle](https://www.db-fiddle.com/) / [SQLiteOnline](https://sqliteonline.com/).

---

## 🗄️ Practice Schema Setup

```sql
-- Run this to set up the practice environment
CREATE TABLE departments (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE employees (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    dept_id    INT REFERENCES departments(id),
    salary     DECIMAL(10,2),
    manager_id INT REFERENCES employees(id),
    hire_date  DATE NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE
);

CREATE TABLE products (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price    DECIMAL(10,2) NOT NULL,
    stock    INT DEFAULT 0
);

CREATE TABLE customers (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE,
    city       VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE orders (
    id          SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(id),
    total       DECIMAL(12,2) DEFAULT 0,
    status      VARCHAR(20) DEFAULT 'pending',
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    id         SERIAL PRIMARY KEY,
    order_id   INT REFERENCES orders(id),
    product_id INT REFERENCES products(id),
    qty        INT NOT NULL,
    price      DECIMAL(10,2) NOT NULL
);

-- Sample data
INSERT INTO departments VALUES (1,'Engineering'),(2,'Marketing'),(3,'HR'),(4,'Finance');

INSERT INTO employees (name,dept_id,salary,manager_id,hire_date) VALUES
('Alice',1,95000,NULL,'2018-01-15'),
('Bob',1,85000,1,'2019-03-01'),
('Carol',2,72000,1,'2020-06-15'),
('David',2,68000,3,'2021-02-01'),
('Eve',3,65000,1,'2019-11-20'),
('Frank',1,90000,1,'2020-08-10'),
('Grace',4,78000,1,'2021-05-05'),
('Hank',3,60000,5,'2022-01-10'),
('Ivy',1,88000,NULL,'2017-07-20'),
('Jack',2,71000,3,'2022-09-01');

INSERT INTO customers (name,email,city) VALUES
('Alice Smith','alice@email.com','New York'),
('Bob Jones','bob@email.com','London'),
('Carol Lee','carol@email.com','New York'),
('David Chen','david@email.com','Tokyo'),
('Eve Brown','eve@email.com','London');

INSERT INTO products (name,category,price,stock) VALUES
('Laptop','Electronics',999.99,50),
('Phone','Electronics',699.99,100),
('Desk','Furniture',299.99,30),
('Chair','Furniture',199.99,80),
('Headphones','Electronics',149.99,200),
('Monitor','Electronics',399.99,60),
('Keyboard','Electronics',79.99,150),
('Notebook','Stationery',4.99,500);

INSERT INTO orders (customer_id,total,status,created_at) VALUES
(1,999.99,'paid','2024-01-10'),(1,349.98,'paid','2024-02-15'),
(2,699.99,'shipped','2024-01-20'),(3,149.99,'paid','2024-03-01'),
(3,1099.98,'pending','2024-03-15'),(4,79.99,'cancelled','2024-02-01'),
(5,399.99,'paid','2024-01-25'),(1,199.99,'paid','2024-04-01'),
(2,479.98,'shipped','2024-04-10'),(4,4.99,'paid','2024-04-15');

INSERT INTO order_items (order_id,product_id,qty,price) VALUES
(1,1,1,999.99),(2,4,1,199.99),(2,7,2,79.99),(3,2,1,699.99),
(4,5,1,149.99),(5,1,1,999.99),(5,6,1,399.99),(6,7,1,79.99),
(7,6,1,399.99),(8,4,1,199.99),(9,2,1,699.99),(9,7,1,79.99),(10,8,1,4.99);
```

---

## 🟢 Beginner Problems

### B1. Basic SELECT
```sql
-- Get all employees with their name and salary, sorted by salary descending
SELECT name, salary FROM employees ORDER BY salary DESC;
```

### B2. Filtering
```sql
-- Find all employees hired after 2020-01-01 earning more than 70000
SELECT name, hire_date, salary
FROM employees
WHERE hire_date > '2020-01-01' AND salary > 70000;
```

### B3. Pattern Matching
```sql
-- Find customers whose name starts with 'A' or lives in 'London'
SELECT name, city FROM customers
WHERE name LIKE 'A%' OR city = 'London';
```

### B4. NULL Handling
```sql
-- Find employees with no manager (top-level employees)
SELECT name FROM employees WHERE manager_id IS NULL;
```

### B5. Aggregation
```sql
-- Count employees, find min/max/avg salary across the entire company
SELECT
    COUNT(*) AS total_employees,
    MIN(salary) AS min_salary,
    MAX(salary) AS max_salary,
    ROUND(AVG(salary), 2) AS avg_salary,
    SUM(salary) AS total_payroll
FROM employees;
```

### B6. GROUP BY
```sql
-- Count employees per department and average salary
SELECT
    d.name AS department,
    COUNT(e.id) AS headcount,
    ROUND(AVG(e.salary), 2) AS avg_salary
FROM departments d
LEFT JOIN employees e ON d.id = e.dept_id
GROUP BY d.id, d.name
ORDER BY headcount DESC;
```

### B7. HAVING
```sql
-- Departments with more than 2 employees and average salary > 75000
SELECT dept_id, COUNT(*) AS cnt, AVG(salary) AS avg_sal
FROM employees
GROUP BY dept_id
HAVING COUNT(*) > 2 AND AVG(salary) > 75000;
```

---

## 🟡 Intermediate Problems

### I1. JOIN Practice
```sql
-- Get all orders with customer name, total, and status
SELECT c.name AS customer, o.id AS order_id, o.total, o.status, o.created_at
FROM orders o
JOIN customers c ON o.customer_id = c.id
ORDER BY o.created_at;
```

### I2. Multiple JOINs
```sql
-- Get order details: customer, product, qty, line total
SELECT
    c.name AS customer,
    p.name AS product,
    oi.qty,
    oi.price AS unit_price,
    oi.qty * oi.price AS line_total
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
ORDER BY c.name, p.name;
```

### I3. LEFT JOIN — Find Missing Data
```sql
-- Find customers who have NEVER placed an order
SELECT c.name, c.email
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.id IS NULL;
```

### I4. Subquery — Above Average
```sql
-- Employees earning above the company average salary
SELECT name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees)
ORDER BY salary DESC;
```

### I5. Self JOIN — Employee Hierarchy
```sql
-- Show each employee and their manager's name
SELECT
    e.name AS employee,
    COALESCE(m.name, 'No Manager') AS manager,
    e.salary
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id
ORDER BY m.name NULLS FIRST, e.name;
```

### I6. Window Functions — Ranking
```sql
-- Rank employees by salary within each department
SELECT
    e.name,
    d.name AS dept,
    e.salary,
    RANK() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS dept_rank
FROM employees e
JOIN departments d ON e.dept_id = d.id;
```

### I7. Top N Per Group
```sql
-- Top 2 highest-paid employees per department
SELECT dept, name, salary
FROM (
    SELECT
        d.name AS dept,
        e.name,
        e.salary,
        ROW_NUMBER() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS rn
    FROM employees e
    JOIN departments d ON e.dept_id = d.id
) ranked
WHERE rn <= 2;
```

### I8. Running Total
```sql
-- Cumulative revenue by order date (ordered by date)
SELECT
    created_at::DATE AS order_date,
    total,
    SUM(total) OVER (ORDER BY created_at) AS running_total
FROM orders
WHERE status = 'paid'
ORDER BY created_at;
```

### I9. Month-over-Month Growth
```sql
-- Monthly revenue and growth %
WITH monthly AS (
    SELECT
        DATE_TRUNC('month', created_at) AS month,
        SUM(total) AS revenue
    FROM orders
    WHERE status IN ('paid','shipped')
    GROUP BY DATE_TRUNC('month', created_at)
)
SELECT
    month,
    revenue,
    LAG(revenue) OVER (ORDER BY month) AS prev_month,
    ROUND(100.0 * (revenue - LAG(revenue) OVER (ORDER BY month))
          / NULLIF(LAG(revenue) OVER (ORDER BY month), 0), 1) AS growth_pct
FROM monthly
ORDER BY month;
```

### I10. Find Duplicates
```sql
-- Find customers with duplicate email addresses
SELECT email, COUNT(*) AS occurrences
FROM customers
GROUP BY email
HAVING COUNT(*) > 1;
```

---

## 🔴 Advanced Problems

### A1. Recursive CTE — Full Hierarchy
```sql
-- Print full org chart with levels and reporting path
WITH RECURSIVE org AS (
    SELECT id, name, manager_id, 0 AS level, name::TEXT AS path
    FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.id, e.name, e.manager_id, o.level + 1,
           o.path || ' → ' || e.name
    FROM employees e
    JOIN org o ON e.manager_id = o.id
)
SELECT REPEAT('  ', level) || name AS org_chart, level, path
FROM org ORDER BY path;
```

### A2. Second Highest Salary (Classic)
```sql
-- Method 1: Offset
SELECT salary AS second_highest
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;

-- Method 2: Subquery
SELECT MAX(salary) AS second_highest
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- Method 3: DENSE_RANK
SELECT salary AS second_highest
FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS dr
    FROM employees
) t WHERE dr = 2 LIMIT 1;
```

### A3. Complex Reporting — Product Revenue Analysis
```sql
-- Revenue per product, % of total, rank
WITH product_revenue AS (
    SELECT
        p.name,
        p.category,
        SUM(oi.qty * oi.price) AS revenue
    FROM order_items oi
    JOIN products p ON oi.product_id = p.id
    JOIN orders o ON oi.order_id = o.id
    WHERE o.status IN ('paid','shipped')
    GROUP BY p.id, p.name, p.category
),
totals AS (
    SELECT SUM(revenue) AS grand_total FROM product_revenue
)
SELECT
    name,
    category,
    revenue,
    ROUND(100.0 * revenue / grand_total, 2) AS pct_of_total,
    RANK() OVER (ORDER BY revenue DESC) AS revenue_rank
FROM product_revenue, totals
ORDER BY revenue DESC;
```

### A4. Pivot Table with CASE
```sql
-- Orders count by customer per month (pivot)
SELECT
    c.name,
    COUNT(CASE WHEN EXTRACT(MONTH FROM o.created_at) = 1 THEN 1 END) AS jan,
    COUNT(CASE WHEN EXTRACT(MONTH FROM o.created_at) = 2 THEN 1 END) AS feb,
    COUNT(CASE WHEN EXTRACT(MONTH FROM o.created_at) = 3 THEN 1 END) AS mar,
    COUNT(CASE WHEN EXTRACT(MONTH FROM o.created_at) = 4 THEN 1 END) AS apr,
    COUNT(*) AS total
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.name
ORDER BY total DESC;
```

### A5. Customer Retention (Cohort)
```sql
-- Count customers who ordered in Jan AND Feb 2024
SELECT COUNT(DISTINCT customer_id) AS retained
FROM orders
WHERE customer_id IN (
    SELECT customer_id FROM orders
    WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01'
)
AND created_at >= '2024-02-01' AND created_at < '2024-03-01';
```

### A6. Gap Detection
```sql
-- Find gaps in sequential IDs (missing order IDs)
SELECT id + 1 AS missing_from,
       next_id - 1 AS missing_to
FROM (
    SELECT id, LEAD(id) OVER (ORDER BY id) AS next_id
    FROM orders
) gaps
WHERE next_id > id + 1;
```

### A7. Transaction with Error Handling
```sql
DO $$
BEGIN
    BEGIN;
    -- Transfer $200 from customer 1's credit to customer 2
    UPDATE customers SET credit = credit - 200 WHERE id = 1;
    UPDATE customers SET credit = credit + 200 WHERE id = 2;
    IF (SELECT credit FROM customers WHERE id = 1) < 0 THEN
        RAISE EXCEPTION 'Insufficient credit!';
    END IF;
    -- COMMIT implicit on DO $$ end
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error: %', SQLERRM;
        -- ROLLBACK implicit on exception
END;
$$;
```

---

## 🔑 Answer Key Patterns

| Problem Type | Technique |
|-------------|-----------|
| Top N per group | ROW_NUMBER() + WHERE rn <= N |
| Second highest | MAX() WHERE < MAX(), or OFFSET 1 |
| Month-over-Month | LAG() + NULLIF() for % change |
| Hierarchy/Tree | Recursive CTE |
| Pivot | CASE WHEN + aggregate |
| Missing records | LEFT JOIN WHERE right.id IS NULL |
| Duplicates | GROUP BY + HAVING COUNT(*) > 1 |
| Running total | SUM() OVER (ORDER BY ...) |
| Gap detection | LEAD() to find consecutive gaps |

---

**← Previous:** [33 — Advanced Topics](./33-advanced-topics.md)
**Next →** [35 — Interview Preparation](./35-interview-prep.md)
