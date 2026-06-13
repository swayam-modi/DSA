# 23 — User Defined Functions (UDF)

> **Goal:** Create reusable functions that return scalar values or table results, usable directly in SQL queries.

---

## 📖 Functions vs Procedures

| Feature | Function | Stored Procedure |
|---------|----------|-----------------|
| Returns value | ✅ Must | Optional (OUT params) |
| Used in SELECT | ✅ Yes | ❌ No |
| DML inside | Limited* | ✅ Yes |
| CALL/EXEC | ❌ No | ✅ Yes |
| Used in WHERE/JOIN | ✅ Yes | ❌ No |
| Transaction control | ❌ No (PostgreSQL) | ✅ Yes |

> * PostgreSQL allows DML in functions but has limitations

---

## 🔢 Scalar Functions

Return a **single value**. Can be used anywhere an expression is valid.

### PostgreSQL

```sql
-- Simple scalar function
CREATE OR REPLACE FUNCTION calculate_tax(p_salary DECIMAL)
RETURNS DECIMAL
LANGUAGE plpgsql
AS $$
DECLARE
    v_tax DECIMAL;
BEGIN
    v_tax := CASE
        WHEN p_salary > 100000 THEN p_salary * 0.30
        WHEN p_salary > 60000  THEN p_salary * 0.20
        WHEN p_salary > 30000  THEN p_salary * 0.10
        ELSE 0
    END;
    RETURN v_tax;
END;
$$;

-- Use in SELECT
SELECT
    name,
    salary,
    calculate_tax(salary) AS tax,
    salary - calculate_tax(salary) AS take_home
FROM employees;

-- Use in WHERE
SELECT name, salary
FROM employees
WHERE salary - calculate_tax(salary) > 60000;
```

### SQL Server

```sql
CREATE OR ALTER FUNCTION dbo.fn_CalculateTax (@Salary DECIMAL(10,2))
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @Tax DECIMAL(10,2);
    SET @Tax = CASE
        WHEN @Salary > 100000 THEN @Salary * 0.30
        WHEN @Salary > 60000  THEN @Salary * 0.20
        WHEN @Salary > 30000  THEN @Salary * 0.10
        ELSE 0
    END;
    RETURN @Tax;
END;

-- Use in query
SELECT name, salary, dbo.fn_CalculateTax(salary) AS tax FROM employees;
```

### MySQL

```sql
DELIMITER //
CREATE FUNCTION fn_calculate_tax(p_salary DECIMAL(10,2))
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE v_tax DECIMAL(10,2);
    SET v_tax = CASE
        WHEN p_salary > 100000 THEN p_salary * 0.30
        WHEN p_salary > 60000  THEN p_salary * 0.20
        WHEN p_salary > 30000  THEN p_salary * 0.10
        ELSE 0
    END;
    RETURN v_tax;
END;
//
DELIMITER ;

SELECT name, salary, fn_calculate_tax(salary) AS tax FROM employees;
```

---

## 📋 Table-Valued Functions

Return a **table** (multiple rows and columns).

### Inline Table-Valued Function (PostgreSQL)

Returns results of a single SELECT. Most efficient.

```sql
-- Returns table of employees in a department above a salary threshold
CREATE OR REPLACE FUNCTION get_dept_high_earners(
    p_dept    VARCHAR,
    p_min_sal DECIMAL DEFAULT 0
)
RETURNS TABLE(
    id       INT,
    name     TEXT,
    salary   DECIMAL,
    hire_date DATE
)
LANGUAGE SQL
AS $$
    SELECT id, name, salary, hire_date
    FROM employees
    WHERE dept = p_dept
      AND salary >= p_min_sal
    ORDER BY salary DESC;
$$;

-- Use in FROM (like a table)
SELECT * FROM get_dept_high_earners('Engineering', 80000);

-- Use in JOIN
SELECT f.name, f.salary, d.location
FROM get_dept_high_earners('Engineering') f
JOIN department_locations d ON d.dept_name = 'Engineering';
```

### Multi-Statement Table-Valued Function (PostgreSQL PL/pgSQL)

More complex logic building up a result table.

```sql
CREATE OR REPLACE FUNCTION get_employee_hierarchy(p_manager_id INT)
RETURNS TABLE(
    employee_id   INT,
    employee_name TEXT,
    manager_name  TEXT,
    depth         INT
)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE hierarchy AS (
        SELECT e.id, e.name, NULL::TEXT AS manager_name, 0 AS lvl
        FROM employees e
        WHERE e.id = p_manager_id

        UNION ALL

        SELECT e.id, e.name, m.name AS manager_name, h.lvl + 1
        FROM employees e
        JOIN hierarchy h ON e.manager_id = h.employee_id
        JOIN employees m ON e.manager_id = m.id
    )
    SELECT id, name, manager_name, lvl FROM hierarchy;
END;
$$;

SELECT * FROM get_employee_hierarchy(1);  -- Alice's full tree
```

