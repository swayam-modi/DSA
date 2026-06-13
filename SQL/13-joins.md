# 13 — Joins

> **Goal:** Combine data from multiple tables using different types of JOINs.

---

## 📖 What is a JOIN?

A JOIN combines rows from two or more tables based on a **related column** (usually a FK-PK relationship).

```
Without JOIN — only one table:        With JOIN — combined data:
SELECT * FROM orders;                 SELECT o.id, c.name, o.total
                                      FROM orders o
┌────┬─────────────┬──────┐           JOIN customers c ON o.customer_id = c.id;
│ id │ customer_id │ total│
├────┼─────────────┼──────┤           ┌────┬───────┬──────┐
│  1 │      1      │  500 │           │ id │ name  │ total│
│  2 │      2      │  300 │           ├────┼───────┼──────┤
│  3 │      1      │  750 │           │  1 │ Alice │  500 │
└────┴─────────────┴──────┘           │  2 │ Bob   │  300 │
                                       │  3 │ Alice │  750 │
customer_id 1 = Alice?                 └────┴───────┴──────┘
customer_id 2 = Bob? → JOIN!
```

### Sample Data

```sql
-- customers
┌────┬───────┬───────────────────┐
│ id │ name  │ email             │
├────┼───────┼───────────────────┤
│  1 │ Alice │ alice@email.com   │
│  2 │ Bob   │ bob@email.com     │
│  3 │ Carol │ carol@email.com   │
│  4 │ David │ david@email.com   │
└────┴───────┴───────────────────┘

-- orders
┌────┬─────────────┬───────┐
│ id │ customer_id │ total │
├────┼─────────────┼───────┤
│  1 │      1      │  500  │
│  2 │      2      │  300  │
│  3 │      1      │  750  │
│  4 │      5      │  200  │  ← customer_id=5 doesn't exist!
└────┴─────────────┴───────┘
```

---

## 🔵 INNER JOIN

Returns only rows where there is a **match in BOTH tables**.

```
customers    ∩    orders
   ●──────────────●
   (only matching)
```

```
Venn Diagram:
┌────────────────────────────┐
│ customers  ┌───────┐orders │
│            │ INNER │       │
│            │  JOIN │       │
│            └───────┘       │
└────────────────────────────┘
```

```sql
SELECT c.name, o.id AS order_id, o.total
FROM customers c
INNER JOIN orders o ON c.id = o.customer_id;
-- OR: JOIN orders o ON c.id = o.customer_id  (INNER is default)

/*
name  | order_id | total
──────┼──────────┼──────
Alice │     1    │  500
Bob   │     2    │  300
Alice │     3    │  750
            ↑
Carol and David have no orders → excluded
Order 4 (customer_id=5) → no matching customer → excluded
*/
```

**Use:** Get only rows with matching data in both tables.

---

## 🟡 LEFT JOIN (LEFT OUTER JOIN)

Returns **all rows from the LEFT table**, plus matching rows from the right. Non-matching right rows become NULL.

```
ALL of customers + matching orders
   ●────────────●
   ●────────────
   ●────────────        (no match → NULLs on right)
```

```sql
SELECT c.name, o.id AS order_id, o.total
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id;

/*
name  | order_id | total
──────┼──────────┼──────
Alice │     1    │  500
Alice │     3    │  750
Bob   │     2    │  300
Carol │   NULL   │ NULL   ← Carol has no orders (NULLs)
David │   NULL   │ NULL   ← David has no orders (NULLs)

Note: Order 4 (customer_id=5) → STILL excluded (no left-side match)
*/
```

**Use:** Get all customers, with their orders if they have any (customer list with order counts).

```sql
-- Find customers with NO orders (anti-join pattern)
SELECT c.name
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.id IS NULL;
-- Carol, David
```

---

## 🟠 RIGHT JOIN (RIGHT OUTER JOIN)

Returns **all rows from the RIGHT table**, plus matching left rows. Non-matching left rows become NULL.

```sql
SELECT c.name, o.id AS order_id, o.total
FROM customers c
RIGHT JOIN orders o ON c.id = o.customer_id;

/*
name  | order_id | total
──────┼──────────┼──────
Alice │     1    │  500
Bob   │     2    │  300
Alice │     3    │  750
NULL  │     4    │  200   ← order with no customer (NULLs on left)
*/
```

> 💡 RIGHT JOIN is rarely used — you can always rewrite it as a LEFT JOIN by switching table order. Most people prefer LEFT JOIN for clarity.

```sql
-- Equivalent LEFT JOIN:
SELECT c.name, o.id AS order_id, o.total
FROM orders o
LEFT JOIN customers c ON c.id = o.customer_id;
```

---

## 🔴 FULL OUTER JOIN

Returns **all rows from BOTH tables**. Non-matching sides become NULL.

```
ALL customers + ALL orders (with NULLs for non-matches)
```

```sql
SELECT c.name, o.id AS order_id, o.total
FROM customers c
FULL OUTER JOIN orders o ON c.id = o.customer_id;

/*
name  | order_id | total
──────┼──────────┼──────
Alice │     1    │  500
Alice │     3    │  750
Bob   │     2    │  300
Carol │   NULL   │ NULL   ← no orders
David │   NULL   │ NULL   ← no orders
NULL  │     4    │  200   ← no matching customer
*/
```

> ⚠️ MySQL does NOT support FULL OUTER JOIN. Workaround:
> ```sql
> SELECT * FROM a LEFT JOIN b ON a.id = b.a_id
> UNION
> SELECT * FROM a RIGHT JOIN b ON a.id = b.a_id;
> ```

---

## ⚪ CROSS JOIN

Returns the **Cartesian product** — every row of Table A combined with every row of Table B.

```
customers (4 rows) × order_statuses (3 rows) = 12 rows
```

