# 07 — DQL: SELECT & Filtering

> **Goal:** Master the SELECT statement — the most-used SQL command — to retrieve, filter, sort, and shape query results.

---

## 📖 SELECT Statement Anatomy

```
SELECT  [DISTINCT] column_list
FROM    table_name
WHERE   condition
GROUP BY column_list
HAVING  aggregate_condition
ORDER BY column_list [ASC|DESC]
LIMIT   n
OFFSET  m;
```

### Logical Execution Order (NOT the written order!)

```
Written Order:          Execution Order:
──────────────          ───────────────
1. SELECT               1. FROM        ← which table
2. FROM                 2. WHERE       ← filter rows
3. WHERE                3. GROUP BY    ← group
4. GROUP BY             4. HAVING      ← filter groups
5. HAVING               5. SELECT      ← compute columns
6. ORDER BY             6. DISTINCT    ← remove dups
7. LIMIT / OFFSET       7. ORDER BY    ← sort
                        8. LIMIT/OFFSET ← paginate
```

> ⚠️ This is why you **cannot** use a SELECT alias in a WHERE clause — WHERE runs before SELECT!

---

## 📋 Sample Data (Used Throughout)

```sql
CREATE TABLE employees (
    id        INT,
    name      VARCHAR(100),
    dept      VARCHAR(50),
    salary    DECIMAL(10,2),
    hire_date DATE,
    manager_id INT
);

INSERT INTO employees VALUES
(1, 'Alice',   'Engineering', 90000, '2020-01-15', NULL),
(2, 'Bob',     'Marketing',   72000, '2019-06-01', 1),
(3, 'Carol',   'Engineering', 85000, '2021-03-22', 1),
(4, 'David',   'HR',          65000, '2022-07-10', 2),
(5, 'Eve',     'Marketing',   78000, '2020-11-05', 2),
(6, 'Frank',   'Engineering', 95000, '2018-09-01', 1),
(7, 'Grace',   'HR',          NULL,  '2023-01-20', 4),
(8, 'Hank',    'Engineering', 70000, '2023-05-15', 6);
```

---

## ⭐ Basic SELECT

```sql
-- Select all columns (avoid in production — use explicit columns)
SELECT * FROM employees;

-- Select specific columns
SELECT id, name, salary FROM employees;

-- Computed column
SELECT id, name, salary, salary * 1.10 AS salary_with_raise
FROM employees;
```

### Aliases (AS)

```sql
-- Column alias
SELECT
    id                          AS employee_id,
    name                        AS full_name,
    salary / 12                 AS monthly_salary,
    UPPER(dept)                 AS department
FROM employees;

-- Table alias
SELECT e.id, e.name, e.salary
FROM employees AS e;          -- 'e' is shorthand for employees
-- Can also omit AS:
FROM employees e;             -- same thing
```

---

## 🔍 WHERE Clause — Filtering

### Comparison Operators

| Operator | Meaning | Example |
|----------|---------|---------|
| `=` | Equal | `salary = 90000` |
| `<>` or `!=` | Not equal | `dept <> 'HR'` |
| `>` | Greater than | `salary > 75000` |
| `<` | Less than | `hire_date < '2021-01-01'` |
| `>=` | Greater or equal | `salary >= 80000` |
| `<=` | Less or equal | `id <= 5` |

```sql
SELECT name, salary FROM employees WHERE salary > 80000;
-- Result: Alice (90000), Carol (85000), Frank (95000)

SELECT name, dept FROM employees WHERE dept <> 'Engineering';
-- Result: Bob, David, Eve (not Engineering)

SELECT name, hire_date FROM employees WHERE hire_date >= '2022-01-01';
-- Result: David, Grace, Hank (hired in 2022 or later)
```

---

### Logical Operators

#### AND — Both conditions must be true

```sql
SELECT name, dept, salary
FROM employees
WHERE dept = 'Engineering' AND salary > 85000;
-- Alice (90000), Frank (95000)
```

#### OR — At least one condition must be true

```sql
SELECT name, dept
FROM employees
WHERE dept = 'HR' OR dept = 'Marketing';
-- Bob, David, Eve, Grace
```

#### NOT — Negate a condition

```sql
SELECT name, dept
FROM employees
WHERE NOT dept = 'Engineering';
-- Bob, David, Eve, Grace (same as dept <> 'Engineering')

SELECT name, salary
FROM employees
WHERE NOT salary > 80000;
-- Bob (72000), David (65000), Eve (78000), Hank (70000)
```

#### Combining Operators (Precedence: NOT > AND > OR)

