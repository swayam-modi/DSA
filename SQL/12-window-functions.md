# 12 — Window Functions

> **Goal:** Use window functions to perform calculations across related rows without collapsing them into a single row like GROUP BY does.

---

## 📖 What are Window Functions?

A **window function** performs a calculation across a **set of table rows related to the current row** — without collapsing rows into a single result like `GROUP BY`.

```
Regular aggregate (collapses rows):    Window function (keeps all rows):
──────────────────────────────────     ─────────────────────────────────
SELECT dept, AVG(salary)               SELECT name, dept, salary,
FROM employees                                AVG(salary) OVER (PARTITION BY dept)
GROUP BY dept;                         FROM employees;

┌──────────────┬────────┐              ┌────────┬──────────────┬────────┬──────────┐
│ dept         │ avg    │              │ name   │ dept         │ salary │ dept_avg │
├──────────────┼────────┤              ├────────┼──────────────┼────────┼──────────┤
│ Engineering  │ 85000  │              │ Alice  │ Engineering  │ 90000  │  85000   │
│ HR           │ 65000  │              │ Carol  │ Engineering  │ 85000  │  85000   │
│ Marketing    │ 75000  │              │ Frank  │ Engineering  │ 95000  │  85000   │
└──────────────┴────────┘              │ Hank   │ Engineering  │ 70000  │  85000   │
                                       │ Bob    │ Marketing    │ 72000  │  75000   │
 3 rows returned                       │ Eve    │ Marketing    │ 78000  │  75000   │
                                       │ David  │ HR           │ 65000  │  65000   │
                                       └────────┴──────────────┴────────┴──────────┘
                                        7 rows returned (all rows preserved!)
```

### Syntax

```sql
function_name(args) OVER (
    [PARTITION BY column_list]
    [ORDER BY column_list]
    [ROWS | RANGE BETWEEN frame_start AND frame_end]
)
```

| Clause | Purpose |
|--------|---------|
| `PARTITION BY` | Like GROUP BY for the window — divides rows into groups |
| `ORDER BY` | Ordering within each partition |
| `ROWS/RANGE BETWEEN` | Defines which rows are included in the window frame |

---

## 🏆 Ranking Functions

### ROW_NUMBER()

Assigns a **unique sequential number** to each row within a partition. Ties get different numbers.

```sql
SELECT
    name,
    dept,
    salary,
    ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC) AS row_num
FROM employees;

/*
name   | dept         | salary | row_num
───────┼──────────────┼────────┼─────────
Frank  | Engineering  | 95000  |   1
Alice  | Engineering  | 90000  |   2
Carol  | Engineering  | 85000  |   3
Hank   | Engineering  | 70000  |   4
Eve    | Marketing    | 78000  |   1    ← restarts per partition
Bob    | Marketing    | 72000  |   2
David  | HR           | 65000  |   1
Grace  | HR           | NULL   |   2    ← NULLs go last (desc)
*/
```

**Use case:** Get top N per group (pagination)

```sql
-- Top 2 highest-paid employees per department
SELECT * FROM (
    SELECT
        name, dept, salary,
        ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC) AS rn
    FROM employees
) ranked
WHERE rn <= 2;
```

---

### RANK()

Assigns rank with **gaps** for ties (same as Olympic rankings).

```sql
SELECT
    name, salary,
    RANK() OVER (ORDER BY salary DESC) AS rank
FROM employees;

/*
name  | salary | rank
──────┼────────┼──────
Frank | 95000  |  1
Alice | 90000  |  2
Carol | 85000  |  3
Eve   | 78000  |  4
Bob   | 72000  |  5
Hank  | 70000  |  6
David | 65000  |  7
Grace | NULL   |  8   ← NULLs last
*/

-- If two people tie at salary 90000, both get rank 2, next is 4 (gap):
/*
Frank | 95000  |  1
Alice | 90000  |  2   ← tie
Carol | 90000  |  2   ← tie
Hank  | 70000  |  4   ← gap! 3 is skipped
*/
```

---

### DENSE_RANK()

Assigns rank **without gaps** for ties.

```sql
SELECT
    name, salary,
    DENSE_RANK() OVER (ORDER BY salary DESC) AS dense_rank
FROM employees;

-- With ties at 90000:
/*
Frank | 95000  |  1
Alice | 90000  |  2   ← tie
Carol | 90000  |  2   ← tie
Hank  | 70000  |  3   ← no gap! Next rank is 3
*/
```

### RANK vs DENSE_RANK vs ROW_NUMBER

