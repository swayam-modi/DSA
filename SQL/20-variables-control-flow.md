# 20 — Variables & Control Flow

> **Goal:** Use variables, conditional logic, and loops in SQL procedural extensions (PL/pgSQL, T-SQL, PL/SQL).

---

## 📖 Overview

Standard SQL is **declarative** — you describe *what* you want, not *how* to get it. For procedural logic, each RDBMS provides extensions:

| RDBMS | Procedural Language |
|-------|-------------------|
| PostgreSQL | PL/pgSQL |
| MySQL | MySQL Stored Procedure language |
| SQL Server | T-SQL (Transact-SQL) |
| Oracle | PL/SQL |

---

## 📦 Variables

### PostgreSQL (PL/pgSQL)

```sql
DO $$
DECLARE
    v_name      VARCHAR(100);
    v_salary    DECIMAL(10,2);
    v_count     INT := 0;           -- initialize with value
    v_today     DATE := CURRENT_DATE;
BEGIN
    -- SET a variable with SELECT INTO
    SELECT name, salary
    INTO v_name, v_salary
    FROM employees
    WHERE id = 1;

    -- Or use direct assignment
    v_count := v_count + 1;

    -- Output
    RAISE NOTICE 'Employee: %, Salary: %', v_name, v_salary;
END;
$$ LANGUAGE plpgsql;
```

### SQL Server (T-SQL)

```sql
DECLARE @name    VARCHAR(100);
DECLARE @salary  DECIMAL(10,2);
DECLARE @count   INT = 0;          -- initialize

-- Assign with SELECT
SELECT @name = name, @salary = salary
FROM employees
WHERE id = 1;

-- Or with SET
SET @count = @count + 1;

-- Output
PRINT 'Employee: ' + @name;
SELECT @name AS name, @salary AS salary;
```

### MySQL

```sql
-- In stored procedure context
DECLARE v_name   VARCHAR(100);
DECLARE v_salary DECIMAL(10,2) DEFAULT 0;

SELECT name, salary INTO v_name, v_salary
FROM employees WHERE id = 1;

SELECT v_name, v_salary;
```

---

## 🔀 Conditional Statements

### IF / IF-ELSE (PL/pgSQL)

```sql
DO $$
DECLARE
    v_salary DECIMAL := 90000;
    v_level  VARCHAR(20);
BEGIN
    IF v_salary >= 90000 THEN
        v_level := 'Senior';
    ELSIF v_salary >= 70000 THEN
        v_level := 'Mid';
    ELSIF v_salary >= 50000 THEN
        v_level := 'Junior';
    ELSE
        v_level := 'Entry';
    END IF;

    RAISE NOTICE 'Level: %', v_level;
END;
$$ LANGUAGE plpgsql;
```

### IF / ELSE (T-SQL)

```sql
DECLARE @salary DECIMAL = 90000;
DECLARE @level  VARCHAR(20);

IF @salary >= 90000
    SET @level = 'Senior';
ELSE IF @salary >= 70000
    SET @level = 'Mid';
ELSE IF @salary >= 50000
    SET @level = 'Junior';
ELSE
    SET @level = 'Entry';

PRINT @level;
```

---

## 🎭 CASE Expression (Standard SQL)

CASE works in `SELECT`, `WHERE`, `ORDER BY`, and `UPDATE`. It's the SQL equivalent of switch/if-else.

### Simple CASE

```sql
SELECT
    name,
    dept,
    CASE dept
        WHEN 'Engineering' THEN 'Tech'
        WHEN 'Marketing'   THEN 'Business'
        WHEN 'HR'          THEN 'People'
        ELSE 'Other'
    END AS dept_group
FROM employees;
```

### Searched CASE (Most Flexible)

```sql
SELECT
    name,
    salary,
    CASE
        WHEN salary >= 90000 THEN 'Senior'
        WHEN salary >= 70000 THEN 'Mid-level'
        WHEN salary >= 50000 THEN 'Junior'
        ELSE 'Entry Level'
    END AS salary_band
FROM employees;
```

### CASE in UPDATE

```sql
UPDATE employees
SET salary =
    CASE
        WHEN dept = 'Engineering' THEN salary * 1.15
        WHEN dept = 'Marketing'   THEN salary * 1.10
        ELSE                           salary * 1.05
    END;
```

