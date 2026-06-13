# 11 — Aggregate Functions & Grouping

> **Goal:** Compute summary statistics across groups of rows using aggregate functions and GROUP BY.

---

## 📖 What are Aggregate Functions?

**Aggregate functions** operate on a **set of rows** and return a **single value**.

```
Without aggregate:            With aggregate (COUNT):
──────────────────            ────────────────────────
SELECT salary                 SELECT COUNT(*) FROM employees;
FROM employees;               ┌─────────┐
                              │ count   │
┌────────┐                    ├─────────┤
│ salary │                    │    8    │
├────────┤                    └─────────┘
│  90000 │
│  72000 │  → 8 rows → 1 result
│  85000 │
│  65000 │
│  78000 │
│  95000 │
│  NULL  │
│  70000 │
└────────┘
```

---

## 📊 Core Aggregate Functions

### COUNT

```sql
-- Count all rows (including NULLs)
SELECT COUNT(*) FROM employees;             -- 8

-- Count non-NULL values in a column
SELECT COUNT(salary) FROM employees;        -- 7 (Grace's salary is NULL)

-- Count distinct values
SELECT COUNT(DISTINCT dept) FROM employees; -- 3 (Engineering, Marketing, HR)

-- Conditional count (PostgreSQL / MySQL)
SELECT COUNT(*) FILTER (WHERE dept = 'Engineering') AS eng_count FROM employees;

-- CASE-based conditional count (all RDBMS)
SELECT
    COUNT(CASE WHEN dept = 'Engineering' THEN 1 END) AS engineers,
    COUNT(CASE WHEN dept = 'HR'          THEN 1 END) AS hr_staff,
    COUNT(CASE WHEN salary > 80000       THEN 1 END) AS high_earners
FROM employees;
```

### SUM

```sql
SELECT SUM(salary) FROM employees;          -- 555000 (NULLs ignored)
SELECT SUM(qty * unit_price) AS revenue FROM order_items;

-- SUM with condition
SELECT SUM(salary) FILTER (WHERE dept = 'Engineering') FROM employees;
```

### AVG

```sql
SELECT AVG(salary) FROM employees;          -- 79285.71... (NULLs ignored)
-- Note: 555000 / 7 (not /8 — NULL is excluded from count)

-- Round the result
SELECT ROUND(AVG(salary), 2) AS avg_salary FROM employees;

-- Compare individual to average
SELECT name, salary, AVG(salary) OVER () AS company_avg FROM employees;
-- (See Window Functions chapter for OVER())
```

### MIN / MAX

```sql
SELECT MIN(salary) FROM employees;          -- 65000 (David)
SELECT MAX(salary) FROM employees;          -- 95000 (Frank)
SELECT MIN(hire_date) FROM employees;       -- 2018-09-01 (Frank - earliest)
SELECT MAX(hire_date) FROM employees;       -- 2023-05-15 (Hank - latest)

-- Min/Max of text (alphabetical)
SELECT MIN(name), MAX(name) FROM employees;  -- Alice, Hank
```

### NULL Behavior in Aggregates

```sql
-- All aggregates (except COUNT(*)) IGNORE NULLs
SELECT AVG(salary) FROM employees;
-- Grace has NULL salary — she is EXCLUDED from both sum and count
-- = (90000 + 72000 + 85000 + 65000 + 78000 + 95000 + 70000) / 7
-- = 555000 / 7 = 79285.71

-- To treat NULL as 0:
SELECT AVG(COALESCE(salary, 0)) FROM employees;
-- = 555000 / 8 = 69375.00
```

---

## 📦 GROUP BY

Groups rows with the same value(s) into summary rows.

```
Without GROUP BY:          With GROUP BY dept:
SELECT dept, COUNT(*)      SELECT dept, COUNT(*) FROM employees GROUP BY dept;
                           ┌──────────────┬────────┐
All 8 rows → error:        │ dept         │ count  │
can't mix non-agg col      ├──────────────┼────────┤
with aggregate without     │ Engineering  │   4    │
GROUP BY                   │ HR           │   2    │
                           │ Marketing    │   2    │
                           └──────────────┴────────┘
```