```
Scores: 100, 90, 90, 70
┌───────────┬─────────────┬──────────────┬────────────┐
│ Score     │ ROW_NUMBER  │ RANK         │ DENSE_RANK │
├───────────┼─────────────┼──────────────┼────────────┤
│ 100       │      1      │      1       │      1     │
│  90       │      2      │      2       │      2     │
│  90       │      3      │      2       │      2     │ ← tie
│  70       │      4      │      4       │      3     │
└───────────┴─────────────┴──────────────┴────────────┘
                             Gap (3 skipped)  No gap
```

---

### NTILE(n)

Divides rows into **n equal buckets**.

```sql
SELECT
    name, salary,
    NTILE(4) OVER (ORDER BY salary DESC) AS quartile
FROM employees;

/*
Frank | 95000  |  1   ← Q1 (top 25%)
Alice | 90000  |  1
Carol | 85000  |  2   ← Q2
Eve   | 78000  |  2
Bob   | 72000  |  3   ← Q3
Hank  | 70000  |  3
David | 65000  |  4   ← Q4 (bottom 25%)
Grace | NULL   |  4
*/
```

---

## 📈 Analytical Functions

### LEAD() — Next Row's Value

```sql
SELECT
    name,
    hire_date,
    LEAD(hire_date) OVER (ORDER BY hire_date) AS next_hire_date,
    LEAD(hire_date, 2) OVER (ORDER BY hire_date) AS hire_two_after,
    LEAD(hire_date, 1, '9999-12-31') OVER (ORDER BY hire_date) AS next_or_max
FROM employees;

/*
name  | hire_date  | next_hire_date | hire_two_after
──────┼────────────┼────────────────┼───────────────
Frank | 2018-09-01 | 2019-06-01     | 2020-01-15
Bob   | 2019-06-01 | 2020-01-15     | 2020-11-05
Alice | 2020-01-15 | 2020-11-05     | 2021-03-22
Eve   | 2020-11-05 | 2021-03-22     | 2022-07-10
Carol | 2021-03-22 | 2022-07-10     | 2023-01-20
David | 2022-07-10 | 2023-01-20     | 2023-05-15
Grace | 2023-01-20 | 2023-05-15     | NULL
Hank  | 2023-05-15 | NULL           | NULL
*/
```

**Use case:** Gap analysis, compare current row to the next row.

```sql
-- Days between consecutive hires
SELECT
    name,
    hire_date,
    LEAD(hire_date) OVER (ORDER BY hire_date) - hire_date AS days_to_next_hire
FROM employees;
```

---

### LAG() — Previous Row's Value

```sql
SELECT
    name,
    salary,
    LAG(salary) OVER (ORDER BY salary DESC)   AS prev_salary,
    salary - LAG(salary) OVER (ORDER BY salary DESC) AS diff_from_prev
FROM employees;

-- Month-over-month revenue growth
SELECT
    month,
    revenue,
    LAG(revenue) OVER (ORDER BY month)                        AS prev_month,
    ROUND(100.0 * (revenue - LAG(revenue) OVER (ORDER BY month))
          / NULLIF(LAG(revenue) OVER (ORDER BY month), 0), 2) AS pct_change
FROM monthly_revenue;
```

---

### FIRST_VALUE() / LAST_VALUE()

Get the first or last value in the window.

```sql
SELECT
    name,
    dept,
    salary,
    FIRST_VALUE(name) OVER (PARTITION BY dept ORDER BY salary DESC) AS top_earner,
    LAST_VALUE(name)  OVER (
        PARTITION BY dept ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS lowest_earner
FROM employees;

/*
The ROWS BETWEEN clause is CRITICAL for LAST_VALUE!
Without it, the default frame only goes to the current row,
so LAST_VALUE just returns the current row's value.
*/
```

---

## 🖼️ Window Frames

The **window frame** defines which rows relative to the current row are included in the calculation.

```
ROWS BETWEEN frame_start AND frame_end

frame_start / frame_end options:
  UNBOUNDED PRECEDING   → first row of the partition
  n PRECEDING           → n rows before current row
  CURRENT ROW           → the current row
  n FOLLOWING           → n rows after current row
  UNBOUNDED FOLLOWING   → last row of the partition
```

### Default Frame Behavior

```
With ORDER BY:    RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
Without ORDER BY: ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
```

### Running Total (Cumulative SUM)

```sql
SELECT
    name,
    hire_date,
    salary,
    SUM(salary) OVER (
        ORDER BY hire_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total
FROM employees
ORDER BY hire_date;

/*
Frank | 2018-09-01 | 95000 |  95000
Bob   | 2019-06-01 | 72000 | 167000
Alice | 2020-01-15 | 90000 | 257000
Eve   | 2020-11-05 | 78000 | 335000
Carol | 2021-03-22 | 85000 | 420000
David | 2022-07-10 | 65000 | 485000
Grace | 2023-01-20 | NULL  | 485000
Hank  | 2023-05-15 | 70000 | 555000
*/
```

