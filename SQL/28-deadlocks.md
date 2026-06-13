# 28 — Deadlocks

> **Goal:** Understand what deadlocks are, how to detect and prevent them, and how databases resolve them.

---

## 📖 What is a Deadlock?

A **deadlock** occurs when two or more transactions are **waiting for each other** to release locks, and none can proceed.

```
T1 holds Lock on Table A, waits for Table B
T2 holds Lock on Table B, waits for Table A

      T1 ──── holds lock on ──► Table A
      │                              │
      │                              │
   waits for                      T2 holds
      │                              │
      ▼                              ▼
   Table B ◄── holds lock on ─── T2
      │
      waits for
      │
      ▼
   Table A (held by T1!)

→ Circular wait! Neither can proceed → DEADLOCK!
```

---

## 🕐 Deadlock Example

```sql
-- Connection 1 (T1):
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;  -- locks row 1
-- (T2 runs here)
UPDATE accounts SET balance = balance + 100 WHERE id = 2;  -- WAITS for T2's lock on row 2
COMMIT;

-- Connection 2 (T2):
BEGIN;
UPDATE accounts SET balance = balance - 50 WHERE id = 2;   -- locks row 2
UPDATE accounts SET balance = balance + 50 WHERE id = 1;   -- WAITS for T1's lock on row 1
COMMIT;

-- DEADLOCK!
-- T1 waits for row 2 (held by T2)
-- T2 waits for row 1 (held by T1)
-- Database detects cycle → kills one transaction (the victim) with error
```

---

## 🔍 Deadlock Detection

### How Databases Detect Deadlocks

```
PostgreSQL / SQL Server periodically scan the lock graph:

Lock Wait Graph:
  T1 → waiting for lock held by T2
  T2 → waiting for lock held by T3
  T3 → waiting for lock held by T1   ← cycle detected!

→ Database selects a "victim" (usually shortest transaction)
→ Victim transaction gets ROLLBACK with error
→ Other transactions can proceed
```

### PostgreSQL Deadlock Error

```
ERROR:  deadlock detected
DETAIL: Process 12345 waits for ShareLock on transaction 67890;
        blocked by process 67890.
        Process 67890 waits for ShareLock on transaction 12345;
        blocked by process 12345.
HINT:   See server log for query details.
```

### SQL Server Deadlock Error

```
Msg 1205, Level 13, State 51, Line 1
Transaction (Process ID XX) was deadlocked on lock resources with
another process and has been chosen as the deadlock victim.
Rerun the transaction.
```

### Viewing Deadlocks (PostgreSQL)

```sql
-- Enable deadlock logging in postgresql.conf:
-- log_lock_waits = on
-- deadlock_timeout = 1s  (how long before checking for deadlock)

-- View recent deadlocks in log:
-- tail -f /var/log/postgresql/postgresql.log | grep deadlock

-- pg_stat_activity: see waiting transactions
SELECT pid, state, wait_event_type, wait_event, query
FROM pg_stat_activity
WHERE wait_event IS NOT NULL;

-- pg_locks: see held locks
SELECT locktype, relation::regclass, mode, granted, pid
FROM pg_locks
WHERE NOT granted;
```

---

## 🛡️ Deadlock Prevention

### 1. Access Resources in Consistent Order (Most Effective!)

Always lock tables/rows in the **same order** across all transactions.

```sql
-- ❌ Deadlock-prone: different order
-- T1: Lock accounts(id=1) then accounts(id=2)
-- T2: Lock accounts(id=2) then accounts(id=1)  ← opposite order!

-- ✅ Safe: consistent order (always smallest id first)
-- T1: Lock accounts(id=1) then accounts(id=2)
-- T2: Lock accounts(id=1) then accounts(id=2)  ← same order!

-- Enforced in code:
-- Sort the IDs before locking:
BEGIN;
SELECT * FROM accounts WHERE id IN (1, 2) ORDER BY id FOR UPDATE;
-- Locks id=1 first, then id=2 — always!
```

### 2. Keep Transactions Short

```sql
-- ❌ Long transaction holds locks for long time → more chance of deadlock
BEGIN;
  -- fetch data
  SELECT ...;
  -- application processing (2 seconds)
  -- ...
  UPDATE ...;  -- lock held for 2+ seconds!
COMMIT;

-- ✅ Do processing outside the transaction
-- Step 1: Fetch data (no transaction)
SELECT ...;
-- Step 2: Process in application code
-- Step 3: Short transaction for writes only
BEGIN;
  UPDATE ...;
COMMIT;  -- lock held for milliseconds
```

### 3. Use Lower Isolation Levels When Possible

```sql
-- SERIALIZABLE creates more locks → more deadlock potential
-- Use READ COMMITTED when sufficient — fewer locks
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

### 4. Use SELECT FOR UPDATE Wisely

```sql
-- Pessimistic locking: lock rows you intend to update
BEGIN;
SELECT * FROM accounts WHERE id = 1 FOR UPDATE;  -- lock row 1 immediately
-- Now no other transaction can lock row 1 until we commit
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
COMMIT;
```

### 5. Use Retry Logic in Application

Since deadlocks can't always be prevented, handle them gracefully:

```javascript
// Node.js example (pseudocode)
async function transfer(fromId, toId, amount, retries = 3) {
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      await db.begin();
      await db.query('UPDATE accounts SET balance = balance - $1 WHERE id = $2', [amount, fromId]);
      await db.query('UPDATE accounts SET balance = balance + $1 WHERE id = $2', [amount, toId]);
      await db.commit();
      return; // success!
    } catch (err) {
      await db.rollback();
      if (err.code === '40P01' && attempt < retries) {  // 40P01 = deadlock_detected (PostgreSQL)
        await sleep(100 * attempt); // exponential backoff
        continue;
      }
      throw err; // give up after retries
    }
  }
}
```

---

## 🚫 Deadlock Resolution

When a deadlock is detected:
1. Database picks a **victim** transaction (usually the one that has done less work)
2. Victim transaction is **automatically rolled back**
3. Victim receives an error code
4. Other transaction(s) proceed normally

```
Victim selection criteria (varies by RDBMS):
  PostgreSQL: lowest cost to rollback (usually the transaction that just started)
  SQL Server: configurable via DEADLOCK_PRIORITY
    SET DEADLOCK_PRIORITY LOW;    -- prefer this transaction to be victim
    SET DEADLOCK_PRIORITY HIGH;   -- prefer other transaction to be victim
    SET DEADLOCK_PRIORITY NORMAL; -- default
```

---

## 🔑 Key Takeaways

| Concept | Detail |
|---------|--------|
| Deadlock | Circular lock wait — no one can proceed |
| Detection | Database scans lock graph periodically for cycles |
| Victim | The transaction that gets auto-rolled back |
| #1 Prevention | Always acquire locks in the same order |
| #2 Prevention | Keep transactions short |
| #3 Prevention | Retry with backoff in application code |
| PostgreSQL error | `40P01` — deadlock_detected |
| SQL Server error | Error 1205 |

**The Golden Rule:** If all transactions acquire locks in the **same order** (e.g., always lock smaller ID first), deadlocks in that code path are **impossible**.

---

**← Previous:** [27 — Isolation Levels](./27-isolation-levels.md)
**Next →** [29 — Normalization](./29-normalization.md)
