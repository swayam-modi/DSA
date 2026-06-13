# 10 — Mathematical Functions

> **Goal:** Use built-in math functions for calculations, rounding, and statistical operations in SQL.

---

## ABS — Absolute Value

```sql
SELECT ABS(-42);        -- 42
SELECT ABS(42);         -- 42
SELECT ABS(-3.14);      -- 3.14

-- Find orders where profit deviation is more than 1000
SELECT * FROM orders WHERE ABS(expected_profit - actual_profit) > 1000;

-- Distance between two values
SELECT ABS(budget - actual_spend) AS variance FROM projects;
```

---

## ROUND — Round to Decimal Places

```sql
SELECT ROUND(3.14159, 2);    -- 3.14
SELECT ROUND(3.14159, 4);    -- 3.1416
SELECT ROUND(3.14159, 0);    -- 3
SELECT ROUND(3.5);           -- 4  (rounds up)
SELECT ROUND(2.5);           -- 2 or 3 (depends on RDBMS: banker's rounding)
SELECT ROUND(-3.14, 1);      -- -3.1

-- Round salary to nearest thousand
SELECT name, ROUND(salary, -3) AS rounded_salary FROM employees;
-- 85500 → 86000

-- Financial reporting: 2 decimal places
SELECT
    product_id,
    ROUND(SUM(qty * price), 2) AS total_revenue
FROM order_items
GROUP BY product_id;
```

---

## FLOOR — Round DOWN

```sql
SELECT FLOOR(3.9);      -- 3
SELECT FLOOR(3.1);      -- 3
SELECT FLOOR(-3.1);     -- -4  (floor goes toward negative infinity)
SELECT FLOOR(3.0);      -- 3

-- Age calculation (full years only)
SELECT FLOOR(EXTRACT(EPOCH FROM AGE(NOW(), birth_date)) / (365.25 * 86400)) AS age
FROM employees;

-- Assign users to buckets 0-9 based on ID
SELECT id, FLOOR(id / 1000) AS bucket FROM users;
```

---

## CEILING / CEIL — Round UP

```sql
SELECT CEILING(3.1);    -- 4
SELECT CEILING(3.9);    -- 4
SELECT CEILING(-3.9);   -- -3  (ceiling goes toward positive infinity)
SELECT CEIL(3.0);       -- 3   (CEIL = alias for CEILING)

-- Minimum pages needed for N items, K per page
-- 25 items, 4 per page → CEIL(25/4.0) = 7 pages
SELECT CEILING(COUNT(*) / 10.0) AS total_pages FROM products;
```

---

## MOD / % — Modulo (Remainder)

```sql
SELECT MOD(10, 3);      -- 1   (10 / 3 = 3 remainder 1)
SELECT MOD(15, 5);      -- 0   (divisible)
SELECT 10 % 3;          -- 1   (operator form)

-- Find even-numbered IDs
SELECT id FROM products WHERE MOD(id, 2) = 0;

-- Distribute rows across 5 partitions
SELECT id, MOD(id, 5) AS partition_key FROM orders;

-- Find every 7th record (weekly samples)
SELECT * FROM logs WHERE MOD(id, 7) = 0;
```

---

## POWER / POW — Exponentiation

```sql
SELECT POWER(2, 10);    -- 1024  (2^10)
SELECT POWER(3, 3);     -- 27    (3^3)
SELECT POW(2, 8);       -- 256   (POW is alias in MySQL/PostgreSQL)
SELECT 2 ^ 10;          -- 1024  (PostgreSQL ^ operator)

-- Compound interest: A = P * (1 + r)^n
SELECT
    principal,
    ROUND(principal * POWER(1 + rate, years), 2) AS final_amount
FROM investments;
```

---

## SQRT — Square Root

```sql
SELECT SQRT(144);       -- 12
SELECT SQRT(2);         -- 1.4142135623730951
SELECT SQRT(0);         -- 0

-- Euclidean distance between two points
SELECT
    SQRT(POWER(x2 - x1, 2) + POWER(y2 - y1, 2)) AS distance
FROM locations;
```

