# 35 — Interview Preparation

> **Goal:** Master the most commonly asked SQL and database interview topics with clear explanations and ready answers.

---

## 🎯 Interview Strategy

```
1. Think out loud — explain your approach before writing SQL
2. Clarify the schema if not provided
3. Start with a simpler version, then optimize
4. Consider edge cases: NULLs, empty results, ties
5. Discuss indexes and performance after the query works
6. Know your RDBMS differences (MySQL vs PostgreSQL)
```

---

## 📋 SQL Fundamentals Quick Reference

### Joins — The Most Asked Topic

```sql
-- INNER JOIN: only matching rows in both tables
SELECT c.name, o.total FROM customers c JOIN orders o ON c.id = o.customer_id;

-- LEFT JOIN: all from left + matching right (NULLs for no match)
SELECT c.name, o.total FROM customers c LEFT JOIN orders o ON c.id = o.customer_id;

-- SELF JOIN: table joined to itself (org chart, reporting)
SELECT e.name, m.name AS manager FROM employees e LEFT JOIN employees m ON e.manager_id = m.id;

-- Anti-join: rows in left with NO match in right
SELECT c.name FROM customers c LEFT JOIN orders o ON c.id = o.customer_id WHERE o.id IS NULL;

-- Key question: "What's the difference between INNER, LEFT, FULL OUTER JOIN?"
-- INNER: only overlap; LEFT: all of left + overlap; FULL: all of both
```

### Window Functions

```sql
-- ROW_NUMBER: unique sequence, no ties
-- RANK: gaps after ties (1,2,2,4)
-- DENSE_RANK: no gaps after ties (1,2,2,3)

-- Classic question: "Get the 2nd highest salary per department"
SELECT dept_id, salary
FROM (
    SELECT dept_id, salary,
           DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS dr
    FROM employees
) t
WHERE dr = 2;
```

### CTEs

```sql
-- CTE vs Subquery: CTEs are named, reusable, can be recursive
WITH high_earners AS (
    SELECT * FROM employees WHERE salary > 80000
)
SELECT * FROM high_earners WHERE dept_id = 1;

-- Recursive CTE (hierarchy):
WITH RECURSIVE tree AS (
    SELECT id, name, 0 AS level FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.id, e.name, t.level + 1
    FROM employees e JOIN tree t ON e.manager_id = t.id
)
SELECT * FROM tree;
```

### Subqueries

```sql
-- Correlated: references outer query (runs once per outer row)
SELECT name FROM employees e
WHERE salary > (SELECT AVG(salary) FROM employees WHERE dept_id = e.dept_id);

-- EXISTS vs IN:
-- EXISTS: short-circuits on first match — use when subquery is large/correlated
-- IN: evaluates full subquery — use for small, static lists
-- NOT IN with NULL: always returns 0 rows! Use NOT EXISTS instead.
```

### Indexes

```sql
-- B-Tree: default, supports =, <, >, BETWEEN, ORDER BY, LIKE 'prefix%'
-- Hash: equality only, no range, no sort
-- GIN: full-text search, JSONB, arrays
-- Partial: WHERE is_active = TRUE (index only active rows)
-- Composite: follows leftmost prefix rule

-- "When would you NOT use an index?"
-- Small tables (< 10K rows), frequently updated columns, low cardinality (bool), 
-- LIKE '%suffix' (can't use B-tree)
```

---

## 🏗️ Database Design Questions

### Q: Design a banking system schema

```sql
CREATE TABLE customers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE accounts (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    type        VARCHAR(20) CHECK (type IN ('checking', 'savings', 'credit')),
    balance     DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency    CHAR(3) DEFAULT 'USD',
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
    id             BIGSERIAL PRIMARY KEY,
    from_account   BIGINT REFERENCES accounts(id),
    to_account     BIGINT REFERENCES accounts(id),
    amount         DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type           VARCHAR(20),   -- debit, credit, transfer
    reference      VARCHAR(50) UNIQUE,
    created_at     TIMESTAMP DEFAULT NOW()
);

-- Transfer money (transaction-safe)
BEGIN;
  UPDATE accounts SET balance = balance - 500 WHERE id = 1;
  UPDATE accounts SET balance = balance + 500 WHERE id = 2;
  CHECK balance >= 0;  -- prevent overdraft
COMMIT;
```

