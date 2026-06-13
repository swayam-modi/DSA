# 09 — Date & Time Functions

> **Goal:** Work with dates and times — extract components, calculate differences, format output.

---

## 📖 Overview

Date/time functions are essential for:
- Filtering records by date ranges
- Calculating age, tenure, deadlines
- Grouping by time period (monthly reports, daily stats)
- Formatting timestamps for display

---

## 🕐 Current Date/Time

| Function | Returns | RDBMS |
|----------|---------|-------|
| `CURRENT_DATE` | Today's date | All |
| `CURRENT_TIME` | Current time | All |
| `CURRENT_TIMESTAMP` | Current date + time | All |
| `NOW()` | Current date + time | PostgreSQL, MySQL |
| `GETDATE()` | Current date + time | SQL Server |
| `SYSDATE` | Current date + time | Oracle |

```sql
-- PostgreSQL / MySQL
SELECT CURRENT_DATE;                -- 2025-01-15
SELECT CURRENT_TIME;                -- 14:30:00.123456
SELECT CURRENT_TIMESTAMP;           -- 2025-01-15 14:30:00.123456+05:30
SELECT NOW();                       -- 2025-01-15 14:30:00.123456+05:30

-- SQL Server
SELECT GETDATE();                   -- 2025-01-15 14:30:00.123

-- Find records from today
SELECT * FROM orders WHERE created_at::DATE = CURRENT_DATE;    -- PostgreSQL
SELECT * FROM orders WHERE DATE(created_at) = CURDATE();       -- MySQL
```

---

## 📅 Date Extraction Functions

### EXTRACT / YEAR / MONTH / DAY

```sql
-- PostgreSQL / MySQL: EXTRACT(part FROM date)
SELECT EXTRACT(YEAR   FROM hire_date) AS hire_year   FROM employees;
SELECT EXTRACT(MONTH  FROM hire_date) AS hire_month  FROM employees;
SELECT EXTRACT(DAY    FROM hire_date) AS hire_day    FROM employees;
SELECT EXTRACT(HOUR   FROM NOW())     AS current_hour;
SELECT EXTRACT(MINUTE FROM NOW())     AS current_minute;
SELECT EXTRACT(DOW    FROM NOW())     AS day_of_week;  -- 0=Sunday...6=Saturday
SELECT EXTRACT(DOY    FROM NOW())     AS day_of_year;  -- 1-366
SELECT EXTRACT(WEEK   FROM NOW())     AS week_number;
SELECT EXTRACT(QUARTER FROM NOW())    AS quarter;      -- 1-4
```

```sql
-- MySQL shorthand functions
SELECT YEAR(hire_date),  MONTH(hire_date), DAY(hire_date)    FROM employees;
SELECT HOUR(NOW()), MINUTE(NOW()), SECOND(NOW());
SELECT DAYOFWEEK(NOW());   -- 1=Sunday ... 7=Saturday
SELECT DAYNAME(NOW());     -- 'Wednesday'
SELECT MONTHNAME(NOW());   -- 'January'
SELECT QUARTER(NOW());     -- 1, 2, 3, or 4
SELECT WEEK(NOW());        -- week number
```

```sql
-- SQL Server
SELECT YEAR(hire_date), MONTH(hire_date), DAY(hire_date) FROM employees;
SELECT DATEPART(weekday, GETDATE());    -- 1=Sunday...7=Saturday
SELECT DATEPART(quarter, GETDATE());   -- 1-4
SELECT DATENAME(month, GETDATE());     -- 'January'
```

### Practical Examples

```sql
-- Count employees hired per year
SELECT
    EXTRACT(YEAR FROM hire_date) AS year,
    COUNT(*) AS hired
FROM employees
GROUP BY EXTRACT(YEAR FROM hire_date)
ORDER BY year;

-- Monthly revenue report
SELECT
    EXTRACT(YEAR FROM created_at)  AS year,
    EXTRACT(MONTH FROM created_at) AS month,
    SUM(total)                     AS revenue
FROM orders
WHERE created_at >= '2024-01-01'
GROUP BY 1, 2
ORDER BY 1, 2;

-- Find all employees hired in Q1 (Jan-Mar)
SELECT name, hire_date
FROM employees
WHERE EXTRACT(QUARTER FROM hire_date) = 1;
```

---

## ➕ Date Arithmetic

### Adding/Subtracting Intervals (PostgreSQL)

```sql
-- Add intervals
SELECT NOW() + INTERVAL '7 days';       -- 1 week from now
SELECT NOW() + INTERVAL '3 months';     -- 3 months from now
SELECT NOW() + INTERVAL '1 year';       -- next year
SELECT NOW() - INTERVAL '30 minutes';   -- 30 min ago
SELECT NOW() - INTERVAL '2 years 3 months 15 days';

-- Column arithmetic
SELECT
    hire_date,
    hire_date + INTERVAL '90 days'  AS probation_end,
    hire_date + INTERVAL '1 year'   AS first_anniversary
FROM employees;

-- Find orders placed in last 30 days
SELECT * FROM orders
WHERE created_at >= NOW() - INTERVAL '30 days';
```

