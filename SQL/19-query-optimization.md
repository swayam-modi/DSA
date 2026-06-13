# 19 — Query Optimization

> **Goal:** Write efficient SQL queries and understand the tools to diagnose and fix slow queries.

---

## 📖 How the Query Optimizer Works

The **Query Optimizer** takes your SQL and finds the most efficient way to execute it.

```
SQL Query
    │
    ▼
Parser           → check syntax, build parse tree
    │
    ▼
Query Rewriter   → apply views, expand wildcards
    │
    ▼
Optimizer        → generate possible execution plans
                   → estimate cost of each plan
                   → choose the lowest-cost plan
    │
    ▼
Executor         → run the chosen plan
    │
    ▼
Result Set
```

The optimizer uses **statistics** (table size, column value distribution, index availability) to estimate costs. Keep statistics up to date:

```sql
-- PostgreSQL
ANALYZE employees;          -- update statistics for one table
ANALYZE;                    -- update all

-- MySQL
ANALYZE TABLE employees;

-- SQL Server
UPDATE STATISTICS employees;
```

---

## 📊 Reading Execution Plans

```sql
-- PostgreSQL: read-only plan
EXPLAIN SELECT * FROM orders WHERE customer_id = 42;

-- PostgreSQL: execute + measure actual time
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM orders WHERE customer_id = 42;
```

### Key Terms

| Term | Meaning | Good/Bad |
|------|---------|----------|
| `Seq Scan` | Full table scan — reads every row | ⚠️ Bad on large tables |
| `Index Scan` | Uses index, fetches rows | ✅ Good |
| `Index Only Scan` | Everything from index, no row fetch | ✅✅ Best |
| `Nested Loop` | For each outer row, probe inner (good for small tables) | ✅/⚠️ |
| `Hash Join` | Build hash table from small table, scan large | ✅ Good for medium tables |
| `Merge Join` | Both sides sorted then merged | ✅ Good for sorted inputs |
| `Sort` | Explicit sort (no index for ORDER BY) | ⚠️ Consider index |
| `Hash Aggregate` | GROUP BY via hashing | ✅ Usually efficient |

---

## 🚀 Performance Best Practices

### 1. Avoid SELECT *

```sql
-- ❌ Fetches all columns (including large TEXT/BLOB fields)
SELECT * FROM products;

-- ✅ Fetch only what you need
SELECT id, name, price FROM products;

-- Reason:
--   • Less network transfer
--   • Enables index-only scans (covering indexes)
--   • Future schema changes won't break query assumptions
```

### 2. Use Proper Indexing

```sql
-- ❌ No index on customer_id → full scan on 1M rows
SELECT * FROM orders WHERE customer_id = 42;

-- ✅ Add index
CREATE INDEX idx_orders_customer ON orders(customer_id);
-- Now → index seek → direct access to matching rows
```

### 3. Avoid Functions on Indexed Columns in WHERE

```sql
-- ❌ Index on hire_date can't be used (function wraps the column)
SELECT * FROM employees WHERE YEAR(hire_date) = 2023;

-- ✅ Rewrite to use the column directly (range)
SELECT * FROM employees
WHERE hire_date >= '2023-01-01' AND hire_date < '2024-01-01';
-- Index on hire_date can now be used!

-- ❌ Case insensitive search (no index on LOWER(email))
WHERE LOWER(email) = 'alice@email.com'

-- ✅ Store emails normalized; or use functional index:
CREATE INDEX idx_email_lower ON users(LOWER(email));   -- PostgreSQL
WHERE LOWER(email) = 'alice@email.com'   -- now uses the index
```

### 4. Avoid Leading Wildcards in LIKE

```sql
-- ❌ Leading wildcard → can't use B-tree index
WHERE name LIKE '%smith'       -- full scan

-- ✅ Prefix → uses index
WHERE name LIKE 'smith%'       -- index scan on name

-- For full-text search needs → use Full Text Search (GIN/GiST indexes)
CREATE INDEX idx_products_fts ON products USING gin(to_tsvector('english', name));
```

### 5. Use EXISTS Instead of IN for Correlated Checks

```sql
-- ❌ IN with large subquery: evaluates full subquery first
SELECT * FROM customers
WHERE id IN (SELECT customer_id FROM orders WHERE total > 1000);

-- ✅ EXISTS: short-circuits on first match
SELECT * FROM customers c
WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.customer_id = c.id AND o.total > 1000
);
```

