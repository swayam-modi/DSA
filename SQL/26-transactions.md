# 26 — Transactions

> **Goal:** Understand how transactions group SQL operations into atomic, consistent, isolated, and durable units of work.

---

## 📖 What is a Transaction?

A **transaction** is a sequence of SQL operations that are treated as a **single logical unit of work**. Either ALL operations succeed, or NONE of them take effect.

```
Bank Transfer: Debit Alice $500, Credit Bob $500

Without transaction:
  Step 1: Alice - $500  ← succeeds
  [CRASH HERE!]
  Step 2: Bob + $500   ← never happens!
  → Alice lost $500, Bob got nothing! 💸

With transaction:
  BEGIN;
    UPDATE accounts SET balance = balance - 500 WHERE id = 'Alice';
    UPDATE accounts SET balance = balance + 500 WHERE id = 'Bob';
  COMMIT;      ← both succeed, or
  ROLLBACK;    ← both are undone (if anything fails)
```

---

## 💎 ACID Properties

### Atomicity — All or Nothing

```
A transaction is atomic: all operations complete or none do.

BEGIN;
  INSERT INTO orders (total) VALUES (500);      -- succeeds
  INSERT INTO order_items (invalid) VALUES...;  -- fails
ROLLBACK;   ← first INSERT is undone too!
```

### Consistency — Rules Are Maintained

```
The database goes from one valid state to another.
Constraints (NOT NULL, FK, CHECK) are enforced.
Balance before = Balance after + transferred amount.

If any constraint is violated → transaction rolls back → DB stays consistent.
```

### Isolation — Concurrent Transactions Don't Interfere

```
Transaction 1:              Transaction 2:
SELECT balance             SELECT balance
  → sees 1000               → sees 1000 (not T1's in-progress change)
UPDATE balance = 500
(not committed yet)
COMMIT                     UPDATE balance = 700
                           → based on 1000, not T1's 500?
                           → Depends on Isolation Level (Chapter 27)
```

### Durability — Committed Data Survives Crashes

```
Once COMMIT is done:
  → Written to transaction log (WAL)
  → Survives crashes, power failures
  → Even before data reaches disk (log replay on restart)

COMMIT
  │
  ├── Write to WAL log (synchronous)  ← durability guaranteed here
  │
  └── Flush to data files (async, later)
```

---

## 🔧 Transaction Commands

### BEGIN / START TRANSACTION

```sql
-- PostgreSQL
BEGIN;
-- or
BEGIN TRANSACTION;
-- or
START TRANSACTION;    -- MySQL / SQL Server

-- SQL Server
BEGIN TRANSACTION;    -- or BEGIN TRAN
```

### COMMIT

Permanently save all changes made in the transaction.

```sql
BEGIN;
  UPDATE accounts SET balance = balance - 500 WHERE id = 1;
  UPDATE accounts SET balance = balance + 500 WHERE id = 2;
COMMIT;
-- Both updates are now permanent!
```

### ROLLBACK

Undo ALL changes made since BEGIN.

```sql
BEGIN;
  DELETE FROM orders WHERE customer_id = 1;
  -- Oops! Wrong customer!
ROLLBACK;
-- DELETE never happened!
```

### SAVEPOINT

Create a partial rollback point within a transaction.

```sql
BEGIN;
  INSERT INTO orders (total) VALUES (500);         -- OK
  SAVEPOINT sp1;

  INSERT INTO order_items (product_id) VALUES (99); -- might fail
  SAVEPOINT sp2;

  UPDATE inventory SET stock = stock - 1;           -- OK

  -- Oops, order_items had an issue
  ROLLBACK TO SAVEPOINT sp1;   -- Undo everything after sp1 (items + inventory)
  -- The original INSERT orders is still intact!

COMMIT;   -- Commits only the original INSERT
```

```sql
-- Release a savepoint (frees memory, but keeps changes)
RELEASE SAVEPOINT sp2;
```

---

## 📋 Transaction Types

### Explicit Transactions

You manually control BEGIN and COMMIT/ROLLBACK.

```sql
BEGIN;
  UPDATE accounts SET balance = balance - 200 WHERE user_id = 1;
  UPDATE accounts SET balance = balance + 200 WHERE user_id = 2;
  INSERT INTO transfer_log (from_id, to_id, amount) VALUES (1, 2, 200);
COMMIT;
```

### Implicit (Auto-Commit) Transactions