---

## RAND / RANDOM — Random Number

```sql
-- PostgreSQL: RANDOM() returns 0.0 to 1.0
SELECT RANDOM();                            -- e.g. 0.7431...

-- Random integer between 1 and 100
SELECT FLOOR(RANDOM() * 100 + 1)::INT;     -- PostgreSQL

-- MySQL: RAND() returns 0.0 to 1.0
SELECT RAND();                              -- e.g. 0.5831...
SELECT FLOOR(RAND() * 100 + 1);            -- random 1-100 (MySQL)

-- SQL Server
SELECT RAND();
SELECT ABS(CHECKSUM(NEWID())) % 100 + 1;   -- random 1-100 (SQL Server)

-- Select 5 random rows
SELECT * FROM products ORDER BY RANDOM() LIMIT 5;          -- PostgreSQL
SELECT * FROM products ORDER BY RAND()   LIMIT 5;          -- MySQL
SELECT TOP 5 * FROM products ORDER BY NEWID();              -- SQL Server
```

---

## PI — Pi Constant

```sql
SELECT PI();            -- 3.141592653589793  (MySQL)
SELECT PI();            -- 3.141592653589793  (PostgreSQL)

-- Circle area
SELECT ROUND(PI() * POWER(radius, 2), 2) AS area FROM circles;
```

---

## EXP — Euler's Number (e^x)

```sql
SELECT EXP(1);          -- 2.718281828... (e^1)
SELECT EXP(2);          -- 7.389056... (e^2)
SELECT EXP(0);          -- 1

-- Exponential growth
SELECT EXP(rate * time) AS growth_factor FROM experiments;
```

---

## LOG — Logarithm

```sql
-- Natural log (base e)
SELECT LN(2.718281828);     -- ~1.0  (PostgreSQL: LN)
SELECT LOG(2.718281828);    -- ~1.0  (MySQL: LOG = natural log)

-- Log base 10
SELECT LOG10(100);          -- 2.0
SELECT LOG10(1000);         -- 3.0
SELECT LOG(100) / LOG(10);  -- 2.0  (change of base formula)

-- Custom base: LOG(base, value) — PostgreSQL
SELECT LOG(2, 1024);        -- 10  (2^10 = 1024)
SELECT LOG(10, 1000);       -- 3
```

---

## SIGN — Sign of a Number

```sql
SELECT SIGN(-42);       -- -1
SELECT SIGN(0);         --  0
SELECT SIGN(42);        --  1

-- Categorize profit/loss
SELECT
    product_id,
    profit,
    CASE SIGN(profit)
        WHEN  1 THEN 'Profit'
        WHEN  0 THEN 'Break-even'
        WHEN -1 THEN 'Loss'
    END AS status
FROM products;
```

---

## TRUNC / TRUNCATE — Truncate (Don't Round)

```sql
-- PostgreSQL: TRUNC
SELECT TRUNC(3.9);      -- 3   (drops decimal, doesn't round)
SELECT TRUNC(-3.9);     -- -3  (toward zero)
SELECT TRUNC(3.14159, 2);  -- 3.14

-- MySQL: TRUNCATE(value, decimals)
SELECT TRUNCATE(3.14159, 2);   -- 3.14
SELECT TRUNCATE(999.99, -2);   -- 900 (truncate to hundreds)

-- vs ROUND:
SELECT ROUND(3.9);    -- 4  (rounds UP)
SELECT TRUNC(3.9);    -- 3  (truncates DOWN to integer)
```

---

## GREATEST / LEAST