### SQL Server — Inline TVF

```sql
CREATE OR ALTER FUNCTION dbo.fn_GetDeptEmployees(@Dept VARCHAR(50))
RETURNS TABLE
AS RETURN
(
    SELECT id, name, salary, hire_date
    FROM employees
    WHERE dept = @Dept
    ORDER BY salary DESC
);

-- Use
SELECT * FROM dbo.fn_GetDeptEmployees('Engineering');

-- JOIN with TVF
SELECT c.name, e.salary
FROM departments c
CROSS APPLY dbo.fn_GetDeptEmployees(c.name) e
WHERE e.salary > 80000;
```

### SQL Server — Multi-Statement TVF

```sql
CREATE OR ALTER FUNCTION dbo.fn_GetTopEarners(@N INT)
RETURNS @Result TABLE (
    id      INT,
    name    VARCHAR(100),
    salary  DECIMAL(10,2),
    rank    INT
)
AS
BEGIN
    INSERT INTO @Result
    SELECT TOP (@N) id, name, salary,
           ROW_NUMBER() OVER (ORDER BY salary DESC) AS rank
    FROM employees
    ORDER BY salary DESC;

    RETURN;
END;

SELECT * FROM dbo.fn_GetTopEarners(5);
```

---

## 🔧 ALTER & DROP Functions

```sql
-- PostgreSQL: replace existing function
CREATE OR REPLACE FUNCTION calculate_tax(p_salary DECIMAL) ...
-- (same name + signature = replaces)

-- PostgreSQL: drop function
DROP FUNCTION calculate_tax(DECIMAL);
DROP FUNCTION IF EXISTS calculate_tax(DECIMAL);

-- SQL Server: alter
ALTER FUNCTION dbo.fn_CalculateTax (...) RETURNS ... AS BEGIN ... END;

-- SQL Server: drop
DROP FUNCTION IF EXISTS dbo.fn_CalculateTax;
```

---

## ⚠️ Function Limitations

```
In most RDBMS, functions CANNOT:
  ❌ COMMIT or ROLLBACK transactions
  ❌ Call stored procedures (generally)
  ❌ Use PRINT/RAISE NOTICE (SQL Server)
  ❌ Modify session state

In PostgreSQL ONLY:
  ✅ Can run DML (INSERT/UPDATE/DELETE) inside functions
     BUT this is risky — use procedures for DML

Performance notes:
  ⚠️ Scalar UDFs in SQL Server can cause row-by-row execution
     (known as "RBAR" — row-by-agonizing-row)
  ✅ Inline TVFs in SQL Server are inlined by the optimizer (fast)
  ✅ Pure SQL functions in PostgreSQL are often inlined
```

---

## ✅ Practical Examples

```sql
-- 1. Full name helper
CREATE OR REPLACE FUNCTION full_name(p_first VARCHAR, p_last VARCHAR)
RETURNS TEXT LANGUAGE SQL AS $$
    SELECT INITCAP(p_first) || ' ' || INITCAP(p_last);
$$;

SELECT full_name(first_name, last_name) FROM employees;

-- 2. Age from birth date
CREATE OR REPLACE FUNCTION age_in_years(p_birth_date DATE)
RETURNS INT LANGUAGE SQL AS $$
    SELECT EXTRACT(YEAR FROM AGE(p_birth_date))::INT;
$$;

SELECT name, age_in_years(birth_date) AS age FROM customers;

-- 3. Check email format
CREATE OR REPLACE FUNCTION is_valid_email(p_email TEXT)
RETURNS BOOLEAN LANGUAGE plpgsql AS $$
BEGIN
    RETURN p_email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$';
END;
$$;

SELECT * FROM users WHERE NOT is_valid_email(email);
```

---

## 🔑 Key Takeaways

| Type | Returns | Used In |
|------|---------|---------|
| Scalar Function | Single value | SELECT, WHERE, ORDER BY |
| Inline TVF | Table (fast, optimizer-friendly) | FROM, JOIN |
| Multi-statement TVF | Table (more flexible) | FROM, JOIN |

**Best Practices:**
- Mark functions `DETERMINISTIC` / `IMMUTABLE` if they always return same result for same input — enables better optimization
- Use `LANGUAGE SQL` for pure SQL functions (faster than PL/pgSQL overhead)
- Prefer inline TVFs over multi-statement TVFs in SQL Server (better performance)
- Avoid scalar UDFs in hot WHERE/JOIN paths in SQL Server (row-by-row execution penalty)

---

**← Previous:** [22 — Stored Procedures](./22-stored-procedures.md)
**Next →** [24 — Triggers](./24-triggers.md)