Most RDBMS run each statement in its own auto-committed transaction by default.

```sql
-- Each of these auto-commits immediately:
INSERT INTO orders (total) VALUES (100);   -- auto-committed
UPDATE products SET stock = stock - 1;    -- auto-committed
DELETE FROM sessions WHERE expired = TRUE; -- auto-committed

-- Disable auto-commit (PostgreSQL)
\set AUTOCOMMIT off    -- psql CLI

-- MySQL
SET autocommit = 0;
-- now you must COMMIT explicitly
```

### Distributed Transactions (2-Phase Commit)

Transactions spanning multiple databases/services. Out of scope for this chapter.

---

## 🔁 Transaction with Error Handling

### PostgreSQL

```sql
DO $$
BEGIN
    BEGIN;    -- implicit in DO block

    UPDATE accounts SET balance = balance - 500 WHERE id = 1;
    UPDATE accounts SET balance = balance + 500 WHERE id = 2;

    -- Verify constraint
    IF (SELECT balance FROM accounts WHERE id = 1) < 0 THEN
        RAISE EXCEPTION 'Insufficient funds!';
    END IF;

    COMMIT;   -- implicit in DO block

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;   -- implicit on exception
        RAISE;
END;
$$;
```

### SQL Server

```sql
BEGIN TRANSACTION;

BEGIN TRY
    UPDATE accounts SET balance = balance - 500 WHERE id = 1;
    UPDATE accounts SET balance = balance + 500 WHERE id = 2;

    IF (SELECT balance FROM accounts WHERE id = 1) < 0
        THROW 50001, 'Insufficient funds', 1;

    COMMIT TRANSACTION;
    PRINT 'Transfer successful';
END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
    PRINT 'Error: ' + ERROR_MESSAGE();
END CATCH;
```

---

## 🗺️ Transaction Flow Diagram

```
BEGIN TRANSACTION
    │
    ▼
Execute SQL statements
    │
    ├── If all OK ──────────► COMMIT ──► Changes permanent
    │                                    WAL written
    │
    └── If error ──────────► ROLLBACK ─► Changes undone
         │                               DB back to pre-BEGIN state
         │
         └── Partial error → ROLLBACK TO SAVEPOINT ──► Partial undo
```

---

## ⚠️ Common Transaction Mistakes

```sql
-- ❌ Long-running transactions (hold locks too long)
BEGIN;
  -- Application processes for 5 minutes...
  UPDATE orders ...;
COMMIT;
-- Other transactions are blocked for 5 minutes!

-- ✅ Keep transactions SHORT — do processing outside transaction
-- Fetch data, process in application, then open brief transaction to write

-- ❌ Committing inside a loop (creates many small transactions)
FOR each_record IN records LOOP
  BEGIN;
  INSERT INTO target SELECT ... WHERE id = each_record.id;
  COMMIT;
END LOOP;

-- ✅ Batch or single transaction
BEGIN;
INSERT INTO target SELECT ... FROM source;
COMMIT;

-- ❌ Not handling errors → uncommitted transactions leak locks
BEGIN;
UPDATE ...;   -- error happens in application code
-- connection closes without COMMIT or ROLLBACK
-- PostgreSQL: connection close = auto-ROLLBACK ✅
-- Best: always explicitly COMMIT or ROLLBACK in error handlers
```

---

## 🔑 Key Takeaways

| Command | Purpose |
|---------|---------|
| `BEGIN` / `START TRANSACTION` | Start explicit transaction |
| `COMMIT` | Make all changes permanent |
| `ROLLBACK` | Undo all changes since BEGIN |
| `SAVEPOINT name` | Create a partial rollback point |
| `ROLLBACK TO SAVEPOINT name` | Undo back to savepoint |
| `RELEASE SAVEPOINT name` | Remove savepoint (keep changes) |

**ACID in one sentence:**
- **A**tomicity: all or nothing
- **C**onsistency: rules always maintained
- **I**solation: transactions don't see each other's in-progress work
- **D**urability: committed data survives crashes

**Best Practices:**
- Keep transactions as **short** as possible
- Open transaction close to the DML, commit immediately after
- Always handle errors with ROLLBACK in catch blocks
- Avoid transactions that span user input (user might never respond!)

---

**← Previous:** [25 — Cursors](./25-cursors.md)
**Next →** [27 — Isolation Levels](./27-isolation-levels.md)