### 6. Avoid N+1 Queries (App Side)

```
N+1 Problem:
  1 query to get customers:         SELECT * FROM customers;
  N queries to get their orders:    SELECT * FROM orders WHERE customer_id = 1;
                                    SELECT * FROM orders WHERE customer_id = 2;
                                    ... (one per customer!)

Fix: JOIN in one query
  SELECT c.name, o.id, o.total
  FROM customers c
  JOIN orders o ON c.id = o.customer_id;
```

### 7. Limit Result Sets

```sql
-- Always LIMIT when you only need a few rows
SELECT * FROM logs ORDER BY created_at DESC LIMIT 100;

-- Use cursor/keyset pagination for large datasets (better than OFFSET):
-- ❌ OFFSET gets slow on large offsets (must scan all prior rows)
SELECT * FROM orders ORDER BY id LIMIT 20 OFFSET 100000;

-- ✅ Keyset pagination (fast, consistent):
SELECT * FROM orders WHERE id > :last_seen_id ORDER BY id LIMIT 20;
```

### 8. Use UNION ALL Over UNION When Possible

```sql
-- ❌ UNION deduplicates (sorts and scans twice)
SELECT id FROM table_a UNION SELECT id FROM table_b;

-- ✅ If no duplicates exist (or you want them)
SELECT id FROM table_a UNION ALL SELECT id FROM table_b;
```

### 9. Avoid Implicit Type Conversions

```sql
-- Column: customer_id INT
-- ❌ Implicit cast: '42' (string) → 42 (int) prevents index use in some RDBMS
WHERE customer_id = '42'

-- ✅ Use correct type
WHERE customer_id = 42
```

### 10. Reduce Full Table Scans

```sql
-- Check with EXPLAIN if a Seq Scan appears on large tables
-- Fix: add appropriate index

-- Partitioning also helps (Chapter 33):
-- Only scan relevant partition instead of whole table
```

---

## 🛠️ Query Tuning Workflow

```
1. Identify slow query
   → Enable slow query log (MySQL: slow_query_log = ON)
   → pg_stat_statements (PostgreSQL)

2. Run EXPLAIN ANALYZE
   → Find Seq Scans, high row estimates, expensive sorts

3. Check indexes
   → Missing index? → CREATE INDEX
   → Wrong index order? → fix composite index
   → Index not used? → rewrite query to let optimizer use it

4. Check table statistics
   → ANALYZE / UPDATE STATISTICS

5. Rewrite query
   → Avoid functions on columns
   → Use EXISTS instead of IN
   → Push filters closer to the data source

6. Consider schema changes
   → Add covering indexes
   → Partition large tables
   → Denormalize hot-path queries

7. Monitor and verify improvement
   → Re-run EXPLAIN ANALYZE → confirm plan changed
   → Compare execution times
```

---

## 📈 pg_stat_statements (PostgreSQL)

Find your slowest queries:

```sql
-- Enable the extension
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Find top 10 slowest queries by total execution time
SELECT
    query,
    calls,
    ROUND(total_exec_time::NUMERIC / 1000, 2) AS total_seconds,
    ROUND(mean_exec_time::NUMERIC, 2)          AS avg_ms,
    rows
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;
```

---

## 🔑 Key Takeaways

| Rule | Reason |
|------|--------|
| `EXPLAIN ANALYZE` before optimizing | Know the actual problem, don't guess |
| Don't use `SELECT *` | Over-fetching; prevents covering indexes |
| Index FK columns | JOINs become fast |
| Avoid functions on indexed columns in WHERE | Prevents index use |
| Use prefix LIKE, not `%prefix` | Prefix allows index scan |
| Use EXISTS over IN for correlated checks | Short-circuits, often faster |
| Use UNION ALL over UNION | Skip deduplication cost |
| Limit offsets for pagination | Use keyset pagination for large datasets |
| Keep statistics current | Optimizer makes better plans |
| Use partial indexes for selective data | Smaller, faster indexes |

---

**← Previous:** [18 — Indexing](./18-indexing.md)
**Next →** [20 — Variables & Control Flow](./20-variables-control-flow.md)