### DATEADD (SQL Server / MySQL)

```sql
-- SQL Server: DATEADD(part, number, date)
SELECT DATEADD(day,   7,  GETDATE());     -- +7 days
SELECT DATEADD(month, 3,  GETDATE());     -- +3 months
SELECT DATEADD(year, -1,  GETDATE());     -- -1 year
SELECT DATEADD(hour, 12,  GETDATE());     -- +12 hours

-- MySQL: DATE_ADD / DATE_SUB
SELECT DATE_ADD(NOW(), INTERVAL 7 DAY);
SELECT DATE_ADD(NOW(), INTERVAL 3 MONTH);
SELECT DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

### DATEDIFF — Calculate Difference

```sql
-- PostgreSQL: subtract dates directly
SELECT
    '2025-12-31'::DATE - '2025-01-01'::DATE AS days_diff;  -- 364
SELECT
    AGE('2025-12-31', '2000-03-15');   -- '25 years 9 months 16 days'
SELECT
    EXTRACT(YEAR FROM AGE(NOW(), hire_date)) AS years_at_company
FROM employees;

-- MySQL: DATEDIFF(end, start) → days
SELECT DATEDIFF('2025-12-31', '2025-01-01');               -- 364
SELECT DATEDIFF(NOW(), hire_date) / 365 AS years_employed  -- approx years
FROM employees;

-- SQL Server: DATEDIFF(part, start, end)
SELECT DATEDIFF(day,   '2025-01-01', '2025-12-31');     -- 364
SELECT DATEDIFF(month, '2023-01-01', GETDATE());          -- months elapsed
SELECT DATEDIFF(year,  hire_date,    GETDATE())           -- years employed
FROM employees;
```

### Age Calculation

```sql
-- PostgreSQL: exact age
SELECT
    name,
    birth_date,
    AGE(birth_date)                         AS age,
    EXTRACT(YEAR FROM AGE(birth_date))      AS years_old
FROM employees;

-- MySQL: approximate age
SELECT
    name,
    birth_date,
    TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) AS age
FROM employees;

-- SQL Server
SELECT
    name,
    birth_date,
    DATEDIFF(year, birth_date, GETDATE()) AS age
FROM employees;
```

---

## 🎨 Date Formatting

### TO_CHAR (PostgreSQL / Oracle)

```sql
SELECT TO_CHAR(NOW(), 'YYYY-MM-DD');           -- '2025-01-15'
SELECT TO_CHAR(NOW(), 'DD/MM/YYYY');           -- '15/01/2025'
SELECT TO_CHAR(NOW(), 'Month DD, YYYY');        -- 'January  15, 2025'
SELECT TO_CHAR(NOW(), 'Day, DD Mon YYYY');      -- 'Wednesday, 15 Jan 2025'
SELECT TO_CHAR(NOW(), 'HH24:MI:SS');           -- '14:30:45'
SELECT TO_CHAR(NOW(), 'HH12:MI:SS AM');        -- '02:30:45 PM'
SELECT TO_CHAR(salary, '$9,999,999.99') FROM employees;  -- currency format
```

### Common TO_CHAR Format Codes (PostgreSQL)

| Code | Meaning | Example |
|------|---------|---------|
| `YYYY` | 4-digit year | 2025 |
| `YY` | 2-digit year | 25 |
| `MM` | Month number | 01 |
| `Mon` | Month abbreviation | Jan |
| `Month` | Full month name | January |
| `DD` | Day of month | 15 |
| `Day` | Full day name | Wednesday |
| `HH24` | Hour (0-23) | 14 |
| `HH12` | Hour (1-12) | 02 |
| `MI` | Minutes | 30 |
| `SS` | Seconds | 45 |
| `AM/PM` | AM/PM indicator | PM |
| `TZ` | Timezone | +05:30 |
| `Q` | Quarter | 1 |
| `IW` | ISO week number | 03 |

### FORMAT (MySQL / SQL Server)

```sql
-- MySQL
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d');            -- '2025-01-15'
SELECT DATE_FORMAT(NOW(), '%d/%m/%Y');            -- '15/01/2025'
SELECT DATE_FORMAT(NOW(), '%W, %d %M %Y');        -- 'Wednesday, 15 January 2025'
SELECT DATE_FORMAT(NOW(), '%H:%i:%s');            -- '14:30:45'
SELECT DATE_FORMAT(NOW(), '%l:%i %p');            -- '2:30 PM'