```sql
-- Return the largest/smallest value among arguments
SELECT GREATEST(1, 5, 3, 7, 2);    -- 7
SELECT LEAST(1, 5, 3, 7, 2);       -- 1

SELECT GREATEST(10, NULL, 5);       -- NULL (if any arg is NULL, returns NULL in PG)
SELECT LEAST(10, NULL, 5);          -- NULL

-- Cap a discount between 0% and 50%
SELECT LEAST(GREATEST(discount, 0), 0.50) AS capped_discount FROM products;

-- Find the later of two dates
SELECT GREATEST(promised_date, actual_date) AS effective_date FROM deliveries;
```

---

## COALESCE — First Non-NULL (Very Useful!)

```sql
-- Returns first non-NULL value
SELECT COALESCE(NULL, NULL, 3, 4, 5);    -- 3

-- Use a fallback value for NULL salary
SELECT name, COALESCE(salary, 0) AS salary FROM employees;

-- First available contact method
SELECT
    name,
    COALESCE(mobile_phone, home_phone, email, 'No contact') AS contact
FROM customers;
```

---

## NULLIF — Return NULL if Equal

```sql
-- Returns NULL if two values are equal; otherwise returns first value
SELECT NULLIF(5, 5);    -- NULL
SELECT NULLIF(5, 3);    -- 5

-- Avoid division by zero!
SELECT total_revenue / NULLIF(total_orders, 0) AS avg_order_value
FROM sales_summary;
-- If total_orders = 0, NULLIF returns NULL → division returns NULL (not error)
```

---

## Math Functions Reference Table

| Function | PostgreSQL | MySQL | SQL Server | Result |
|----------|-----------|-------|------------|--------|
| Absolute | `ABS(x)` | `ABS(x)` | `ABS(x)` | Positive value |
| Round | `ROUND(x,n)` | `ROUND(x,n)` | `ROUND(x,n)` | Nearest at n decimals |
| Floor | `FLOOR(x)` | `FLOOR(x)` | `FLOOR(x)` | Round down |
| Ceiling | `CEILING(x)` | `CEILING(x)` | `CEILING(x)` | Round up |
| Modulo | `MOD(x,y)` / `x%y` | `MOD(x,y)` | `x%y` | Remainder |
| Power | `POWER(x,y)` | `POW(x,y)` | `POWER(x,y)` | x^y |
| Square root | `SQRT(x)` | `SQRT(x)` | `SQRT(x)` | √x |
| Random | `RANDOM()` | `RAND()` | `RAND()` | 0.0 to 1.0 |
| Pi | `PI()` | `PI()` | `PI()` | 3.14159... |
| Exp | `EXP(x)` | `EXP(x)` | `EXP(x)` | e^x |
| Natural log | `LN(x)` | `LOG(x)` | `LOG(x)` | logₑ(x) |
| Log base 10 | `LOG10(x)` | `LOG10(x)` | `LOG10(x)` | log₁₀(x) |
| Sign | `SIGN(x)` | `SIGN(x)` | `SIGN(x)` | -1, 0, or 1 |
| Truncate | `TRUNC(x,n)` | `TRUNCATE(x,n)` | `ROUND(x,n,1)` | Cut off decimals |
| Greatest | `GREATEST(...)` | `GREATEST(...)` | (use CASE) | Largest argument |
| Least | `LEAST(...)` | `LEAST(...)` | (use CASE) | Smallest argument |

---

## 🔑 Key Takeaways

| Function | Primary Use Case |
|----------|----------------|
| `ABS` | Distance, deviation, variance |
| `ROUND` | Financial reporting, display precision |
| `FLOOR` / `CEIL` | Page counts, bucket assignments |
| `MOD` | Even/odd check, partitioning |
| `POWER` | Compound interest, distances |
| `SQRT` | Euclidean distance, statistics |
| `RANDOM` | Random sampling, shuffle |
| `COALESCE` | Default values for NULLs |
| `NULLIF` | Safe division, avoid divide-by-zero |
| `GREATEST/LEAST` | Clamping values, comparing dates |

---

**← Previous:** [09 — Date & Time Functions](./09-datetime-functions.md)
**Next →** [11 — Aggregate Functions & Grouping](./11-aggregate-functions.md)