```sql
-- Count employees per department
SELECT dept, COUNT(*) AS headcount
FROM employees
GROUP BY dept
ORDER BY headcount DESC;

-- Average salary per department
SELECT
    dept,
    COUNT(*)                       AS headcount,
    ROUND(AVG(salary), 2)         AS avg_salary,
    MIN(salary)                    AS min_salary,
    MAX(salary)                    AS max_salary,
    SUM(salary)                    AS total_payroll
FROM employees
GROUP BY dept
ORDER BY avg_salary DESC;

-- Group by multiple columns
SELECT
    dept,
    EXTRACT(YEAR FROM hire_date) AS hire_year,
    COUNT(*)                     AS count
FROM employees
GROUP BY dept, hire_year
ORDER BY dept, hire_year;
```

### Rules for GROUP BY

```
✅ In SELECT, you can only have:
   1. Columns listed in GROUP BY
   2. Aggregate functions (COUNT, SUM, AVG, MIN, MAX)

❌ This is WRONG:
SELECT dept, name, COUNT(*)     -- 'name' not in GROUP BY!
FROM employees
GROUP BY dept;

✅ This is RIGHT:
SELECT dept, COUNT(*), MIN(name) -- use aggregate on 'name'
FROM employees
GROUP BY dept;
```

---

## 🔍 HAVING

`HAVING` filters **groups** (after GROUP BY). Think of it as `WHERE` for aggregates.

```
WHERE  → filters individual rows BEFORE grouping
HAVING → filters groups AFTER aggregating
```

```sql
-- Departments with more than 2 employees
SELECT dept, COUNT(*) AS headcount
FROM employees
GROUP BY dept
HAVING COUNT(*) > 2;
-- Engineering: 4  ✅
-- HR: 2          ❌ (not > 2)
-- Marketing: 2   ❌

-- Departments with average salary > 75000
SELECT dept, ROUND(AVG(salary), 2) AS avg_salary
FROM employees
GROUP BY dept
HAVING AVG(salary) > 75000;
-- Engineering: 85000 ✅
-- Marketing: 75000   ❌ (not > 75000, it's equal)

-- WHERE + HAVING together
SELECT dept, COUNT(*) AS active_count, AVG(salary) AS avg_salary
FROM employees
WHERE is_active = TRUE          -- filter rows first
GROUP BY dept
HAVING AVG(salary) > 70000     -- then filter groups
ORDER BY avg_salary DESC;
```

### WHERE vs HAVING

```sql
-- ❌ WRONG: cannot use aggregate in WHERE
SELECT dept, COUNT(*)
FROM employees
WHERE COUNT(*) > 2           -- ❌ error!
GROUP BY dept;

-- ✅ CORRECT: use HAVING
SELECT dept, COUNT(*)
FROM employees
GROUP BY dept
HAVING COUNT(*) > 2;         -- ✅
```

---

## 🔢 Advanced Grouping

### ROLLUP — Hierarchical Subtotals

Generates subtotals at each level of a hierarchy, plus a grand total.

```sql
SELECT
    dept,
    EXTRACT(YEAR FROM hire_date) AS year,
    COUNT(*)                     AS count
FROM employees
GROUP BY ROLLUP(dept, year)
ORDER BY dept NULLS LAST, year NULLS LAST;

/*
dept          | year | count
──────────────┼──────┼──────
Engineering   | 2018 |   1
Engineering   | 2020 |   1
Engineering   | 2021 |   1
Engineering   | 2023 |   1
Engineering   | NULL |   4   ← subtotal for Engineering
HR            | 2022 |   1
HR            | 2023 |   1
HR            | NULL |   2   ← subtotal for HR
Marketing     | 2019 |   1
Marketing     | 2020 |   1
Marketing     | NULL |   2   ← subtotal for Marketing
NULL          | NULL |   8   ← grand total
*/
```

### CUBE — All Possible Subtotals

Generates subtotals for **all combinations** of the grouped columns.

```sql
SELECT
    dept,
    EXTRACT(YEAR FROM hire_date) AS year,
    COUNT(*)
FROM employees
GROUP BY CUBE(dept, year)
ORDER BY dept NULLS LAST, year NULLS LAST;

-- Produces subtotals for:
-- (dept, year)  → each department per year
-- (dept, NULL)  → each department total
-- (NULL, year)  → each year total (all depts)
-- (NULL, NULL)  → grand total
```