### Q: Design an e-commerce schema (key points)

```
entities: customers, products, categories, orders, order_items, cart, reviews
key decisions:
  - Store price in order_items (snapshot — product price can change)
  - M:N: products ↔ categories, orders ↔ products (via order_items)
  - Soft delete: is_active column instead of DELETE
  - created_at + updated_at on every table
  - Unique constraint: one review per customer per product
```

---

## ⚡ Performance Questions

### Q: How do you find a slow query?

```
1. Enable slow query log (MySQL: slow_query_log = ON, threshold = 1s)
   Or: pg_stat_statements (PostgreSQL)
2. EXPLAIN ANALYZE the query
3. Look for Seq Scan on large tables → missing index
4. Look for large row estimates → stale statistics → ANALYZE
5. Identify: functions on indexed columns, NOT IN with NULLs, SELECT *
```

### Q: What's a covering index?

```sql
-- Query needs: dept (filter) + name, salary (select)
-- Without covering index: index scan on dept → fetch each row for name, salary

-- Covering index: all query columns in the index → no table fetch needed!
CREATE INDEX idx_emp_covering ON employees(dept_id) INCLUDE (name, salary);
-- PostgreSQL: INCLUDE adds columns to leaf nodes only (not searchable)
-- or: CREATE INDEX idx_emp_covering ON employees(dept_id, name, salary);
-- Now: index-only scan → 10x+ faster!
```

### Q: EXPLAIN output interpretation

```
Seq Scan         → Full table scan (no index) ⚠️
Index Scan       → Uses index, fetches rows ✅  
Index Only Scan  → Everything from index (covering) ✅✅
Nested Loop      → Inner table scanned per outer row
Hash Join        → Build hash table, probe → good for medium tables
Merge Join       → Both sides sorted → good for sorted large tables
```

---

## 🔒 Transactions & Isolation

### Q: What is ACID?

```
Atomicity    → All operations in a transaction succeed or all fail (BEGIN...COMMIT/ROLLBACK)
Consistency  → DB goes from one valid state to another; constraints always enforced
Isolation    → Concurrent transactions don't see each other's in-progress changes
Durability   → Committed data survives crashes (WAL/redo log)
```

### Q: What are isolation levels and when to use them?

```
Read Uncommitted  → Can read dirty data (uncommitted changes) — almost never use
Read Committed    → Default in PG, SQL Server — prevents dirty reads — most OLTP apps
Repeatable Read   → Default in MySQL — prevents dirty + non-repeatable reads
Serializable      → Prevents all anomalies — critical financial operations
Snapshot          → MVCC-based, readers don't block writers — PostgreSQL default behavior

Interview trap: "What problems does each level prevent?"
  Dirty Read        → prevented by Read Committed+
  Non-Repeatable    → prevented by Repeatable Read+
  Phantom Read      → prevented by Serializable
```

### Q: What is a deadlock? How do you prevent it?

```
Deadlock = circular lock wait; T1 holds A, waits for B; T2 holds B, waits for A.

Prevention:
  1. Always acquire locks in the same order (most effective)
  2. Keep transactions short (release locks quickly)
  3. Use lower isolation when sufficient
  4. Implement retry logic with backoff for deadlock errors (code: 40P01 in PostgreSQL)

Resolution: DB automatically picks a victim, rolls it back.
```

---

## 📐 Normalization

### Q: What are the normal forms?

```
1NF: Atomic values, no repeating groups, has a PK
2NF: 1NF + no partial dependencies (all non-key cols depend on WHOLE PK)
3NF: 2NF + no transitive dependencies (no non-key col depends on another non-key col)
BCNF: 3NF + every functional dependency left side is a superkey

"To what level should you normalize?"
→ 3NF for OLTP. Denormalize selectively for OLAP/reporting.
```

