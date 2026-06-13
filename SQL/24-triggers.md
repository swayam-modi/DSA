# 24 — Triggers

> **Goal:** Automatically execute SQL logic in response to table events (INSERT, UPDATE, DELETE).

---

## 📖 What is a Trigger?

A **trigger** is a stored procedure that **automatically fires** when a specified event occurs on a table or view.

```
Event: INSERT into orders
         │
         ▼
    ┌─────────────┐
    │   TRIGGER   │ ← fires automatically!
    │             │
    │  - validate │
    │  - audit    │
    │  - cascade  │
    └─────────────┘
         │
         ▼
    Order inserted (or rejected)
```

---

## 🔧 Trigger Components

```
CREATE TRIGGER trigger_name
{BEFORE | AFTER | INSTEAD OF}    ← when it fires
{INSERT | UPDATE | DELETE}       ← on what event
[OR INSERT | OR UPDATE | ...]    ← multiple events
ON table_name                    ← which table
[FOR EACH ROW | FOR EACH STATEMENT]
EXECUTE FUNCTION trigger_function();
```

---

## 📝 DML Triggers

### AFTER INSERT Trigger — Audit Log (PostgreSQL)

```sql
-- Step 1: Create trigger function
CREATE OR REPLACE FUNCTION log_employee_insert()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    -- NEW = the newly inserted row
    INSERT INTO audit_log (table_name, action, record_id, data, changed_at, changed_by)
    VALUES (
        'employees',
        'INSERT',
        NEW.id,
        row_to_json(NEW),
        NOW(),
        current_user
    );
    RETURN NEW;
END;
$$;

-- Step 2: Attach trigger to table
CREATE TRIGGER trg_employee_insert_audit
AFTER INSERT ON employees
FOR EACH ROW EXECUTE FUNCTION log_employee_insert();

-- Test
INSERT INTO employees (name, salary, dept) VALUES ('Alice', 90000, 'Engineering');
-- Automatically logs to audit_log!
```

### BEFORE INSERT Trigger — Validation + Auto-fill

```sql
CREATE OR REPLACE FUNCTION before_employee_insert()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    -- Auto-normalize email
    NEW.email := LOWER(TRIM(NEW.email));

    -- Auto-set hire_date if not provided
    IF NEW.hire_date IS NULL THEN
        NEW.hire_date := CURRENT_DATE;
    END IF;

    -- Validate salary
    IF NEW.salary < 0 THEN
        RAISE EXCEPTION 'Salary cannot be negative: %', NEW.salary;
    END IF;

    -- Return modified row
    RETURN NEW;   -- MUST RETURN NEW in BEFORE trigger!
END;
$$;

CREATE TRIGGER trg_before_employee_insert
BEFORE INSERT ON employees
FOR EACH ROW EXECUTE FUNCTION before_employee_insert();
```

### AFTER UPDATE Trigger — Track Changes

```sql
CREATE OR REPLACE FUNCTION log_salary_change()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    -- OLD = row before update, NEW = row after update
    IF OLD.salary <> NEW.salary THEN
        INSERT INTO salary_history (employee_id, old_salary, new_salary, changed_at)
        VALUES (NEW.id, OLD.salary, NEW.salary, NOW());
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_salary_change
AFTER UPDATE ON employees
FOR EACH ROW
WHEN (OLD.salary IS DISTINCT FROM NEW.salary)  -- only fire if salary changed
EXECUTE FUNCTION log_salary_change();
```

### AFTER DELETE Trigger — Soft Delete / Archive

```sql
CREATE OR REPLACE FUNCTION archive_deleted_employee()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    -- OLD = the deleted row
    INSERT INTO employees_archive
    SELECT OLD.*, NOW() AS deleted_at;

    RETURN OLD;
END;
$$;

CREATE TRIGGER trg_employee_delete_archive
AFTER DELETE ON employees
FOR EACH ROW EXECUTE FUNCTION archive_deleted_employee();

-- Test
DELETE FROM employees WHERE id = 5;
-- Row is deleted from employees AND archived in employees_archive
SELECT * FROM employees_archive;
```

---

## 🔄 INSTEAD OF Triggers (Views)

Fire INSTEAD of the original DML operation. Used for updatable views.

```sql
-- A view that joins employees and departments
CREATE VIEW employee_dept_view AS
SELECT e.id, e.name, e.salary, d.name AS dept_name
FROM employees e
JOIN departments d ON e.dept_id = d.id;

-- Allow UPDATE through this view
CREATE OR REPLACE FUNCTION update_through_view()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE v_dept_id INT;
BEGIN
    -- Find department ID from name
    SELECT id INTO v_dept_id FROM departments WHERE name = NEW.dept_name;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Department % not found', NEW.dept_name;
    END IF;

    -- Update underlying table
    UPDATE employees
    SET name = NEW.name, salary = NEW.salary, dept_id = v_dept_id
    WHERE id = NEW.id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_update_employee_view
INSTEAD OF UPDATE ON employee_dept_view
FOR EACH ROW EXECUTE FUNCTION update_through_view();
```