-- SQL Server
SELECT FORMAT(GETDATE(), 'yyyy-MM-dd');           -- '2025-01-15'
SELECT FORMAT(GETDATE(), 'dd/MM/yyyy');           -- '15/01/2025'
SELECT FORMAT(GETDATE(), 'dddd, dd MMMM yyyy');   -- 'Wednesday, 15 January 2025'
SELECT FORMAT(GETDATE(), 'hh:mm:ss tt');          -- '02:30:45 PM'
```

---

## 📦 Date Conversion / Parsing

```sql
-- PostgreSQL: cast text to date
SELECT '2025-01-15'::DATE;
SELECT '14:30:00'::TIME;
SELECT '2025-01-15 14:30:00'::TIMESTAMP;
SELECT TO_DATE('15/01/2025', 'DD/MM/YYYY');
SELECT TO_TIMESTAMP('15/01/2025 14:30', 'DD/MM/YYYY HH24:MI');

-- MySQL
SELECT STR_TO_DATE('15/01/2025', '%d/%m/%Y');     -- DATE: 2025-01-15

-- SQL Server
SELECT CAST('2025-01-15' AS DATE);
SELECT CONVERT(DATE, '15/01/2025', 103);           -- style 103 = dd/mm/yyyy
```

---

## 🗺️ Date Function Quick Reference

```
Today:
  PostgreSQL: CURRENT_DATE, NOW()
  MySQL:      CURDATE(), NOW()
  SQL Server: CAST(GETDATE() AS DATE), GETDATE()

Extract year:
  All: EXTRACT(YEAR FROM col) or YEAR(col)

Add 30 days:
  PostgreSQL: col + INTERVAL '30 days'
  MySQL:      DATE_ADD(col, INTERVAL 30 DAY)
  SQL Server: DATEADD(day, 30, col)

Days between dates:
  PostgreSQL: end_date - start_date
  MySQL:      DATEDIFF(end, start)
  SQL Server: DATEDIFF(day, start, end)

Format date:
  PostgreSQL: TO_CHAR(col, 'DD/MM/YYYY')
  MySQL:      DATE_FORMAT(col, '%d/%m/%Y')
  SQL Server: FORMAT(col, 'dd/MM/yyyy')

Truncate to month start:
  PostgreSQL: DATE_TRUNC('month', NOW())
  MySQL:      DATE_FORMAT(NOW(), '%Y-%m-01')
  SQL Server: DATEADD(day, 1-DAY(GETDATE()), CAST(GETDATE() AS DATE))
```

---

## ✅ Real-World Use Cases

```sql
-- 1. Orders in the last 7 days
SELECT * FROM orders
WHERE created_at >= NOW() - INTERVAL '7 days';

-- 2. Employees with anniversary today
SELECT name, hire_date
FROM employees
WHERE EXTRACT(MONTH FROM hire_date) = EXTRACT(MONTH FROM NOW())
  AND EXTRACT(DAY   FROM hire_date) = EXTRACT(DAY   FROM NOW());

-- 3. Monthly active users
SELECT
    TO_CHAR(login_at, 'YYYY-MM') AS month,
    COUNT(DISTINCT user_id)      AS mau
FROM user_logins
GROUP BY TO_CHAR(login_at, 'YYYY-MM')
ORDER BY month;

-- 4. Overdue invoices (due date passed, not paid)
SELECT invoice_id, due_date, CURRENT_DATE - due_date AS days_overdue
FROM invoices
WHERE paid = FALSE AND due_date < CURRENT_DATE
ORDER BY days_overdue DESC;

-- 5. Cohort analysis: users by signup month
SELECT
    DATE_TRUNC('month', created_at) AS signup_month,
    COUNT(*)                         AS new_users
FROM users
GROUP BY signup_month
ORDER BY signup_month;

-- 6. Working days remaining in month
SELECT
    DATE_TRUNC('month', NOW() + INTERVAL '1 month')::DATE - CURRENT_DATE
    AS days_left_in_month;
```

---

## 🔑 Key Takeaways

| Task | PostgreSQL | MySQL | SQL Server |
|------|-----------|-------|------------|
| Current date | `CURRENT_DATE` | `CURDATE()` | `CAST(GETDATE() AS DATE)` |
| Add interval | `+ INTERVAL '...'` | `DATE_ADD(d, INTERVAL n unit)` | `DATEADD(unit, n, d)` |
| Days between | `d2 - d1` | `DATEDIFF(d2, d1)` | `DATEDIFF(day, d1, d2)` |
| Extract part | `EXTRACT(part FROM d)` | `YEAR(d)`, `MONTH(d)` etc. | `DATEPART(part, d)` |
| Format | `TO_CHAR(d, fmt)` | `DATE_FORMAT(d, fmt)` | `FORMAT(d, fmt)` |
| Truncate | `DATE_TRUNC(part, d)` | `DATE_FORMAT` | `DATEADD` tricks |
| Age | `AGE(d)` | `TIMESTAMPDIFF(YEAR, d, NOW())` | `DATEDIFF(year, d, GETDATE())` |

---

**← Previous:** [08 — String Functions](./08-string-functions.md)
**Next →** [10 — Mathematical Functions](./10-math-functions.md)
