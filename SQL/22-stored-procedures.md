# 22 — Stored Procedures

> **Goal:** Encapsulate reusable business logic in the database using stored procedures.

---

## 📖 What is a Stored Procedure?

A **stored procedure** is a named, compiled block of SQL (+ procedural logic) stored in the database. It's called by name and can accept parameters, execute multiple statements, and return results.

```
Without Stored Procedure:              With Stored Procedure:
────────────────────────────────────   ────────────────────────────────────
Application sends 5 SQL statements     Application calls:
over the network each time:              CALL process_order(order_id, user_id);
  INSERT INTO orders...
  UPDATE inventory...                  DB executes all logic internally
  INSERT INTO audit_log...             (1 network round trip)
  SELECT ...
  UPDATE customer_stats...
```

**Benefits:**
- **Performance:** Pre-compiled, one network call
- **Security:** Grant EXECUTE without exposing tables
- **Reusability:** Called from multiple applications
- **Consistency:** Business logic in one place
- **Reduced network traffic:** Multiple operations in one call

---

## 🏗️ Creating Procedures

### PostgreSQL (PL/pgSQL)

```sql
CREATE OR REPLACE PROCEDURE update_employee_salary(
    p_employee_id INT,
    p_new_salary  DECIMAL
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Validate
    IF p_new_salary <= 0 THEN
        RAISE EXCEPTION 'Salary must be positive, got: %', p_new_salary;
    END IF;

    -- Update
    UPDATE employees
    SET salary     = p_new_salary,
        updated_at = NOW()
    WHERE id = p_employee_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Employee % not found', p_employee_id;
    END IF;

    RAISE NOTICE 'Salary updated for employee %', p_employee_id;
END;
$$;

-- Call the procedure
CALL update_employee_salary(1, 95000.00);
```

### SQL Server (T-SQL)

```sql
CREATE OR ALTER PROCEDURE usp_UpdateEmployeeSalary
    @EmployeeId  INT,
    @NewSalary   DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;    -- suppress row count messages

    IF @NewSalary <= 0
    BEGIN
        RAISERROR('Salary must be positive.', 16, 1);
        RETURN;
    END;

    UPDATE employees
    SET salary     = @NewSalary,
        updated_at = GETDATE()
    WHERE id = @EmployeeId;

    IF @@ROWCOUNT = 0
        RAISERROR('Employee not found.', 16, 1);
END;

-- Execute
EXEC usp_UpdateEmployeeSalary @EmployeeId = 1, @NewSalary = 95000;
```

### MySQL

```sql
DELIMITER //
CREATE PROCEDURE update_employee_salary(
    IN  p_employee_id INT,
    IN  p_new_salary  DECIMAL(10,2)
)
BEGIN
    DECLARE v_count INT;

    IF p_new_salary <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Salary must be positive';
    END IF;

    SELECT COUNT(*) INTO v_count FROM employees WHERE id = p_employee_id;

    IF v_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Employee not found';
    END IF;

    UPDATE employees
    SET salary = p_new_salary,
        updated_at = NOW()
    WHERE id = p_employee_id;
END;
//
DELIMITER ;

-- Call
CALL update_employee_salary(1, 95000.00);
```

---

## 📥 Parameters

### Input Parameters

Values passed IN to the procedure.

```sql
CREATE OR REPLACE PROCEDURE hire_employee(
    p_first_name VARCHAR(50),
    p_last_name  VARCHAR(50),
    p_email      VARCHAR(100),
    p_salary     DECIMAL(10,2),
    p_dept_id    INT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO employees (first_name, last_name, email, salary, dept_id, hire_date)
    VALUES (p_first_name, p_last_name, p_email, p_salary, p_dept_id, CURRENT_DATE);
    RAISE NOTICE 'Employee hired: % %', p_first_name, p_last_name;
END;
$$;

CALL hire_employee('John', 'Doe', 'john@company.com', 75000, 10);
```

### Output Parameters

Return values back to the caller.

```sql
-- PostgreSQL: use INOUT or return via function
CREATE OR REPLACE PROCEDURE get_employee_info(
    IN  p_id        INT,
    OUT p_name      TEXT,
    OUT p_salary    DECIMAL,
    OUT p_dept      TEXT
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT name, salary, dept
    INTO p_name, p_salary, p_dept
    FROM employees
    WHERE id = p_id;
END;
$$;

-- Call with output
DO $$
DECLARE v_name TEXT; v_salary DECIMAL; v_dept TEXT;
BEGIN
    CALL get_employee_info(1, v_name, v_salary, v_dept);
    RAISE NOTICE '%, %, %', v_name, v_salary, v_dept;
END;
$$;
```

```sql
-- SQL Server OUTPUT parameters
CREATE OR ALTER PROCEDURE usp_GetEmployeeInfo
    @EmployeeId  INT,
    @Name        VARCHAR(100) OUTPUT,
    @Salary      DECIMAL(10,2) OUTPUT
AS
BEGIN
    SELECT @Name = name, @Salary = salary
    FROM employees WHERE id = @EmployeeId;
END;

-- Call
DECLARE @eName VARCHAR(100), @eSalary DECIMAL(10,2);
EXEC usp_GetEmployeeInfo 1, @eName OUTPUT, @eSalary OUTPUT;
PRINT @eName + ' earns ' + CAST(@eSalary AS VARCHAR);
```

---

## 📊 Returning Result Sets