```sql
-- ⚠️ Always use parentheses to make intent clear!

-- Wrong intent (AND evaluated before OR):
SELECT * FROM employees
WHERE dept = 'HR' OR dept = 'Marketing' AND salary > 75000;
-- Reads as: dept='HR' OR (dept='Marketing' AND salary > 75000)

-- Correct with parens:
SELECT * FROM employees
WHERE (dept = 'HR' OR dept = 'Marketing') AND salary > 75000;
```

---

### BETWEEN … AND

Inclusive range check (equivalent to `>= AND <=`).

```sql
SELECT name, salary
FROM employees
WHERE salary BETWEEN 70000 AND 90000;
-- Bob (72000), Carol (85000), Eve (78000), Alice (90000), Hank (70000)
-- (includes both endpoints!)

SELECT name, hire_date
FROM employees
WHERE hire_date BETWEEN '2020-01-01' AND '2021-12-31';
-- Alice (2020-01-15), Bob (originally 2019... not included), Eve (2020-11-05), Carol (2021-03-22)
```

```sql
-- NOT BETWEEN
SELECT name, salary
FROM employees
WHERE salary NOT BETWEEN 70000 AND 90000;
-- David (65000), Frank (95000)
```

---

### IN / NOT IN

Test membership in a list of values.

```sql
-- IN: equivalent to OR chain
SELECT name, dept
FROM employees
WHERE dept IN ('HR', 'Marketing');
-- Bob, David, Eve, Grace

-- NOT IN
SELECT name, dept
FROM employees
WHERE dept NOT IN ('HR', 'Marketing');
-- Alice, Carol, Frank, Hank (Engineering only)

-- IN with subquery
SELECT name
FROM employees
WHERE id IN (
    SELECT manager_id FROM employees WHERE manager_id IS NOT NULL
);
-- Employees who are managers: Alice, Bob, Frank, David
```

> ⚠️ **NOT IN with NULLs is dangerous!**
> ```sql
> -- If the subquery can return NULL, NOT IN returns no rows!
> WHERE id NOT IN (1, 2, NULL)
> -- This evaluates to: WHERE id NOT IN (1, 2) AND id <> NULL
> -- NULL comparison is always UNKNOWN → whole condition = UNKNOWN → no rows!
> -- Use NOT EXISTS instead when NULLs are possible.
> ```

---

### LIKE / Pattern Matching

```
Wildcards:
  % → matches any sequence of characters (including zero)
  _ → matches exactly one character
```

```sql
-- Names starting with 'A'
SELECT name FROM employees WHERE name LIKE 'A%';
-- Alice

-- Names ending with 'e'
SELECT name FROM employees WHERE name LIKE '%e';
-- Alice, Grace

-- Names containing 'ar'
SELECT name FROM employees WHERE name LIKE '%ar%';
-- Carol

-- Names where second letter is 'o'
SELECT name FROM employees WHERE name LIKE '_o%';
-- Bob

-- NOT LIKE
SELECT name FROM employees WHERE name NOT LIKE 'A%';
-- Bob, Carol, David, Eve, Frank, Grace, Hank

-- Case-insensitive (PostgreSQL: ILIKE)
SELECT name FROM employees WHERE name ILIKE 'a%';
-- Alice

-- MySQL: LIKE is case-insensitive by default for ci collations
```

---

### NULL Handling

**NULL means "unknown"** — it is not zero, not empty string, not false. It is the absence of a value.

```sql
-- ❌ WRONG — cannot compare with = or <>
SELECT name FROM employees WHERE salary = NULL;    -- returns 0 rows!
SELECT name FROM employees WHERE salary <> NULL;   -- returns 0 rows!

-- ✅ CORRECT — use IS NULL / IS NOT NULL
SELECT name, salary FROM employees WHERE salary IS NULL;
-- Grace (salary is NULL)

SELECT name, salary FROM employees WHERE salary IS NOT NULL;
-- Alice, Bob, Carol, David, Eve, Frank, Hank
```

```
NULL arithmetic:
NULL + 5       = NULL
NULL * 100     = NULL
NULL = NULL    = NULL (unknown!)
NULL <> NULL   = NULL (unknown!)
NULL IS NULL   = TRUE ✅
```

---

## 📊 ORDER BY — Sorting

```sql
-- Sort by salary descending (highest first)
SELECT name, salary
FROM employees
ORDER BY salary DESC;

-- Sort by salary ascending (lowest first, default)
SELECT name, salary
FROM employees
ORDER BY salary ASC;    -- ASC is default, can omit

-- Multiple sort columns
SELECT name, dept, salary
FROM employees
ORDER BY dept ASC, salary DESC;
-- Groups by dept alphabetically, then highest salary first within each dept

-- Sort by column alias
SELECT name, salary * 12 AS annual_salary
FROM employees
ORDER BY annual_salary DESC;

-- Sort by column position (not recommended — fragile)
SELECT name, salary FROM employees ORDER BY 2 DESC;   -- 2nd column = salary

-- NULL ordering (PostgreSQL: NULLS LAST / NULLS FIRST)
SELECT name, salary FROM employees
ORDER BY salary DESC NULLS LAST;   -- NULLs appear at the end
```

