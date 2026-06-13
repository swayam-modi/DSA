# 17 — CTEs & Derived Tables

> **Goal:** Use Common Table Expressions (CTEs) and derived tables to write readable, maintainable, and recursive queries.

---

## 📖 What is a CTE?

A **Common Table Expression (CTE)** is a **temporary named result set** defined at the beginning of a query using `WITH`. It acts like a temporary view scoped to that single query.

```sql
WITH cte_name AS (
    -- the CTE query
    SELECT ...
)
-- main query uses the CTE
SELECT * FROM cte_name;
```

**Benefits over subqueries:**
- More readable (defined before use, like top-down code)
- Can be referenced **multiple times** in the main query
- Can be **recursive** (not possible with subqueries)

---

## 🔧 Simple CTE

```sql
-- Without CTE (subquery in FROM)
SELECT dept, avg_salary
FROM (
    SELECT dept, AVG(salary) AS avg_salary
    FROM employees
    WHERE is_active = TRUE
    GROUP BY dept
) AS dept_stats
WHERE avg_salary > 75000;

-- With CTE (same result, more readable)
WITH dept_stats AS (
    SELECT dept, AVG(salary) AS avg_salary
    FROM employees
    WHERE is_active = TRUE
    GROUP BY dept
)
SELECT dept, avg_salary
FROM dept_stats
WHERE avg_salary > 75000;
```

---

## 🔗 Multiple CTEs

Chain multiple CTEs separated by commas.

```sql
WITH
-- CTE 1: calculate department averages
dept_averages AS (
    SELECT
        dept,
        ROUND(AVG(salary), 2) AS avg_salary,
        COUNT(*)              AS headcount
    FROM employees
    WHERE is_active = TRUE
    GROUP BY dept
),
-- CTE 2: find high-paying departments (avg > company avg)
company_avg AS (
    SELECT AVG(salary) AS overall_avg FROM employees WHERE is_active = TRUE
),
-- CTE 3: identify high-paying depts
high_paying_depts AS (
    SELECT d.dept, d.avg_salary, d.headcount
    FROM dept_averages d
    CROSS JOIN company_avg c
    WHERE d.avg_salary > c.overall_avg
)
-- Final query: employees in high-paying departments
SELECT e.name, e.dept, e.salary, h.avg_salary AS dept_avg
FROM employees e
JOIN high_paying_depts h ON e.dept = h.dept
WHERE e.is_active = TRUE
ORDER BY e.dept, e.salary DESC;
```

---

## 🔁 Recursive CTE

A recursive CTE **calls itself** to traverse hierarchical or graph-like data (org charts, file systems, category trees, etc.)

### Structure

```sql
WITH RECURSIVE cte_name AS (
    -- Base case: starting point (non-recursive)
    SELECT ...
    UNION ALL
    -- Recursive case: references cte_name
    SELECT ... FROM ... JOIN cte_name ON ...
)
SELECT * FROM cte_name;
```

### Example 1: Employee Hierarchy (Org Chart)

```sql
-- employees table: id, name, manager_id (self-referencing)

WITH RECURSIVE org_chart AS (
    -- Base case: find the top-level employee (no manager)
    SELECT id, name, manager_id, 0 AS level, name AS path
    FROM employees
    WHERE manager_id IS NULL   -- Alice is the CEO

    UNION ALL

    -- Recursive case: find each employee's reports
    SELECT
        e.id,
        e.name,
        e.manager_id,
        oc.level + 1                   AS level,
        oc.path || ' → ' || e.name     AS path  -- build org path
    FROM employees e
    INNER JOIN org_chart oc ON e.manager_id = oc.id
)
SELECT
    REPEAT('  ', level) || name AS indented_name,  -- indent by level
    level,
    path
FROM org_chart
ORDER BY path;

/*
Result:
indented_name   | level | path
────────────────┼───────┼──────────────────────
Alice           │   0   │ Alice
  Bob           │   1   │ Alice → Bob
    David       │   2   │ Alice → Bob → David
      Grace     │   3   │ Alice → Bob → David → Grace
    Eve         │   2   │ Alice → Bob → Eve
  Carol         │   1   │ Alice → Carol
  Frank         │   1   │ Alice → Frank
    Hank        │   2   │ Alice → Frank → Hank
*/
```

### Example 2: Number Series Generator

```sql
-- Generate numbers 1 to 10 (no table needed)
WITH RECURSIVE num_series AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM num_series WHERE n < 10
)
SELECT n FROM num_series;
-- 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
```

### Example 3: Date Series