```sql
SELECT c.name, s.status
FROM customers c
CROSS JOIN (VALUES ('pending'), ('paid'), ('shipped')) AS s(status);

/*
Alice | pending
Alice | paid
Alice | shipped
Bob   | pending
Bob   | paid
Bob   | shipped
...
(4 × 3 = 12 rows)
*/
```

**Use case:** Generate all combinations (e.g., all products × all stores for inventory).

> ⚠️ A 1000-row × 1000-row CROSS JOIN = 1,000,000 rows! Use carefully.

---

## 🔁 SELF JOIN

A table joined to **itself**. Used for hierarchical/recursive data.

```sql
-- employees: manager_id references the same employees table
SELECT
    e.name       AS employee,
    m.name       AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;

/*
employee | manager
─────────┼─────────
Alice    │ NULL       (Alice has no manager — she IS the top)
Bob      │ Alice
Carol    │ Alice
David    │ Bob
Eve      │ Bob
Frank    │ Alice
Grace    │ David
Hank     │ Frank
*/
```

---

## 📊 Visual Summary of All JOINs

```
A = customers   B = orders

INNER JOIN          LEFT JOIN           RIGHT JOIN          FULL OUTER JOIN
   A ∩ B            A + (A ∩ B)         B + (A ∩ B)          A ∪ B

 ┌───┬───┐         ┌───┬───┐          ┌───┬───┐           ┌───┬───┐
 │   │███│         │███│███│          │   │███│           │███│███│
 │   │███│         │███│███│          │   │███│           │███│███│
 └───┴───┘         └───┴───┘          └───┴───┘           └───┴───┘
Only overlap       All of A,          All of B,            Everything,
                   nulls for B        nulls for A          nulls both sides

CROSS JOIN          SELF JOIN
  A × B             A ⋈ A

Every A row        Table joined
combined with      to itself
every B row        (hierarchy)
```

---

## 🔗 Multiple Table JOINs

```sql
-- Join 3 tables: orders → customers → products via order_items
SELECT
    o.id         AS order_id,
    c.name       AS customer,
    p.name       AS product,
    oi.qty,
    oi.unit_price,
    (oi.qty * oi.unit_price) AS line_total
FROM orders o
JOIN customers  c  ON o.customer_id  = c.id
JOIN order_items oi ON o.id          = oi.order_id
JOIN products   p  ON oi.product_id  = p.id
WHERE o.status = 'paid'
ORDER BY o.id, p.name;
```

---

## ⚡ JOIN Optimization Tips

### 1. Always Index Foreign Key Columns

```sql
-- Without index on customer_id → full table scan for each order
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```

### 2. Filter Before Joining (Push Predicates Down)

```sql
-- ❌ Slow: joins all rows then filters
SELECT c.name, o.total
FROM customers c
JOIN orders o ON c.id = o.customer_id
WHERE o.created_at >= '2024-01-01';

-- ✅ Better: let optimizer push the filter (modern optimizers do this automatically)
-- But with subqueries, you control it:
SELECT c.name, recent.total
FROM customers c
JOIN (
    SELECT customer_id, total
    FROM orders
    WHERE created_at >= '2024-01-01'
) AS recent ON c.id = recent.customer_id;
```

### 3. Use EXPLAIN to Check Join Order

```sql
EXPLAIN SELECT c.name, o.total
FROM customers c
JOIN orders o ON c.id = o.customer_id;
-- Shows which table is scanned first, which indexes are used
```

### 4. Avoid Joining on Functions

```sql
-- ❌ Can't use index on LOWER(email)
JOIN customers c ON LOWER(c.email) = LOWER(o.email)

-- ✅ Store emails normalized, join directly
UPDATE customers SET email = LOWER(email);  -- normalize once
JOIN customers c ON c.email = o.email       -- then use index
```

---

## 🎯 Common JOIN Patterns

```sql
-- 1. Anti-join: customers who NEVER ordered
SELECT c.name
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.id IS NULL;

-- 2. Customers who ordered in BOTH 2023 and 2024
SELECT DISTINCT o1.customer_id
FROM orders o1
JOIN orders o2 ON o1.customer_id = o2.customer_id
WHERE EXTRACT(YEAR FROM o1.created_at) = 2023
  AND EXTRACT(YEAR FROM o2.created_at) = 2024;

-- 3. Employees and their direct reports count
SELECT
    m.name          AS manager,
    COUNT(e.id)     AS report_count
FROM employees m
LEFT JOIN employees e ON e.manager_id = m.id
GROUP BY m.id, m.name
ORDER BY report_count DESC;

-- 4. Products that have NEVER been ordered
SELECT p.name
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
WHERE oi.id IS NULL;
```

---

## 🔑 Key Takeaways

| Join Type | Returns | Use When |
|-----------|---------|----------|
| INNER JOIN | Rows matching in both tables | Only want complete matches |
| LEFT JOIN | All from left + matching right | All records from primary table |
| RIGHT JOIN | All from right + matching left | Rare; rewrite as LEFT JOIN |
| FULL OUTER JOIN | All from both tables | Detect unmatched rows in either table |
| CROSS JOIN | All combinations | Generate combinations (careful with large tables) |
| SELF JOIN | Table joined to itself | Hierarchies, comparing rows to same table |

**Best Practices:**
- Always use table aliases (`e`, `c`, `o`) in multi-table queries
- Index every foreign key column
- Use `EXPLAIN` to verify join plans
- Prefer LEFT JOIN over RIGHT JOIN (more readable)
- Use anti-join pattern (`LEFT JOIN ... WHERE right.id IS NULL`) to find orphans

---

**← Previous:** [12 — Window Functions](./12-window-functions.md)
**Next →** [14 — Subqueries](./14-subqueries.md)