### Moving Average (Rolling Average)

```sql
SELECT
    sale_date,
    daily_revenue,
    ROUND(AVG(daily_revenue) OVER (
        ORDER BY sale_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ), 2) AS rolling_7day_avg
FROM daily_sales;
-- Average of current day + previous 6 days
```

### Sliding Window

```sql
-- Sum of current row ± 1 row (3-row window)
SELECT
    name, salary,
    SUM(salary) OVER (
        ORDER BY salary
        ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING
    ) AS local_sum
FROM employees;
```

---

## 🗺️ PARTITION BY Explained

```
No PARTITION BY:                     PARTITION BY dept:
──────────────────────────────────   ──────────────────────────────────────────
All rows = one window                Rows split into windows by dept:

┌─────────────────────────┐          ┌─────────────┐ ┌──────────┐ ┌──────────┐
│ All 8 employees         │          │ Engineering │ │Marketing │ │   HR     │
│ RANK() over everyone    │          │ Frank (1)   │ │ Eve  (1) │ │David (1) │
└─────────────────────────┘          │ Alice (2)   │ │ Bob  (2) │ │Grace (2) │
                                     │ Carol (3)   │ └──────────┘ └──────────┘
                                     │ Hank  (4)   │
                                     └─────────────┘
```

```sql
-- Global rank
SELECT name, RANK() OVER (ORDER BY salary DESC) AS global_rank FROM employees;

-- Rank within department
SELECT name, dept, RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS dept_rank
FROM employees;
```

---

## ✅ Practical Examples

```sql
-- 1. Percentile rank of each employee's salary
SELECT
    name, salary,
    PERCENT_RANK() OVER (ORDER BY salary)    AS pct_rank,  -- 0 to 1
    CUME_DIST()    OVER (ORDER BY salary)    AS cume_dist  -- cumulative distribution
FROM employees;

-- 2. Compare each employee's salary to their department's average
SELECT
    name,
    dept,
    salary,
    ROUND(AVG(salary) OVER (PARTITION BY dept), 2)                AS dept_avg,
    salary - AVG(salary) OVER (PARTITION BY dept)                  AS diff_from_avg,
    ROUND(salary / AVG(salary) OVER (PARTITION BY dept) * 100, 1) AS pct_of_avg
FROM employees
WHERE salary IS NOT NULL;

-- 3. Detect salary jumps between consecutive hires
SELECT
    name,
    hire_date,
    salary,
    salary - LAG(salary) OVER (ORDER BY hire_date) AS salary_change
FROM employees
ORDER BY hire_date;

-- 4. Number each customer's orders
SELECT
    order_id,
    customer_id,
    created_at,
    ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY created_at) AS order_num
FROM orders;
-- Lets you easily find each customer's FIRST order (order_num = 1)

-- 5. Median salary (PostgreSQL)
SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM employees;
```

---

## 🔑 Key Takeaways

| Function | Purpose | Notes |
|----------|---------|-------|
| `ROW_NUMBER()` | Unique sequential number per partition | No ties |
| `RANK()` | Rank with gaps for ties | Gaps after ties |
| `DENSE_RANK()` | Rank without gaps for ties | No gaps |
| `NTILE(n)` | Divide into n equal buckets | Quartiles, deciles |
| `LEAD(col, n)` | Look n rows ahead | NULL at partition end |
| `LAG(col, n)` | Look n rows back | NULL at partition start |
| `FIRST_VALUE(col)` | First value in window frame | Frame matters! |
| `LAST_VALUE(col)` | Last value in window frame | Always use UNBOUNDED FOLLOWING |
| `SUM() OVER (...)` | Running total or windowed sum | Use ROWS BETWEEN for control |
| `AVG() OVER (...)` | Moving average | n PRECEDING AND CURRENT ROW |

**Key rules:**
- Window functions run in the `SELECT` phase (after WHERE, before ORDER BY)
- They **do not collapse rows** — all rows are returned
- `PARTITION BY` is optional — without it, the whole result set is one window
- `ORDER BY` inside `OVER()` is separate from the query's `ORDER BY`
- Always specify frame for `LAST_VALUE` — default frame stops at current row

---

**← Previous:** [11 — Aggregate Functions](./11-aggregate-functions.md)
**Next →** [13 — Joins](./13-joins.md)