---

## 📋 SQL Server Triggers (T-SQL)

```sql
-- SQL Server: combined AFTER trigger for INSERT and UPDATE
CREATE OR ALTER TRIGGER trg_employees_audit
ON employees
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Handle INSERTS
    IF EXISTS(SELECT 1 FROM inserted) AND NOT EXISTS(SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (action, employee_id, new_data, changed_at)
        SELECT 'INSERT', id, (SELECT * FROM inserted FOR JSON AUTO), GETDATE()
        FROM inserted;
    END;

    -- Handle UPDATES
    IF EXISTS(SELECT 1 FROM inserted) AND EXISTS(SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (action, employee_id, old_data, new_data, changed_at)
        SELECT 'UPDATE', i.id,
               (SELECT * FROM deleted d WHERE d.id = i.id FOR JSON AUTO),
               (SELECT * FROM inserted ii WHERE ii.id = i.id FOR JSON AUTO),
               GETDATE()
        FROM inserted i;
    END;

    -- Handle DELETES
    IF NOT EXISTS(SELECT 1 FROM inserted) AND EXISTS(SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (action, employee_id, old_data, changed_at)
        SELECT 'DELETE', id, (SELECT * FROM deleted FOR JSON AUTO), GETDATE()
        FROM deleted;
    END;
END;
```

---

## ⏱️ Trigger Timing Summary

```
BEFORE INSERT  → modify NEW data, validate, reject by returning NULL
AFTER INSERT   → audit, update derived tables, send notifications
BEFORE UPDATE  → modify NEW data, validate changes
AFTER UPDATE   → audit OLD vs NEW, maintain summary tables
BEFORE DELETE  → validate deletion is allowed
AFTER DELETE   → archive OLD data, cleanup related records
INSTEAD OF     → handle DML through views (PostgreSQL, SQL Server)
```

### NEW and OLD in Trigger Context

```
PostgreSQL:    NEW = new row (INSERT/UPDATE), OLD = old row (UPDATE/DELETE)
SQL Server:    inserted table = new rows, deleted table = old rows
MySQL:         NEW.col = new value, OLD.col = old value
```

---

## ⚠️ Trigger Best Practices

```
✅ DO:
  - Keep trigger logic simple and fast (runs on EVERY affected row)
  - Use FOR EACH ROW only when you need row-level access
  - Use FOR EACH STATEMENT for bulk efficiency when row data not needed
  - Document why the trigger exists in a comment
  - Test triggers under concurrent load

❌ DON'T:
  - Create complex business logic in triggers (hard to debug, test)
  - Create trigger chains (trigger A fires trigger B which fires trigger C)
  - Raise errors in AFTER triggers for validation (use BEFORE instead)
  - Query large tables inside row-level triggers (N+1 problem!)
  - Use triggers for things better done in application code
```

---

## 🗑️ Managing Triggers

```sql
-- PostgreSQL: disable/enable trigger
ALTER TABLE employees DISABLE TRIGGER trg_salary_change;
ALTER TABLE employees ENABLE  TRIGGER trg_salary_change;
ALTER TABLE employees DISABLE TRIGGER ALL;    -- all triggers on table

-- PostgreSQL: drop trigger
DROP TRIGGER trg_salary_change ON employees;
DROP TRIGGER IF EXISTS trg_salary_change ON employees;

-- PostgreSQL: list triggers
SELECT trigger_name, event_manipulation, event_object_table
FROM information_schema.triggers
WHERE event_object_schema = 'public';

-- SQL Server: disable/enable
DISABLE TRIGGER trg_employees_audit ON employees;
ENABLE  TRIGGER trg_employees_audit ON employees;

-- SQL Server: drop
DROP TRIGGER IF EXISTS trg_employees_audit;
```

---

## 🔑 Key Takeaways

| Trigger Type | When It Fires | Use Case |
|-------------|--------------|---------|
| BEFORE INSERT | Before row inserted | Validate, normalize input |
| AFTER INSERT | After row inserted | Audit, update aggregates |
| BEFORE UPDATE | Before row updated | Validate change |
| AFTER UPDATE | After row updated | Log history, propagate changes |
| BEFORE DELETE | Before row deleted | Check if safe to delete |
| AFTER DELETE | After row deleted | Archive, cleanup |
| INSTEAD OF | Replace the operation | Complex view updates |

---

**← Previous:** [23 — User Defined Functions](./23-user-defined-functions.md)
**Next →** [25 — Cursors](./25-cursors.md)