### Q: What is denormalization?

```
Intentionally introducing redundancy for performance:
  - Store product_name in order_items (avoid JOIN for reports)
  - Pre-compute order totals (avoid SUM every time)
  - Materialized views (periodically refreshed snapshots)

Trade-off: faster reads, slower writes, potential inconsistency.
Use for: read-heavy workloads, data warehouses, analytics.
```

---

## 🏦 Real-World Scenario Queries

### Banking System

```sql
-- Find accounts with negative balance
SELECT a.id, c.name, a.balance
FROM accounts a JOIN customers c ON a.customer_id = c.id
WHERE a.balance < 0;

-- Top 5 customers by total transactions last 30 days
SELECT c.name, COUNT(t.id) AS tx_count, SUM(t.amount) AS total
FROM customers c
JOIN accounts a ON c.id = a.customer_id
JOIN transactions t ON a.id = t.from_account
WHERE t.created_at >= NOW() - INTERVAL '30 days'
GROUP BY c.id, c.name
ORDER BY total DESC LIMIT 5;
```

### E-commerce

```sql
-- Products low on stock with their revenue
SELECT p.name, p.stock,
       COALESCE(SUM(oi.qty * oi.price), 0) AS total_revenue
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
WHERE p.stock < 10
GROUP BY p.id, p.name, p.stock
ORDER BY p.stock;

-- Customer LTV (lifetime value)
SELECT c.name, COUNT(o.id) AS orders, SUM(o.total) AS ltv
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id AND o.status = 'paid'
GROUP BY c.id, c.name
ORDER BY ltv DESC NULLS LAST;
```

### Library System

```sql
-- Books checked out but not returned (overdue)
SELECT b.title, m.name, l.due_date,
       CURRENT_DATE - l.due_date AS days_overdue
FROM loans l
JOIN books b ON l.book_id = b.id
JOIN members m ON l.member_id = m.id
WHERE l.return_date IS NULL AND l.due_date < CURRENT_DATE
ORDER BY days_overdue DESC;
```

---

## 🔑 Must-Know Interview Answers

| Question | Quick Answer |
|----------|-------------|
| DELETE vs TRUNCATE | DELETE = row-by-row, WHERE, rollback; TRUNCATE = instant, no WHERE, resets identity |
| WHERE vs HAVING | WHERE filters rows (before GROUP BY); HAVING filters groups (after) |
| UNION vs UNION ALL | UNION deduplicates; UNION ALL keeps all — UNION ALL is faster |
| Primary vs Unique Key | PK: NOT NULL + UNIQUE, one per table; UNIQUE: allows one NULL, multiple per table |
| Clustered vs Non-clustered | Clustered: data physically sorted by key; Non-clustered: separate index with pointers |
| Correlated subquery | References outer query; runs once per outer row — often slow |
| DENSE_RANK vs RANK | RANK has gaps (1,2,2,4); DENSE_RANK has no gaps (1,2,2,3) |
| EXISTS vs IN | EXISTS short-circuits; IN evaluates all; NOT IN broken with NULLs — use NOT EXISTS |
| Dirty/Phantom/Non-repeatable reads | Dirty: read uncommitted; NRR: same row different values; Phantom: different row count |
| CAP theorem | Consistency, Availability, Partition tolerance — pick 2 in distributed systems |

---

## 📚 Resources

- [LeetCode SQL](https://leetcode.com/problemset/database/) — 70+ SQL problems
- [HackerRank SQL](https://www.hackerrank.com/domains/sql) — Beginner to Advanced
- [Mode Analytics SQL Tutorial](https://mode.com/sql-tutorial/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Use The Index, Luke](https://use-the-index-luke.com/) — Indexing deep-dive

---

**← Previous:** [34 — Practice Problems](./34-practice-problems.md)
**← Back to Index:** [README.md](./README.md)

---

*🎉 Congratulations on completing the SQL & Relational Database Complete Learning Guide!*
