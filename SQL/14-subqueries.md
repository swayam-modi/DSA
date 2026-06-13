# 14 — Subqueries

> **Goal:** Use queries nested inside other queries to solve complex data retrieval problems.

---

## 📖 What is a Subquery?

A **subquery** (also called a **nested query** or **inner query**) is a SQL query embedded inside another SQL query.

```sql
-- Find employees earning above average salary
SELECT name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
--              └─────── subquery ───────────────┘
```

Subqueries can appear in:
- `WHERE` clause
- `FROM` clause (derived table)
- `SELECT` clause (scalar subquery)
- `HAVING` clause

---

## 1️⃣ Scalar Subquery

Returns **exactly one row and one column** (a single value). Used anywhere a single value is expected.

```sql
-- Employees earning above the average
SELECT name, salary,
       (SELECT AVG(salary) FROM employees) AS company_avg
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);

-- Each product's price vs max price in category
SELECT
    p.name,
    p.price,
    (SELECT MAX(price) FROM products p2 WHERE p2.category_id = p.category_id) AS category_max
FROM products p;

-- Order count for a specific customer
SELECT
    c.name,
    (SELECT COUNT(*) FROM orders o WHERE o.customer_id = c.id) AS order_count
FROM customers c;
```

> ⚠️ A scalar subquery that returns more than one row causes an error.

---

## 2️⃣ Row Subquery

Returns one row with multiple columns.

```sql
-- Find the employee with the exact same name and department as employee #1
SELECT * FROM employees
WHERE (name, dept) = (SELECT name, dept FROM employees WHERE id = 1);
```

---

## 3️⃣ Table Subquery (Derived Table)

Returns multiple rows and columns. Used in `FROM` clause.

```sql
-- Average salary per department, then average of those averages
SELECT AVG(dept_avg) AS avg_of_avgs
FROM (
    SELECT dept, AVG(salary) AS dept_avg
    FROM employees
    GROUP BY dept
) AS dept_stats;     -- ← must have an alias!

-- Top 3 products by revenue, with their category
SELECT p.name, p.category, p.revenue
FROM (
    SELECT
        p.id, p.name, c.name AS category,
        SUM(oi.qty * oi.unit_price) AS revenue
    FROM products p
    JOIN categories c ON p.category_id = c.id
    JOIN order_items oi ON p.id = oi.product_id
    GROUP BY p.id, p.name, c.name
) AS p
ORDER BY p.revenue DESC
LIMIT 3;
```

---

## 4️⃣ Correlated Subquery

A subquery that **references columns from the outer query**. Runs once per outer row.

```sql
-- Find employees earning more than their department average
SELECT name, dept, salary
FROM employees e_outer
WHERE salary > (
    SELECT AVG(salary)
    FROM employees e_inner
    WHERE e_inner.dept = e_outer.dept   -- ← references outer query!
);

/*
Execution:
For each employee in outer query:
  1. Run inner query with THAT employee's dept
  2. Compare salary to department average
  3. Include if salary > dept avg

Alice   (Engineering): Engineering avg = 85000 → 90000 > 85000 ✅
Carol   (Engineering): 85000 > 85000 ❌ (not strictly greater)
Frank   (Engineering): 95000 > 85000 ✅
Bob     (Marketing): Marketing avg = 75000 → 72000 > 75000 ❌
Eve     (Marketing): 78000 > 75000 ✅
David   (HR): HR avg = 65000 → 65000 > 65000 ❌
*/
```

> ⚠️ Correlated subqueries run **once per outer row** — can be slow on large datasets. Consider rewriting with JOINs or window functions.

```sql
-- Same result using window function (often faster):
SELECT name, dept, salary
FROM (
    SELECT name, dept, salary,
           AVG(salary) OVER (PARTITION BY dept) AS dept_avg
    FROM employees
) t
WHERE salary > dept_avg;
```

---

## 5️⃣ Nested Subquery

Multiple levels of nesting.

```sql
-- Find customers who ordered the most expensive product in each category
SELECT c.name
FROM customers c
WHERE c.id IN (
    SELECT o.customer_id
    FROM orders o
    JOIN order_items oi ON o.id = oi.order_id
    WHERE oi.product_id IN (
        SELECT p.id
        FROM products p
        WHERE p.price = (
            SELECT MAX(price)
            FROM products p2
            WHERE p2.category_id = p.category_id
        )
    )
);
```

---

## 🔍 Subquery Operators

### EXISTS / NOT EXISTS

Tests whether the subquery **returns any rows**. More efficient than `IN` when the subquery is large.

```sql
-- Customers who have placed at least one order (EXISTS)
SELECT c.name
FROM customers c
WHERE EXISTS (
    SELECT 1                          -- SELECT 1 is conventional (value doesn't matter)
    FROM orders o
    WHERE o.customer_id = c.id
);

-- Customers who have NEVER ordered (NOT EXISTS)
SELECT c.name
FROM customers c
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.customer_id = c.id
);
```

