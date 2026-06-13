# 18 — Indexing

> **Goal:** Understand how indexes work, the different types available, and how to use them to dramatically speed up queries.

---

## 📖 Why Indexes?

Without an index, every query does a **full table scan** — reading every row to find matches. With an index, the database can jump directly to the relevant rows.

```
Without Index (Full Table Scan):       With Index (Index Seek):
─────────────────────────────────      ──────────────────────────────────
SELECT * FROM orders                   SELECT * FROM orders
WHERE customer_id = 42;                WHERE customer_id = 42;

Reads ALL 1,000,000 rows           →   Reads ~10 rows from B-tree
and checks each one                    and directly accesses data

Like reading every page               Like using a book's index:
of a book to find a topic             "customer_id 42 → pages 441, 2891, 7023"
```

**When indexes are most useful:**
- `WHERE` conditions on large tables
- `JOIN` conditions (FK columns)
- `ORDER BY` and `GROUP BY` columns
- Columns used in LIKE (prefix only: `LIKE 'abc%'`)

**When indexes hurt:**
- `INSERT`, `UPDATE`, `DELETE` — each change requires index maintenance
- Small tables — full scan may be faster than index lookup
- Very high cardinality writes (e.g., log tables)

---

## 🌳 Index Internals: B-Tree

The default index type in PostgreSQL, MySQL, and SQL Server is the **B-Tree (Balanced Tree)**.

```
B-Tree Index on salary:

                    ┌─────────────┐
                    │     70000   │  ← root node
                    └──────┬──────┘
              ┌────────────┴────────────┐
        ┌─────▼─────┐            ┌──────▼──────┐
        │  65000    │            │   85000     │  ← internal nodes
        └─────┬─────┘            └──────┬──────┘
      ┌───────┴──────┐         ┌────────┴───────┐
  ┌───▼───┐      ┌───▼───┐  ┌──▼────┐      ┌────▼──┐
  │65000  │      │72000  │  │90000  │      │95000  │  ← leaf nodes
  │→ row  │      │→ row  │  │→ row  │      │→ row  │  (point to actual rows)
  └───────┘      └───────┘  └───────┘      └───────┘

Lookup salary = 90000:
  root (70000) → right (85000) → right leaf (90000) → row pointer ✅
  ~3 comparisons instead of scanning all 8 rows!
```

**B-Tree properties:**
- Self-balancing — all leaf nodes at same depth
- Supports: `=`, `<`, `>`, `<=`, `>=`, `BETWEEN`, `LIKE 'prefix%'`, `ORDER BY`
- `O(log n)` lookup time

---

## 🗂️ Types of Indexes

### Clustered Index

The table data is **physically sorted** by the clustered index key. Only **one per table**.

```
SQL Server / MySQL InnoDB use Clustered Indexes:

Clustered index on id (default for PRIMARY KEY):
  Data pages on disk are sorted by id:
  [id=1 | id=2 | id=3 | ... | id=1000000]

  Lookup id=500000 → go to middle of data → found! ✅

PostgreSQL: heap-based (no true clustered index)
  But: CLUSTER command can physically reorder once
  CREATE INDEX idx_orders_date ON orders(created_at);
  CLUSTER orders USING idx_orders_date;  -- one-time reorder
```

### Non-Clustered Index (Secondary Index)

A **separate structure** from the table data. Contains index keys + pointers (row IDs / PKs) to actual rows.

```sql
-- Create a non-clustered index
CREATE INDEX idx_employees_dept ON employees(dept);

-- How it works:
Index: dept → row_pointer
  'Engineering' → [rowid1, rowid3, rowid6, rowid8]
  'HR'          → [rowid4, rowid7]
  'Marketing'   → [rowid2, rowid5]

Query: WHERE dept = 'HR'
  → Index lookup → [rowid4, rowid7]
  → Fetch rows 4 and 7 from table
```

---

### Composite Index

An index on **multiple columns**. Column order matters!