```sql
-- Generate all dates in January 2025
WITH RECURSIVE date_series AS (
    SELECT '2025-01-01'::DATE AS d
    UNION ALL
    SELECT d + INTERVAL '1 day' FROM date_series WHERE d < '2025-01-31'
)
SELECT d FROM date_series;

-- Practical: show revenue for every day (even days with $0)
WITH RECURSIVE date_series AS (
    SELECT '2025-01-01'::DATE AS d
    UNION ALL
    SELECT d + 1 FROM date_series WHERE d < '2025-01-31'
)
SELECT
    ds.d AS date,
    COALESCE(SUM(o.total), 0) AS revenue
FROM date_series ds
LEFT JOIN orders o ON o.created_at::DATE = ds.d
GROUP BY ds.d
ORDER BY ds.d;
```

### Example 4: Category Tree (E-commerce)

```sql
-- categories: id, name, parent_id (can be nested)
WITH RECURSIVE category_tree AS (
    -- Root categories (no parent)
    SELECT id, name, parent_id, 0 AS depth, name AS breadcrumb
    FROM categories
    WHERE parent_id IS NULL

    UNION ALL

    SELECT
        c.id,
        c.name,
        c.parent_id,
        ct.depth + 1,
        ct.breadcrumb || ' > ' || c.name
    FROM categories c
    JOIN category_tree ct ON c.parent_id = ct.id
)
SELECT
    id,
    REPEAT('—', depth) || ' ' || name AS category_name,
    breadcrumb
FROM category_tree
ORDER BY breadcrumb;

/*
category_name              | breadcrumb
───────────────────────────┼───────────────────────────────────
 Electronics               | Electronics
— Computers                | Electronics > Computers
—— Laptops                 | Electronics > Computers > Laptops
—— Desktops                | Electronics > Computers > Desktops
— Phones                   | Electronics > Phones
 Clothing                  | Clothing
*/
```

### Prevent Infinite Recursion

Always include a **termination condition**:

```sql
-- ✅ Safe: WHERE clause stops recursion
UNION ALL
SELECT n + 1 FROM num_series WHERE n < 1000   -- stops at 1000

-- PostgreSQL LIMIT: also works as safety net
-- Set max recursion depth (PostgreSQL):
SET max_recursive_iterations = 1000;   -- default is safe

-- Cycle detection (PostgreSQL 14+):
WITH RECURSIVE path AS (
    SELECT id, ARRAY[id] AS visited
    UNION ALL
    SELECT g.to_id, visited || g.to_id
    FROM graph g
    JOIN path p ON g.from_id = p.id
    WHERE g.to_id <> ALL(visited)   -- avoid cycles!
)
SELECT * FROM path;
```

---

## 📋 Derived Tables

A **derived table** is a subquery used directly in the `FROM` clause without a name defined with `WITH`. It's an **inline view**.

```sql
-- Derived table (anonymous subquery in FROM)
SELECT dept_summary.dept, dept_summary.avg_salary
FROM (
    SELECT dept, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY dept
) AS dept_summary            -- ← must have alias!
WHERE dept_summary.avg_salary > 75000;
```

### CTE vs Derived Table

```
Derived Table:                         CTE (WITH):
─────────────────────────────────────  ─────────────────────────────────────
SELECT * FROM                          WITH dept_avg AS (
  (SELECT dept, AVG(salary) AS avg       SELECT dept, AVG(salary) AS avg
   FROM employees GROUP BY dept) t       FROM employees GROUP BY dept
WHERE t.avg > 75000;                   )
                                       SELECT * FROM dept_avg
                                       WHERE avg > 75000;

Inline — defined at point of use       Defined at top — cleaner
Hard to reuse                          Can be referenced multiple times
No recursion                           Can be recursive
Works in all SQL databases             Requires CTE support (all modern RDBMS)
```

---

## 🔑 Key Takeaways

| Feature | Simple CTE | Multiple CTE | Recursive CTE |
|---------|-----------|-------------|--------------|
| Readability | ✅ High | ✅✅ High | ✅ Good |
| Reusable in query | ✅ Yes | ✅ Yes | ✅ Yes |
| Recursive | ❌ No | ❌ No | ✅ Yes |
| Hierarchical data | ❌ No | ❌ No | ✅ Yes |
| Multiple levels | N/A | ✅ Yes | ✅ Yes |

**When to use CTEs:**
- Complex multi-step queries (break into readable pieces)
- When the same subquery is needed multiple times
- Hierarchical/recursive data (org charts, trees, graphs)
- Replacing deeply nested subqueries

**Recursive CTE pattern:**
1. **Base case** — starting rows (anchor)
2. `UNION ALL`
3. **Recursive case** — join CTE to itself, advance one level
4. **Termination** — WHERE clause stops recursion

---

**← Previous:** [16 — Set Operators](./16-set-operators.md)
**Next →** [18 — Indexing](./18-indexing.md)
