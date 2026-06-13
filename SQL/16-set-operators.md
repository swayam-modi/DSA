# 16 — Set Operators

> **Goal:** Combine result sets from multiple SELECT queries using UNION, INTERSECT, and EXCEPT.

---

## 📖 What are Set Operators?

Set operators combine the results of two or more SELECT statements into a single result set.

```
SELECT A ...      SET OPERATOR      SELECT B ...
  (5 rows)                            (4 rows)
       └──────────────┬───────────────┘
                      ▼
               Combined result
```

### Rules for Set Operators

1. Both queries must have the **same number of columns**
2. Corresponding columns must have **compatible data types**
3. Column names in the result come from the **first query**

```sql
-- ✅ Valid
SELECT id, name FROM employees
UNION
SELECT id, company_name FROM suppliers;   -- 2 cols, compatible types

-- ❌ Invalid: different number of columns
SELECT id, name, salary FROM employees
UNION
SELECT id, company_name FROM suppliers;
```

---

## 🔵 UNION — Combine and Remove Duplicates

Combines results of two queries and **removes duplicate rows**.

```sql
-- All emails (from customers and employees, no duplicates)
SELECT email, 'customer' AS source FROM customers
UNION
SELECT email, 'employee' AS source FROM employees;

-- All cities where we have customers or stores
SELECT city FROM customers
UNION
SELECT city FROM stores;   -- deduplicates!
```

### Visual

```
A = {1, 2, 3, 4}     B = {3, 4, 5, 6}

UNION = A ∪ B = {1, 2, 3, 4, 5, 6}   (no duplicates)
```

### ORDER BY with UNION

```sql
-- ORDER BY applies to the ENTIRE result (use last query's perspective)
SELECT name, salary FROM employees WHERE dept = 'Engineering'
UNION
SELECT name, salary FROM contractors WHERE dept = 'Engineering'
ORDER BY salary DESC;   -- applies to combined result
```

---

## 🟡 UNION ALL — Combine and Keep Duplicates

Like UNION but **keeps all duplicates**. Faster than UNION (no deduplication step).

```sql
-- All transactions (sales + refunds, including duplicates)
SELECT amount, 'sale'   AS type FROM sales
UNION ALL
SELECT amount, 'refund' AS type FROM refunds;

-- Count all records including duplicates
SELECT COUNT(*) FROM (
    SELECT id FROM table_a
    UNION ALL
    SELECT id FROM table_b
) AS combined;
```

### UNION vs UNION ALL

```
A = {1, 2, 3}     B = {2, 3, 4}

UNION:     {1, 2, 3, 4}      -- removes dup 2 and 3
UNION ALL: {1, 2, 3, 2, 3, 4} -- keeps all

Use UNION ALL when:
✅ You know there are no duplicates (different sources)
✅ You want duplicates (e.g., counting all transactions)
✅ Performance matters (UNION ALL is faster)

Use UNION when:
✅ You need deduplication (merging similar lists)
```

---

## 🟢 INTERSECT — Rows in Both Results

Returns only rows that appear in **both** queries.

```sql
-- Customers who are also newsletter subscribers
SELECT email FROM customers
INTERSECT
SELECT email FROM newsletter_subscribers;

-- Products ordered in BOTH Jan and Feb 2024
SELECT product_id FROM order_items
JOIN orders ON order_items.order_id = orders.id
WHERE EXTRACT(MONTH FROM orders.created_at) = 1
  AND EXTRACT(YEAR FROM orders.created_at) = 2024
INTERSECT
SELECT product_id FROM order_items
JOIN orders ON order_items.order_id = orders.id
WHERE EXTRACT(MONTH FROM orders.created_at) = 2
  AND EXTRACT(YEAR FROM orders.created_at) = 2024;
```

### Visual

```
A = {1, 2, 3, 4}     B = {3, 4, 5, 6}

INTERSECT = A ∩ B = {3, 4}   (only what's in both)
```

> ⚠️ MySQL does not support INTERSECT (before 8.0.31). Workaround with JOIN:
> ```sql
> SELECT a.email FROM customers a
> INNER JOIN newsletter_subscribers b ON a.email = b.email;
> -- or:
> SELECT email FROM customers WHERE email IN (SELECT email FROM newsletter_subscribers);
> ```

---