```sql
CREATE INDEX idx_emp_dept_salary ON employees(dept, salary);

-- Effective for:
WHERE dept = 'Engineering'                       -- ✅ leftmost prefix
WHERE dept = 'Engineering' AND salary > 80000    -- ✅ leftmost prefix + more
WHERE dept = 'Engineering' ORDER BY salary       -- ✅

-- NOT effective for:
WHERE salary > 80000                             -- ❌ not leftmost column
WHERE salary > 80000 AND dept = 'Engineering'   -- ❌ (optimizer may reorder)
```

```
Leftmost Prefix Rule:
Index (A, B, C) can be used for:
  WHERE A = ?                     ✅
  WHERE A = ? AND B = ?           ✅
  WHERE A = ? AND B = ? AND C = ? ✅
  WHERE A = ? ORDER BY B          ✅ (sometimes)
  WHERE B = ?                     ❌ (skips A)
  WHERE C = ?                     ❌ (skips A and B)
```

---

### Unique Index

Enforces uniqueness while also providing fast lookup.

```sql
-- Creating a unique index
CREATE UNIQUE INDEX idx_users_email ON users(email);

-- This is equivalent to adding a UNIQUE constraint:
ALTER TABLE users ADD CONSTRAINT uq_email UNIQUE (email);
-- (PostgreSQL creates a unique index automatically for UNIQUE constraints)
```

---

### Partial Index

Indexes only rows matching a **filter condition**. Smaller and faster.

```sql
-- Index only active users (not inactive/deleted ones)
CREATE INDEX idx_users_active_email
ON users(email)
WHERE is_active = TRUE;

-- Index only orders not yet fulfilled
CREATE INDEX idx_orders_pending
ON orders(created_at)
WHERE status = 'pending';

-- Use case: only 5% of orders are pending — index only those 5%
-- Instead of 1M rows indexed, only 50K rows indexed → much smaller
```

---

### Covering Index

An index that **includes all columns** needed by a query — no need to fetch the actual row.

```sql
-- Query needs: dept (filter) + name, salary (select)
SELECT name, salary
FROM employees
WHERE dept = 'Engineering';

-- Non-covering index on dept:
--   1. Index lookup: dept='Engineering' → rowids
--   2. Fetch actual rows to get name, salary (extra I/O!)

-- Covering index (includes name and salary):
CREATE INDEX idx_emp_dept_covering ON employees(dept)
INCLUDE (name, salary);    -- PostgreSQL / SQL Server syntax

-- OR: composite index with all needed columns
CREATE INDEX idx_emp_dept_name_sal ON employees(dept, name, salary);
-- Now query can be answered entirely from the index (index-only scan)!
```

---

### Hash Index

Fast for **exact equality** lookups. Does NOT support range queries.

```sql
-- PostgreSQL: hash index
CREATE INDEX idx_users_email_hash ON users USING HASH (email);

-- Use for: WHERE email = 'alice@email.com'  ✅
-- Useless for: WHERE email LIKE 'alice%'    ❌
-- Useless for: ORDER BY email               ❌
```

---

## 📊 EXPLAIN — Analyze Query Plans

`EXPLAIN` shows how the database will execute a query.

```sql
-- PostgreSQL
EXPLAIN SELECT name FROM employees WHERE dept = 'Engineering';
/*
Seq Scan on employees  (cost=0.00..1.18 rows=4 width=17)
  Filter: ((dept)::text = 'Engineering'::text)
*/

-- After creating index:
CREATE INDEX idx_emp_dept ON employees(dept);

EXPLAIN SELECT name FROM employees WHERE dept = 'Engineering';
/*
Index Scan using idx_emp_dept on employees  (cost=0.14..8.16 rows=4 width=17)
  Index Cond: ((dept)::text = 'Engineering'::text)
*/
```

### EXPLAIN ANALYZE — Execute + Measure

