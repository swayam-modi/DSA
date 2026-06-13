# 25 — Cursors

> **Goal:** Use cursors to process query results row-by-row when set-based operations are not feasible.

---

## 📖 What is a Cursor?

A **cursor** is a database object that allows you to **process query results one row at a time** rather than as a set.

```
Set-based (preferred):            Cursor-based (row-by-row):
──────────────────────────────    ──────────────────────────────
UPDATE orders                     OPEN cursor on orders;
SET tax = total * 0.10            LOOP:
WHERE status = 'pending';           FETCH next row
                                    UPDATE this row's tax
1 operation, all rows at once       NEXT row
                                  CLOSE cursor;
```

> ⚠️ **Cursors are slow.** Always try set-based operations first. Use cursors only when row-by-row processing is truly necessary (e.g., calling a stored procedure for each row, or complex per-row logic that can't be set-based).

---

## 🔄 Cursor Lifecycle

```
DECLARE → OPEN → FETCH → (process) → repeat FETCH until done → CLOSE → DEALLOCATE
```

### PostgreSQL Cursor

```sql
DO $$
DECLARE
    -- 1. DECLARE the cursor
    emp_cursor CURSOR FOR
        SELECT id, name, salary FROM employees WHERE is_active = TRUE;

    v_id     INT;
    v_name   VARCHAR;
    v_salary DECIMAL;
BEGIN
    -- 2. OPEN the cursor
    OPEN emp_cursor;

    LOOP
        -- 3. FETCH next row
        FETCH NEXT FROM emp_cursor INTO v_id, v_name, v_salary;

        -- Exit loop when no more rows
        EXIT WHEN NOT FOUND;

        -- Process each row
        RAISE NOTICE 'Employee: %, Salary: %', v_name, v_salary;

        -- Could do complex per-row logic here
    END LOOP;

    -- 4. CLOSE the cursor
    CLOSE emp_cursor;
END;
$$ LANGUAGE plpgsql;
```

### SQL Server Cursor

```sql
DECLARE
    @EmployeeId   INT,
    @EmployeeName VARCHAR(100),
    @Salary       DECIMAL(10,2);

-- 1. DECLARE cursor
DECLARE emp_cursor CURSOR
    LOCAL FAST_FORWARD    -- read-only, forward-only (fastest)
FOR
    SELECT id, name, salary FROM employees WHERE is_active = 1;

-- 2. OPEN
OPEN emp_cursor;

-- 3. FETCH first row
FETCH NEXT FROM emp_cursor INTO @EmployeeId, @EmployeeName, @Salary;

-- Loop while rows remain
WHILE @@FETCH_STATUS = 0
BEGIN
    PRINT 'Employee: ' + @EmployeeName + ', Salary: ' + CAST(@Salary AS VARCHAR);

    -- 3. FETCH next row
    FETCH NEXT FROM emp_cursor INTO @EmployeeId, @EmployeeName, @Salary;
END;

-- 4. CLOSE
CLOSE emp_cursor;

-- 5. DEALLOCATE (free resources)
DEALLOCATE emp_cursor;
```

### MySQL Cursor (inside Stored Procedure)

```sql
DELIMITER //
CREATE PROCEDURE process_employees()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_id   INT;
    DECLARE v_name VARCHAR(100);
    DECLARE v_salary DECIMAL(10,2);

    -- DECLARE cursor
    DECLARE emp_cursor CURSOR FOR
        SELECT id, name, salary FROM employees WHERE is_active = 1;

    -- Handler for when no more rows
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- OPEN
    OPEN emp_cursor;

    read_loop: LOOP
        FETCH emp_cursor INTO v_id, v_name, v_salary;

        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Process row
        SELECT CONCAT('Processing: ', v_name, ' - $', v_salary);
    END LOOP;

    -- CLOSE
    CLOSE emp_cursor;
END;
//
DELIMITER ;

CALL process_employees();
```

---

## 📋 Cursor Types (SQL Server)

| Type | Description |
|------|-------------|
| `FORWARD_ONLY` (default) | Can only move forward (FETCH NEXT) |
| `SCROLL` | Can move in any direction (NEXT, PRIOR, FIRST, LAST, ABSOLUTE n, RELATIVE n) |
| `FAST_FORWARD` | Optimized forward-only read-only cursor (fastest) |
| `STATIC` | Copy of data at OPEN time (snapshot; not affected by changes) |
| `DYNAMIC` | Sees all changes to underlying data |
| `KEYSET` | Keys captured at OPEN; data refreshed on FETCH |

```sql
-- SCROLL cursor — can move in any direction
DECLARE scroll_cursor CURSOR SCROLL FOR
SELECT name, salary FROM employees ORDER BY salary;

OPEN scroll_cursor;
FETCH LAST    FROM scroll_cursor;    -- go to last row
FETCH PRIOR   FROM scroll_cursor;    -- go to previous row
FETCH FIRST   FROM scroll_cursor;    -- go to first row
FETCH ABSOLUTE 3 FROM scroll_cursor; -- go to row 3
FETCH RELATIVE 2 FROM scroll_cursor; -- move forward 2 rows
CLOSE scroll_cursor;
DEALLOCATE scroll_cursor;
```

---

## ⚠️ Cursor Performance Issues

```
Why cursors are slow:
  • Row-by-row = O(n) database roundtrips within the procedure
  • Each FETCH acquires/releases locks
  • No vectorized execution (no batch optimization)
  • SQL Server: cursor loops can be 100x slower than set-based queries

Example: Apply 5% raise to 100,000 employees
  Cursor:      1 UPDATE per row = 100,000 operations
  Set-based:   1 UPDATE for all = 1 operation

Cursor: 30 seconds
Set-based: 0.3 seconds  → 100x faster!
```

---

## ✅ Set-Based Alternatives

Always try to replace cursors with:

```sql
-- ❌ Cursor: apply 5% raise department by department
-- (requires calling external API per dept — forced row-by-row)

-- ✅ Set-based: apply 5% raise to all Engineering
UPDATE employees
SET salary = salary * 1.05
WHERE dept = 'Engineering';

-- ❌ Cursor: generate row number per group
-- ✅ Window function:
SELECT id, name, ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary) AS rn
FROM employees;

-- ❌ Cursor: accumulate running total
-- ✅ Window function:
SELECT id, salary, SUM(salary) OVER (ORDER BY hire_date) AS running_total
FROM employees;

-- ❌ Cursor: process each employee's orders
-- ✅ JOIN + aggregate:
SELECT e.name, COUNT(o.id) AS order_count, SUM(o.total) AS total_spent
FROM employees e
LEFT JOIN orders o ON e.id = o.employee_id
GROUP BY e.id, e.name;
```

### When Cursors Are Acceptable

```
Acceptable cursor use cases:
✅ Calling a stored procedure for each row (can't do this set-based)
✅ Sending emails/notifications per row (external side effects)
✅ Complex row-by-row sequential processing (e.g., running balance with complex rules)
✅ DDL operations per table (e.g., rebuild each index in a maintenance script)
✅ Very small result sets where performance doesn't matter
```

---

## 🔑 Key Takeaways

| Concept | Detail |
|---------|--------|
| `DECLARE CURSOR` | Define the cursor and its query |
| `OPEN` | Execute the query, position before first row |
| `FETCH NEXT` | Get the next row |
| `@@FETCH_STATUS` / `NOT FOUND` | Check if fetch succeeded |
| `CLOSE` | Release result set (but cursor definition remains) |
| `DEALLOCATE` | Free all cursor resources (SQL Server) |
| `FAST_FORWARD` | Fastest cursor type (SQL Server) |
| Set-based | Always prefer over cursors when possible |

**The rule:** If you're using a cursor, ask yourself — *"Can I solve this with JOINs, window functions, or CTEs?"* Usually the answer is yes.

---

**← Previous:** [24 — Triggers](./24-triggers.md)
**Next →** [26 — Transactions](./26-transactions.md)