### CASE in ORDER BY

```sql
-- Custom sort: Engineering first, then HR, then Marketing
SELECT name, dept FROM employees
ORDER BY
    CASE dept
        WHEN 'Engineering' THEN 1
        WHEN 'HR'          THEN 2
        WHEN 'Marketing'   THEN 3
        ELSE                    4
    END, name;
```

### CASE for Pivot

```sql
-- Pivot: dept rows → columns
SELECT
    COUNT(CASE WHEN dept = 'Engineering' THEN 1 END) AS engineering_count,
    COUNT(CASE WHEN dept = 'HR'          THEN 1 END) AS hr_count,
    COUNT(CASE WHEN dept = 'Marketing'   THEN 1 END) AS marketing_count
FROM employees;

-- Result:
-- engineering_count | hr_count | marketing_count
--         4         |    2     |       2
```

---

## 🔄 Loops

### WHILE Loop (T-SQL)

```sql
DECLARE @i INT = 1;
DECLARE @sum INT = 0;

WHILE @i <= 10
BEGIN
    SET @sum = @sum + @i;
    SET @i = @i + 1;
END;

PRINT 'Sum: ' + CAST(@sum AS VARCHAR);  -- Sum: 55
```

### LOOP in PL/pgSQL

```sql
DO $$
DECLARE
    v_i   INT := 1;
    v_sum INT := 0;
BEGIN
    LOOP
        EXIT WHEN v_i > 10;    -- exit condition
        v_sum := v_sum + v_i;
        v_i := v_i + 1;
    END LOOP;
    RAISE NOTICE 'Sum: %', v_sum;
END;
$$ LANGUAGE plpgsql;
```

### FOR Loop (PL/pgSQL)

```sql
DO $$
BEGIN
    -- Loop over a range
    FOR i IN 1..10 LOOP
        RAISE NOTICE 'i = %', i;
    END LOOP;

    -- Loop over query results
    FOR rec IN SELECT name, salary FROM employees LOOP
        RAISE NOTICE '% earns %', rec.name, rec.salary;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
```

### WHILE in MySQL

```sql
DELIMITER //
CREATE PROCEDURE count_to_ten()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 10 DO
        SELECT i;
        SET i = i + 1;
    END WHILE;
END;
//
DELIMITER ;

CALL count_to_ten();
```

### REPEAT...UNTIL (MySQL / PL/pgSQL)

```sql
-- PL/pgSQL
DO $$
DECLARE v_i INT := 0;
BEGIN
    LOOP
        v_i := v_i + 1;
        EXIT WHEN v_i >= 5;
    END LOOP;
    RAISE NOTICE 'v_i = %', v_i;
END;
$$ LANGUAGE plpgsql;

-- MySQL
REPEAT
    SET i = i + 1;
UNTIL i >= 5 END REPEAT;
```

---

## 🔑 Key Takeaways

| Concept | PostgreSQL (PL/pgSQL) | SQL Server (T-SQL) | MySQL |
|---------|----------------------|-------------------|-------|
| Declare variable | `DECLARE v_name TYPE;` | `DECLARE @name TYPE` | `DECLARE v_name TYPE` |
| Assign | `v_name := value;` | `SET @name = value` | `SET v_name = value` |
| From SELECT | `SELECT col INTO v` | `SELECT @v = col` | `SELECT col INTO v` |
| IF | `IF ... THEN ... ELSIF ... END IF` | `IF ... ELSE IF` | `IF ... ELSEIF ... END IF` |
| CASE | `CASE WHEN THEN ELSE END` | Same | Same |
| FOR loop | `FOR i IN 1..n LOOP` | (use WHILE) | `REPEAT/WHILE` |
| WHILE | `LOOP ... EXIT WHEN ... END LOOP` | `WHILE ... BEGIN ... END` | `WHILE ... DO ... END WHILE` |
| Print/debug | `RAISE NOTICE 'msg'` | `PRINT 'msg'` | `SELECT 'msg'` |

**Important:** CASE expressions are standard SQL and work everywhere in SELECT, WHERE, UPDATE, and ORDER BY. Procedural IF/LOOP constructs only work inside stored procedures and PL/pgSQL blocks.

---

**← Previous:** [19 — Query Optimization](./19-query-optimization.md)
**Next →** [21 — JSON Processing](./21-json-processing.md)