```sql
-- PostgreSQL: runs the query and shows ACTUAL timings
EXPLAIN ANALYZE
SELECT * FROM orders WHERE customer_id = 42;
/*
Seq Scan on orders  (cost=0.00..25000.00 rows=100 width=80)
                    (actual time=0.042..284.122 rows=23 loops=1)
  Filter: (customer_id = 42)
  Rows Removed by Filter: 999977
Planning Time: 0.5 ms
Execution Time: 284.3 ms   ← very slow! Full scan.

After: CREATE INDEX idx_orders_customer ON orders(customer_id);

Index Scan using idx_orders_customer on orders
                    (cost=0.43..12.35 rows=23 width=80)
                    (actual time=0.018..0.062 rows=23 loops=1)
Execution Time: 0.1 ms    ← 2000x faster!
*/
```

### Reading EXPLAIN Output

```
Seq Scan     → Full table scan (no index used) ⚠️
Index Scan   → Index used, then fetch rows ✅
Index Only Scan → Index covers all needed columns (fastest) ✅✅
Bitmap Heap Scan → Multiple index lookups batched (good for ranges)
Nested Loop  → Join strategy: for each row in outer, scan inner
Hash Join    → Build hash table from smaller table, probe with larger
Merge Join   → Both tables sorted, merged together
```

---

## 📋 Index Management

```sql
-- List all indexes on a table (PostgreSQL)
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'employees';

-- Check index usage statistics (PostgreSQL)
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,     -- times index was used
    idx_tup_read  -- tuples returned via index
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Drop an index
DROP INDEX idx_emp_dept;
DROP INDEX IF EXISTS idx_emp_dept;   -- no error if not exists

-- Rebuild an index (removes bloat, PostgreSQL)
REINDEX INDEX idx_emp_dept;
REINDEX TABLE employees;

-- MySQL: show indexes
SHOW INDEXES FROM employees;
```

---

## 🎯 Indexing Strategy Best Practices

```
1. Index every Foreign Key column
   CREATE INDEX idx_orders_customer ON orders(customer_id);

2. Index columns frequently used in WHERE
   WHERE status = 'active'    → CREATE INDEX ON orders(status);

3. Index columns used in JOIN ON conditions
   JOIN orders ON c.id = o.customer_id  → index on o.customer_id

4. Index columns in ORDER BY / GROUP BY
   ORDER BY created_at DESC   → CREATE INDEX ON orders(created_at);

5. Use composite indexes for multi-column WHERE
   WHERE dept = 'Eng' AND salary > 80000
   → CREATE INDEX ON employees(dept, salary);

6. Use partial indexes for selective conditions
   WHERE is_active = TRUE     → CREATE INDEX ... WHERE is_active = TRUE;

7. Consider covering indexes for hot queries
   → INCLUDE all columns the query needs

8. Don't over-index: every index costs INSERT/UPDATE performance
   → Monitor with pg_stat_user_indexes and remove unused ones

9. Avoid indexing:
   - Very small tables (full scan is fine)
   - Columns with very low cardinality (Boolean: only TRUE/FALSE)
   - Columns that change very frequently (update overhead)
```

---

## 🔑 Key Takeaways

| Index Type | Best For | Limitations |
|-----------|---------|------------|
| B-Tree (default) | Equality, ranges, sorting | Not for unstructured text |
| Clustered | Primary key lookups | One per table |
| Non-clustered | Secondary lookups | Extra storage, slower writes |
| Composite | Multi-column WHERE | Leftmost prefix rule |
| Unique | Uniqueness + fast lookup | Only unique values |
| Partial | Selective subsets | Only filters matching rows |
| Covering | Hot read-heavy queries | More storage |
| Hash | Exact equality only | No range, no sort |

**The Golden Rule:** Indexes speed up SELECTs but slow down INSERTs/UPDATEs/DELETEs. Index strategically — profile first, then add.

---

**← Previous:** [17 — CTEs & Derived Tables](./17-ctes-derived-tables.md)
**Next →** [19 — Query Optimization](./19-query-optimization.md)
