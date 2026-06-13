# 27 — Isolation Levels

> **Goal:** Understand how databases control concurrent access and the tradeoffs between consistency and performance.

---

## 📖 Why Isolation Levels?

When multiple transactions run **simultaneously**, they can interfere with each other. Isolation levels define **how much** one transaction can see of another's in-progress changes.

```
Higher Isolation → More consistency, less concurrency (slower)
Lower Isolation  → More concurrency (faster), more anomalies possible
```

---

## 🐛 Concurrency Problems

### 1. Dirty Read

Reading **uncommitted** data from another transaction.

```
T1:                          T2:
BEGIN;
UPDATE salary = 90000        
(not committed yet)          BEGIN;
                             SELECT salary;  → sees 90000! (dirty!)
ROLLBACK;                    
(salary reverts to 75000)    Commits with wrong data!
```

### 2. Non-Repeatable Read

Reading the **same row twice** and getting **different values** because another transaction modified it between reads.

```
T1:                          T2:
BEGIN;
SELECT salary;  → 75000
                             BEGIN;
                             UPDATE salary = 90000;
                             COMMIT;
SELECT salary;  → 90000!    ← Changed! Different value in same transaction!
COMMIT;
```

### 3. Phantom Read

A re-executed query returns **different rows** because another transaction inserted or deleted rows.

```
T1:                          T2:
BEGIN;
SELECT COUNT(*) WHERE dept = 'Eng'
  → returns 4 rows
                             BEGIN;
                             INSERT INTO employees (dept='Eng') ...;
                             COMMIT;
SELECT COUNT(*) WHERE dept = 'Eng'
  → returns 5 rows!  ← phantom row appeared!
COMMIT;
```

---

## 🔒 Isolation Levels

### ANSI/ISO Standard Levels

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|----------------|-----------|--------------------|----|
| Read Uncommitted | ✅ Possible | ✅ Possible | ✅ Possible |
| Read Committed | ❌ Prevented | ✅ Possible | ✅ Possible |
| Repeatable Read | ❌ Prevented | ❌ Prevented | ✅ Possible |
| Serializable | ❌ Prevented | ❌ Prevented | ❌ Prevented |

---

### 1. Read Uncommitted (Lowest Isolation)

Can read **uncommitted changes** from other transactions.

```sql
-- PostgreSQL
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
-- (PostgreSQL treats this as READ COMMITTED internally)

-- SQL Server
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
BEGIN TRANSACTION;
SELECT * FROM orders;   -- may read dirty data!
COMMIT;

-- OR: table hint
SELECT * FROM orders WITH (NOLOCK);  -- SQL Server dirty read hint
```

**Use case:** Reports where slightly stale/dirty data is acceptable. Rarely used.

---

### 2. Read Committed (Default in PostgreSQL, SQL Server, Oracle)

Only reads **committed** data. Each statement gets a fresh snapshot.

```sql
-- PostgreSQL (default)
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

BEGIN;
SELECT salary FROM employees WHERE id = 1;  -- reads committed value at this moment
-- ... another transaction updates and commits salary ...
SELECT salary FROM employees WHERE id = 1;  -- may return NEW committed value
COMMIT;
```

**Prevents:** Dirty reads
**Allows:** Non-repeatable reads, phantom reads

---

### 3. Repeatable Read (Default in MySQL/InnoDB)

Ensures that **if you read a row, you'll see the same values** if you read it again — regardless of other committed changes.

```sql
-- PostgreSQL
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

BEGIN;
SELECT salary FROM employees WHERE id = 1;  -- 75000
-- ... another transaction updates salary to 90000 and commits ...
SELECT salary FROM employees WHERE id = 1;  -- still 75000! (snapshot)
COMMIT;
```

**Prevents:** Dirty reads, non-repeatable reads
**Allows:** Phantom reads (in standard SQL, but PostgreSQL prevents them too in its MVCC implementation)

---

### 4. Serializable (Highest Isolation)

Transactions execute as if they were **run serially** (one after another). Prevents all anomalies.

```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

BEGIN;
SELECT COUNT(*) FROM employees WHERE dept = 'Engineering';  -- 4
-- another transaction cannot insert into Engineering until we commit!
-- (predicate lock on dept='Engineering')
COMMIT;
```

**Prevents:** All anomalies (dirty reads, non-repeatable reads, phantom reads)
**Cost:** Lowest concurrency — transactions may need to retry (serialization failure error)

---

### 5. Snapshot Isolation (PostgreSQL MVCC / SQL Server)

Not a standard ANSI level, but widely used. Each transaction sees a **consistent snapshot** of the database at a point in time.

```sql
-- SQL Server: Snapshot isolation
ALTER DATABASE mydb SET ALLOW_SNAPSHOT_ISOLATION ON;

SET TRANSACTION ISOLATION LEVEL SNAPSHOT;
BEGIN TRANSACTION;
SELECT * FROM accounts;   -- consistent snapshot of when TX started
COMMIT;

-- PostgreSQL: MVCC is the basis for READ COMMITTED and REPEATABLE READ
-- PostgreSQL's REPEATABLE READ is essentially Snapshot Isolation
```

**Benefits:** Readers don't block writers, writers don't block readers. No dirty reads.
**Uses:** PostgreSQL (all levels use MVCC), SQL Server SNAPSHOT level.

---

## 🔧 Setting Isolation Levels

```sql
-- PostgreSQL
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- or for all future transactions in session:
SET default_transaction_isolation TO 'repeatable read';

-- SQL Server
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- MySQL
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
SET GLOBAL  TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Per transaction (MySQL / SQL Server):
START TRANSACTION WITH CONSISTENT SNAPSHOT;  -- MySQL
```

---

## 🗺️ How MVCC Works (PostgreSQL)

**MVCC (Multi-Version Concurrency Control):** Instead of locking, keep multiple versions of each row.

```
accounts table row for id=1:

Version 1: balance=1000, created by TXN_100
Version 2: balance=500,  created by TXN_200  (current)

Transaction TXN_150 (started before TXN_200):
  → sees Version 1 (balance=1000) — it was committed before TXN_150 started

Transaction TXN_250 (started after TXN_200):
  → sees Version 2 (balance=500)  — TXN_200 was committed before TXN_250 started

Result: Readers and writers don't block each other!
Old versions are cleaned up by VACUUM (background process).
```

---

## 📊 Choosing an Isolation Level

```
Need high throughput, can tolerate stale reads?
  → Read Committed (default for most RDBMS)

Need consistent reports within a single transaction?
  → Repeatable Read or Snapshot Isolation

Need absolute correctness (financial systems, inventory)?
  → Serializable (but handle serialization failures with retries)

Never use Read Uncommitted unless you're absolutely sure
dirty reads are acceptable (very rare).
```

---

## 🔑 Key Takeaways

| Level | Prevents | Performance | Use Case |
|-------|---------|-------------|----------|
| Read Uncommitted | Nothing | Highest | Almost never |
| Read Committed | Dirty reads | High | Default — web apps, OLTP |
| Repeatable Read | Dirty + non-repeatable | Medium | Consistent read transactions |
| Serializable | All anomalies | Lowest | Critical financial operations |
| Snapshot | Dirty + non-repeatable | High | PostgreSQL MVCC; SQL Server SNAPSHOT |

**MVCC**: PostgreSQL and modern RDBMS use multi-version concurrency — readers don't block writers, enabling high concurrency without sacrificing consistency.

---

**← Previous:** [26 — Transactions](./26-transactions.md)
**Next →** [28 — Deadlocks](./28-deadlocks.md)