---

## 📄 LIMIT & OFFSET — Pagination

```sql
-- Get first 3 rows
SELECT name, salary FROM employees
ORDER BY salary DESC
LIMIT 3;
-- Frank (95000), Alice (90000), Carol (85000)

-- Skip first 3, get next 3
SELECT name, salary FROM employees
ORDER BY salary DESC
LIMIT 3 OFFSET 3;
-- Eve (78000), Bob (72000), Hank (70000)
-- (used for pagination: page 2, 3 per page)
```

### Pagination Formula

```
Page 1: LIMIT {per_page} OFFSET 0
Page 2: LIMIT {per_page} OFFSET {per_page}
Page N: LIMIT {per_page} OFFSET ({N-1} * {per_page})

-- Example: 3 per page, page 2
LIMIT 3 OFFSET 3    -- (2-1) * 3 = 3
```

### RDBMS Equivalents

| RDBMS | Syntax |
|-------|--------|
| PostgreSQL / MySQL / SQLite | `LIMIT n OFFSET m` |
| SQL Server | `FETCH NEXT n ROWS ONLY` with `OFFSET m ROWS` |
| Oracle | `FETCH FIRST n ROWS ONLY` |

```sql
-- SQL Server
SELECT name, salary FROM employees
ORDER BY salary DESC
OFFSET 3 ROWS FETCH NEXT 3 ROWS ONLY;

-- Oracle
SELECT name, salary FROM employees
ORDER BY salary DESC
OFFSET 3 ROWS FETCH NEXT 3 ROWS ONLY;  -- Oracle 12c+
```

---

## 🔠 DISTINCT — Remove Duplicates

```sql
-- Without DISTINCT — shows all depts (with repetitions)
SELECT dept FROM employees;
-- Engineering, Marketing, Engineering, HR, Marketing, Engineering, HR, Engineering

-- With DISTINCT — unique values only
SELECT DISTINCT dept FROM employees;
-- Engineering, HR, Marketing

-- DISTINCT on multiple columns — unique combinations
SELECT DISTINCT dept, manager_id
FROM employees
ORDER BY dept;
```

> ⚠️ `DISTINCT` is applied to the **entire row** being selected, not just one column.
> `SELECT DISTINCT dept, salary` means unique (dept, salary) combinations.

---

## 🔢 FETCH / TOP (SQL Server)

```sql
-- SQL Server: TOP
SELECT TOP 5 name, salary
FROM employees
ORDER BY salary DESC;

-- SQL Server: FETCH
SELECT name, salary FROM employees
ORDER BY salary DESC
OFFSET 0 ROWS FETCH FIRST 5 ROWS ONLY;
```

---

## 🗺️ Full Query Example

```sql
-- Find top 3 Engineering employees hired after 2019, by salary
SELECT
    id,
    name,
    salary,
    salary * 1.10   AS salary_after_raise,
    hire_date
FROM employees
WHERE
    dept = 'Engineering'
    AND hire_date > '2019-12-31'
    AND salary IS NOT NULL
ORDER BY salary DESC
LIMIT 3;

/*
Execution:
  FROM employees             → 8 rows
  WHERE dept='Engineering'   → 4 rows (Alice, Carol, Frank, Hank)
  WHERE hire_date > 2019-12-31 → removes Frank (2018)
  WHERE salary IS NOT NULL   → all 3 have salary
  SELECT + alias             → compute salary_after_raise
  ORDER BY salary DESC       → Alice, Carol, Hank
  LIMIT 3                    → all 3 qualify
*/
```

---

## 🔑 Key Takeaways

| Concept | Key Rule |
|---------|---------|
| Execution order | FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT |
| `=` vs `IS NULL` | Use `IS NULL` for null checks, never `= NULL` |
| `BETWEEN` | Inclusive on both ends |
| `IN` with NULLs | Avoid `NOT IN` when subquery may return NULLs |
| `LIKE` wildcards | `%` = any chars, `_` = one char |
| `ORDER BY` default | ASC (ascending) |
| `LIMIT` + `OFFSET` | Used for pagination |
| `DISTINCT` | Applies to entire selected row |
| Column aliases | Available in ORDER BY, NOT in WHERE (use subquery) |

---

**← Previous:** [06 — DML](./06-dml.md)
**Next →** [08 — String Functions](./08-string-functions.md)