```sql
-- PostgreSQL: RETURNS TABLE via a FUNCTION (not PROCEDURE)
CREATE OR REPLACE FUNCTION get_dept_employees(p_dept VARCHAR)
RETURNS TABLE(id INT, name TEXT, salary DECIMAL)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT e.id, e.name, e.salary
    FROM employees e
    WHERE e.dept = p_dept
    ORDER BY e.salary DESC;
END;
$$;

-- Call
SELECT * FROM get_dept_employees('Engineering');
```

```sql
-- SQL Server: just SELECT inside procedure
CREATE OR ALTER PROCEDURE usp_GetDeptEmployees
    @Dept VARCHAR(50)
AS
BEGIN
    SELECT id, name, salary
    FROM employees
    WHERE dept = @Dept
    ORDER BY salary DESC;
END;

EXEC usp_GetDeptEmployees 'Engineering';
```

---

## 🔴 Exception Handling

### PostgreSQL (TRY/CATCH via EXCEPTION block)

```sql
CREATE OR REPLACE PROCEDURE safe_transfer(
    p_from_account INT,
    p_to_account   INT,
    p_amount       DECIMAL
)
LANGUAGE plpgsql AS $$
DECLARE
    v_balance DECIMAL;
BEGIN
    -- Check balance
    SELECT balance INTO v_balance
    FROM accounts WHERE id = p_from_account;

    IF v_balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient funds. Balance: %, Requested: %',
            v_balance, p_amount;
    END IF;

    -- Perform transfer
    UPDATE accounts SET balance = balance - p_amount WHERE id = p_from_account;
    UPDATE accounts SET balance = balance + p_amount WHERE id = p_to_account;

    RAISE NOTICE 'Transfer of % completed', p_amount;

EXCEPTION
    WHEN OTHERS THEN
        -- Log the error
        INSERT INTO error_log (proc_name, error_msg, created_at)
        VALUES ('safe_transfer', SQLERRM, NOW());
        -- Re-raise
        RAISE;
END;
$$;
```

### SQL Server (TRY...CATCH)

```sql
CREATE OR ALTER PROCEDURE usp_SafeTransfer
    @FromAccount INT,
    @ToAccount   INT,
    @Amount      DECIMAL(15,2)
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE accounts SET balance = balance - @Amount WHERE id = @FromAccount;
        UPDATE accounts SET balance = balance + @Amount WHERE id = @ToAccount;

        COMMIT TRANSACTION;
        PRINT 'Transfer completed';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        INSERT INTO error_log (proc_name, error_number, error_message, created_at)
        VALUES ('usp_SafeTransfer', ERROR_NUMBER(), ERROR_MESSAGE(), GETDATE());
        THROW;   -- re-raise the error
    END CATCH;
END;
```

---

## 🔒 Security with EXECUTE AS / SECURITY DEFINER

```sql
-- PostgreSQL: SECURITY DEFINER
-- Procedure runs with the OWNER's privileges, not the caller's
CREATE OR REPLACE PROCEDURE adjust_salary(p_emp_id INT, p_amount DECIMAL)
LANGUAGE plpgsql
SECURITY DEFINER    -- runs as procedure owner (e.g. admin)
AS $$
BEGIN
    UPDATE employees SET salary = salary + p_amount WHERE id = p_emp_id;
END;
$$;

-- SQL Server: WITH ENCRYPTION (hides source code)
CREATE OR ALTER PROCEDURE usp_AdjustSalary
WITH ENCRYPTION
AS
BEGIN
    -- Source code hidden from sys.sql_modules
END;
```

---

## 🔧 Managing Procedures

```sql
-- List procedures (PostgreSQL)
SELECT routine_name, routine_type
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_type = 'PROCEDURE';

-- View procedure source (PostgreSQL)
SELECT pg_get_functiondef('update_employee_salary'::regproc);

-- Drop procedure
DROP PROCEDURE update_employee_salary(INT, DECIMAL);
DROP PROCEDURE IF EXISTS update_employee_salary(INT, DECIMAL);

-- SQL Server: view procedure definition
EXEC sp_helptext 'usp_UpdateEmployeeSalary';
```

---

## ✅ Best Practices

```
1. Use meaningful names: usp_  prefix (SQL Server), sp_  (MySQL), or descriptive names
2. Always validate input parameters before using them
3. Use transactions for multi-statement operations
4. Handle exceptions and log errors
5. Use SET NOCOUNT ON (SQL Server) to suppress row count messages
6. Keep procedures focused — one procedure, one responsibility
7. Avoid SELECT * inside procedures
8. Document with comments (purpose, parameters, return values)
9. Use SECURITY DEFINER carefully — can be a privilege escalation vector
10. Test with edge cases: NULL inputs, empty results, boundary values
```

---

## 🔑 Key Takeaways

| Concept | Detail |
|---------|--------|
| Stored Procedure | Named, reusable block of SQL + procedural logic |
| CREATE OR REPLACE | Replaces if exists, creates if not |
| IN parameter | Input value to procedure |
| OUT parameter | Return value from procedure |
| RAISE EXCEPTION | Signal an error (PostgreSQL) |
| TRY/CATCH | Exception handling block |
| SECURITY DEFINER | Runs with owner's privileges |
| CALL | Execute a procedure |

---

**← Previous:** [21 — JSON Processing](./21-json-processing.md)
**Next →** [23 — User Defined Functions](./23-user-defined-functions.md)