```
EXISTS vs IN:
──────────────────────────────────────────────────────────────
EXISTS stops scanning as soon as it finds ONE matching row.
IN evaluates the full subquery first, then checks membership.

When subquery is large or correlated → EXISTS is usually faster.
When subquery is small/static list → IN is simpler and fine.
```

### IN / NOT IN with Subquery

```sql
-- Orders for customers in Engineering dept
SELECT o.id, o.total
FROM orders o
WHERE o.customer_id IN (
    SELECT id FROM customers WHERE dept = 'Engineering'
);

-- ⚠️ NOT IN with NULLs is a known trap!
SELECT name FROM employees
WHERE id NOT IN (
    SELECT manager_id FROM employees  -- if ANY manager_id is NULL...
);
-- Returns ZERO rows if subquery contains NULL!
-- Use NOT EXISTS instead:
SELECT name FROM employees e
WHERE NOT EXISTS (
    SELECT 1 FROM employees m WHERE m.manager_id = e.id
);
```

### ANY / SOME

Returns TRUE if the condition is true for **at least one** value in the subquery.

```sql
-- Employees earning more than ANY HR employee
SELECT name, salary
FROM employees
WHERE salary > ANY (
    SELECT salary FROM employees WHERE dept = 'HR'
);
-- salary > 65000 (lowest in HR) → effectively same as > MIN(HR salary)

-- ✅ Equivalent:
WHERE salary > (SELECT MIN(salary) FROM employees WHERE dept = 'HR')
```

### ALL

Returns TRUE if the condition is true for **all** values in the subquery.

```sql
-- Employees earning more than ALL Marketing employees
SELECT name, salary
FROM employees
WHERE salary > ALL (
    SELECT salary FROM employees WHERE dept = 'Marketing'
);
-- salary > 78000 (highest in Marketing) → effectively same as > MAX(marketing)

-- ✅ Equivalent:
WHERE salary > (SELECT MAX(salary) FROM employees WHERE dept = 'Marketing')
```

---

## 🗺️ Subquery vs JOIN vs CTE

```
Same question: "Names of customers who ordered product #5"

Subquery:                            JOIN:
SELECT c.name                        SELECT DISTINCT c.name
FROM customers c                     FROM customers c
WHERE c.id IN (                      JOIN orders o ON c.id = o.customer_id
    SELECT o.customer_id             JOIN order_items oi ON o.id = oi.order_id
    FROM orders o                    WHERE oi.product_id = 5;
    JOIN order_items oi
      ON o.id = oi.order_id
    WHERE oi.product_id = 5
);

CTE (most readable for complex):
WITH product5_customers AS (
    SELECT DISTINCT o.customer_id
    FROM orders o
    JOIN order_items oi ON o.id = oi.order_id
    WHERE oi.product_id = 5
)
SELECT c.name
FROM customers c
JOIN product5_customers p ON c.id = p.customer_id;
```

**When to use which:**
- **Subquery in WHERE:** Simple filtering using a list or condition
- **Derived table in FROM:** Need to treat query result as a table
- **EXISTS:** Performance-sensitive existence checks
- **CTE:** Multi-step logic, readability (covered in Chapter 17)
- **JOIN:** When you need columns from both tables

---

## ✅ Practice Examples

```sql
-- 1. Products never ordered
SELECT p.name
FROM products p
WHERE p.id NOT IN (SELECT product_id FROM order_items);
-- ⚠️ Use NOT EXISTS if product_id could be NULL in order_items

-- 2. Second highest salary (classic interview question)
SELECT MAX(salary) AS second_highest
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- Better: using LIMIT/OFFSET
SELECT salary AS second_highest
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;

-- 3. Departments with above-average headcount
SELECT dept, COUNT(*) AS headcount
FROM employees
GROUP BY dept
HAVING COUNT(*) > (SELECT AVG(cnt) FROM (
    SELECT COUNT(*) AS cnt FROM employees GROUP BY dept
) AS dept_counts);

-- 4. Customers with more orders than average customer
SELECT c.name, COUNT(o.id) AS order_count
FROM customers c
JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.name
HAVING COUNT(o.id) > (
    SELECT AVG(cnt) FROM (
        SELECT COUNT(*) AS cnt FROM orders GROUP BY customer_id
    ) AS avg_orders
);
```

---

## 🔑 Key Takeaways

| Subquery Type | Where Used | Returns | Correlated? |
|---------------|-----------|---------|-------------|
| Scalar | SELECT, WHERE, HAVING | 1 row, 1 col | Optional |
| Row | WHERE (row comparison) | 1 row, n cols | Optional |
| Derived table | FROM | Many rows | No |
| Correlated | WHERE, SELECT, HAVING | Varies | Yes |
| EXISTS | WHERE | True/False | Usually |

**Key Rules:**
- Derived tables **must** have an alias
- `NOT IN` with NULL → unpredictable results → use `NOT EXISTS`
- Correlated subqueries run per outer row → can be slow
- `EXISTS` short-circuits on first match → usually faster than `IN`
- `> ANY(subquery)` = `> MIN(subquery)`
- `> ALL(subquery)` = `> MAX(subquery)`

---

**← Previous:** [13 — Joins](./13-joins.md)
**Next →** [15 — Views](./15-views.md)