## 🔴 EXCEPT / MINUS — Rows in First but Not Second

Returns rows in the **first query** that are **not in the second**.

```sql
-- Customers who have NOT subscribed to newsletter
SELECT email FROM customers
EXCEPT
SELECT email FROM newsletter_subscribers;

-- Products that were ordered in Jan but NOT in Feb
SELECT product_id FROM order_items
JOIN orders ON order_items.order_id = orders.id
WHERE EXTRACT(MONTH FROM orders.created_at) = 1
EXCEPT
SELECT product_id FROM order_items
JOIN orders ON order_items.order_id = orders.id
WHERE EXTRACT(MONTH FROM orders.created_at) = 2;
```

### Visual

```
A = {1, 2, 3, 4}     B = {3, 4, 5, 6}

EXCEPT = A - B = {1, 2}   (in A but not B)
```

> 📝 **Oracle uses MINUS instead of EXCEPT:**
> ```sql
> SELECT email FROM customers
> MINUS
> SELECT email FROM newsletter_subscribers;
> ```

---

## 🗺️ Set Operators Summary Diagram

```
A = {1, 2, 3}    B = {3, 4, 5}

  ┌───────────────────────────────────────────────────────────────┐
  │                                                               │
  │  UNION        UNION ALL    INTERSECT    EXCEPT (A-B)          │
  │                                                               │
  │  {1,2,3,4,5}  {1,2,3,3,   {3}          {1,2}                │
  │  (no dups)     4,5}                     (A only)             │
  │               (keep dups)                                     │
  └───────────────────────────────────────────────────────────────┘
```

---

## ✅ Real-World Examples

```sql
-- 1. Find all unique contact emails across tables
SELECT email, 'customer'  AS role FROM customers
UNION
SELECT email, 'employee'  AS role FROM employees
UNION
SELECT email, 'supplier'  AS role FROM suppliers
ORDER BY email;

-- 2. Active users from this month AND last month (retained users)
SELECT user_id FROM sessions WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM NOW())
INTERSECT
SELECT user_id FROM sessions WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM NOW()) - 1;

-- 3. Products in our catalog but never stocked (new additions)
SELECT product_id FROM catalog
EXCEPT
SELECT product_id FROM inventory;

-- 4. Full audit log combining inserts and deletes
SELECT id, 'INSERT' AS action, created_at AS event_time FROM audit_inserts
UNION ALL
SELECT id, 'DELETE' AS action, deleted_at AS event_time FROM audit_deletes
ORDER BY event_time;

-- 5. Data reconciliation: records in DB1 but not DB2
SELECT account_id FROM production_db.accounts
EXCEPT
SELECT account_id FROM replica_db.accounts;
```

---

## ⚠️ Common Mistakes

```sql
-- ❌ ORDER BY in each sub-query (not allowed)
SELECT name FROM customers ORDER BY name   -- ❌
UNION
SELECT name FROM suppliers ORDER BY name;  -- ❌

-- ✅ ORDER BY only at the end
SELECT name FROM customers
UNION
SELECT name FROM suppliers
ORDER BY name;   -- applies to the whole result

-- ❌ Column count mismatch
SELECT id, name FROM customers
UNION
SELECT id FROM suppliers;   -- ❌ different column count

-- ❌ Incompatible types (implicit)
SELECT id, salary FROM employees    -- salary is DECIMAL
UNION
SELECT id, email FROM customers;   -- email is VARCHAR
-- May fail or implicitly cast — be explicit with CAST()
```

---

## 🔑 Key Takeaways

| Operator | Returns | Notes |
|----------|---------|-------|
| `UNION` | All rows, deduplicates | Slower (dedup step) |
| `UNION ALL` | All rows, including duplicates | Faster — use when dups OK |
| `INTERSECT` | Rows in BOTH queries | Not in MySQL < 8.0.31 |
| `EXCEPT` | Rows in first, not second | `MINUS` in Oracle |

**Rules:**
- Same number of columns in all queries
- Compatible column data types
- Column names from first query
- `ORDER BY` goes at the very end (applies to combined result)
- `UNION ALL` is faster than `UNION` — prefer it when no dedup needed

---

**← Previous:** [15 — Views](./15-views.md)
**Next →** [17 — CTEs & Derived Tables](./17-ctes-derived-tables.md)