### GROUPING SETS — Explicit Control

Define exactly which combinations to aggregate.

```sql
SELECT
    dept,
    EXTRACT(YEAR FROM hire_date) AS year,
    COUNT(*)
FROM employees
GROUP BY GROUPING SETS (
    (dept, year),    -- group by dept + year
    (dept),          -- group by dept only
    ()               -- grand total
);
-- Equivalent to ROLLUP(dept, year) in this case
```

### GROUPING() Function

Identifies whether a column is NULL due to ROLLUP/CUBE (vs actual NULL data).

```sql
SELECT
    CASE WHEN GROUPING(dept) = 1 THEN 'ALL DEPTS' ELSE dept END AS dept,
    CASE WHEN GROUPING(EXTRACT(YEAR FROM hire_date)) = 1 THEN 'ALL YEARS'
         ELSE CAST(EXTRACT(YEAR FROM hire_date) AS TEXT) END AS year,
    COUNT(*) AS count
FROM employees
GROUP BY ROLLUP(dept, EXTRACT(YEAR FROM hire_date));
```

---

## 🗺️ Full Query Flow with Aggregates

```
FROM employees             → 8 rows loaded
        │
        ▼
WHERE is_active = TRUE     → filter to 7 rows (Grace excluded if inactive)
        │
        ▼
GROUP BY dept              → 3 groups: Engineering, HR, Marketing
        │
        ▼
Compute aggregates:
  COUNT(*): 4, 2, 2
  AVG(salary): 85000, NULL-excluded, 75000
        │
        ▼
HAVING AVG(salary) > 75000 → filter groups: only Engineering passes
        │
        ▼
SELECT dept, COUNT(*), AVG(salary)  → project columns
        │
        ▼
ORDER BY avg_salary DESC   → sort remaining groups
        │
        ▼
LIMIT 5                    → return top 5 (all 1 group here)
```

---

## ✅ Practice Examples

```sql
-- 1. Total revenue per product category
SELECT
    c.name AS category,
    SUM(oi.qty * oi.unit_price) AS total_revenue,
    COUNT(DISTINCT o.id)         AS total_orders
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN categories c ON p.category_id = c.id
JOIN orders o ON oi.order_id = o.id
GROUP BY c.name
ORDER BY total_revenue DESC;

-- 2. Customers who placed more than 5 orders
SELECT
    customer_id,
    COUNT(*) AS order_count
FROM orders
GROUP BY customer_id
HAVING COUNT(*) > 5
ORDER BY order_count DESC;

-- 3. Monthly revenue for 2024
SELECT
    TO_CHAR(created_at, 'YYYY-MM') AS month,
    ROUND(SUM(total), 2)           AS revenue,
    COUNT(*)                        AS orders
FROM orders
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01'
GROUP BY TO_CHAR(created_at, 'YYYY-MM')
ORDER BY month;

-- 4. Departments where no employee earns below 70000
SELECT dept
FROM employees
GROUP BY dept
HAVING MIN(salary) >= 70000;

-- 5. Find duplicate emails in users table
SELECT email, COUNT(*) AS occurrences
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
```

---

## 🔑 Key Takeaways

| Concept | Rule |
|---------|------|
| `COUNT(*)` | Counts all rows including NULLs |
| `COUNT(col)` | Counts non-NULL values only |
| `AVG`, `SUM`, `MIN`, `MAX` | Ignore NULL values |
| `GROUP BY` | SELECT must only contain grouped cols + aggregates |
| `WHERE` | Filters rows BEFORE grouping |
| `HAVING` | Filters groups AFTER aggregation |
| `ROLLUP` | Hierarchical subtotals + grand total |
| `CUBE` | All combinations of subtotals |
| `GROUPING SETS` | Manual control of grouping combinations |
| Division by zero | Use `NULLIF(count, 0)` in denominator |

---

**← Previous:** [10 — Mathematical Functions](./10-math-functions.md)
**Next →** [12 — Window Functions](./12-window-functions.md)
