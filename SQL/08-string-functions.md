# 08 — String Functions

> **Goal:** Learn built-in string functions to manipulate, search, format, and transform text data.

---

## 📖 Overview

String functions work on `CHAR`, `VARCHAR`, and `TEXT` columns. They are used in `SELECT`, `WHERE`, `ORDER BY`, and `UPDATE` statements.

> 💡 All examples use PostgreSQL syntax. MySQL/SQL Server differences are noted where they differ.

---

## 📏 Basic Functions

### LENGTH / LEN — String Length

```sql
SELECT LENGTH('Hello World');     -- 11 (PostgreSQL / MySQL)
SELECT LEN('Hello World');        -- 11 (SQL Server)

-- Count characters in a column
SELECT name, LENGTH(name) AS name_length
FROM employees
ORDER BY name_length DESC;

-- Find rows with email longer than 50 chars
SELECT email FROM users WHERE LENGTH(email) > 50;
```

### LOWER / UPPER — Case Conversion

```sql
SELECT LOWER('HELLO WORLD');   -- 'hello world'
SELECT UPPER('hello world');   -- 'HELLO WORLD'

-- Case-insensitive search
SELECT * FROM employees
WHERE LOWER(name) = LOWER('alice');

-- Normalize email on insert
UPDATE users SET email = LOWER(email);
```

### TRIM / LTRIM / RTRIM — Remove Whitespace

```sql
SELECT TRIM('  Hello World  ');    -- 'Hello World'
SELECT LTRIM('  Hello World  ');   -- 'Hello World  '
SELECT RTRIM('  Hello World  ');   -- '  Hello World'

-- TRIM with specific character (PostgreSQL)
SELECT TRIM(BOTH 'x' FROM 'xxHelloxx');   -- 'Hello'
SELECT TRIM(LEADING '0' FROM '00042');    -- '42'

-- Useful for cleaning dirty data
UPDATE contacts SET phone = TRIM(phone);
```

---

## ✂️ Extraction Functions

### LEFT / RIGHT — Extract from Start/End

```sql
SELECT LEFT('Hello World', 5);    -- 'Hello'
SELECT RIGHT('Hello World', 5);   -- 'World'

-- Get first 3 chars of department code
SELECT LEFT(dept_code, 3) AS prefix FROM departments;

-- Get last 4 digits of phone
SELECT RIGHT(phone, 4) AS last_four FROM customers;
```

### SUBSTRING / SUBSTR — Extract Portion

```sql
-- SUBSTRING(string, start_position, length)
SELECT SUBSTRING('Hello World', 7, 5);    -- 'World'
SELECT SUBSTRING('Hello World', 1, 5);    -- 'Hello'

-- From position 7 to end (omit length)
SELECT SUBSTRING('Hello World', 7);       -- 'World'

-- MySQL alternative: SUBSTR (same as SUBSTRING)
SELECT SUBSTR('Hello World', 7, 5);       -- 'World'

-- Extract year from a date string
SELECT SUBSTRING(hire_date::TEXT, 1, 4) AS hire_year FROM employees;
-- Better: use EXTRACT(YEAR FROM hire_date) instead
```

---

## 🔎 Search Functions

### POSITION / CHARINDEX / INSTR — Find Substring Position

```sql
-- PostgreSQL: POSITION(substring IN string) — returns 1-based index, 0 if not found
SELECT POSITION('World' IN 'Hello World');   -- 7
SELECT POSITION('xyz' IN 'Hello World');     -- 0 (not found)

-- SQL Server: CHARINDEX(substring, string [, start])
SELECT CHARINDEX('World', 'Hello World');    -- 7

-- MySQL: INSTR(string, substring)
SELECT INSTR('Hello World', 'World');        -- 7

-- Use case: find rows where email has no '@'
SELECT email FROM users WHERE POSITION('@' IN email) = 0;
```

### PATINDEX — Pattern-Based Position (SQL Server)

```sql
-- SQL Server: find position of first digit
SELECT PATINDEX('%[0-9]%', 'abc123def');    -- 4
SELECT PATINDEX('%@%.%', 'user@email.com'); -- 5 (@ position)
```

---

## 🔧 Modification Functions

### REPLACE — Substitute Substring

```sql
SELECT REPLACE('Hello World', 'World', 'SQL');    -- 'Hello SQL'
SELECT REPLACE('aababab', 'ab', 'X');             -- 'aXXX'

-- Remove all spaces
SELECT REPLACE('Hello World', ' ', '');           -- 'HelloWorld'

-- Anonymize phone numbers
UPDATE customers
SET phone = REPLACE(phone, SUBSTRING(phone, 1, 6), '******')
WHERE id = 1;
```

### CONCAT / || — Concatenate Strings

```sql
-- CONCAT (all RDBMS)
SELECT CONCAT('Hello', ' ', 'World');             -- 'Hello World'
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM employees;

-- || operator (PostgreSQL, SQLite, Oracle)
SELECT 'Hello' || ' ' || 'World';                -- 'Hello World'

-- ⚠️ NULL behavior: CONCAT ignores NULLs in MySQL, but || with NULL = NULL in PG
SELECT CONCAT('Hello', NULL, 'World');            -- 'HelloWorld' (MySQL)
SELECT 'Hello' || NULL || 'World';               -- NULL (PostgreSQL)
-- Use COALESCE for safe concatenation:
SELECT CONCAT(first_name, ' ', COALESCE(middle_name, ''), ' ', last_name) FROM employees;
```

### CONCAT_WS — Concatenate with Separator

```sql
-- CONCAT_WS(separator, str1, str2, ...) — ignores NULLs
SELECT CONCAT_WS(', ', 'Alice', 'Smith', 'PhD');  -- 'Alice, Smith, PhD'
SELECT CONCAT_WS('-', '2025', '01', '15');         -- '2025-01-15'

-- Build full name, skipping NULL middle name
SELECT CONCAT_WS(' ', first_name, middle_name, last_name) AS full_name
FROM employees;
-- middle_name NULL is automatically skipped
```

### STUFF — Delete and Insert (SQL Server)

```sql
-- STUFF(string, start, length, replacement)
-- Delete 5 chars at position 7, insert 'SQL'
SELECT STUFF('Hello World', 7, 5, 'SQL');         -- 'Hello SQL'

-- Mask credit card: replace middle digits
SELECT STUFF(cc_number, 5, 8, '********') AS masked FROM payments;
-- '1234 5678 9012 3456' → '1234 ******** 3456' (adjust positions)
```

---

## 🧰 Utility Functions

### ASCII / CHAR — Character Codes

```sql
SELECT ASCII('A');     -- 65
SELECT ASCII('a');     -- 97
SELECT CHAR(65);       -- 'A'   (SQL Server / MySQL)
SELECT CHR(65);        -- 'A'   (PostgreSQL / Oracle)

-- Check if character is uppercase
SELECT name FROM employees WHERE ASCII(LEFT(name, 1)) BETWEEN 65 AND 90;
```

### REVERSE — Reverse a String

```sql
SELECT REVERSE('Hello');    -- 'olleH'

-- Palindrome check
SELECT word FROM words WHERE word = REVERSE(word);
```

### REPLICATE / REPEAT — Repeat String

```sql
SELECT REPLICATE('ab', 3);    -- 'ababab'  (SQL Server)
SELECT REPEAT('ab', 3);       -- 'ababab'  (PostgreSQL / MySQL)

-- Left-pad a number with zeros
SELECT REPEAT('0', 5 - LENGTH(CAST(id AS TEXT))) || CAST(id AS TEXT)
FROM products;
-- id=7 → '00007'
-- Better: use LPAD
SELECT LPAD(CAST(id AS TEXT), 5, '0') FROM products;  -- PostgreSQL/MySQL
```

### LPAD / RPAD — Left/Right Pad

```sql
SELECT LPAD('42', 6, '0');       -- '000042'   (left pad with zeros)
SELECT RPAD('Hello', 10, '.');   -- 'Hello.....' (right pad with dots)

-- Format invoice numbers
SELECT LPAD(CAST(invoice_id AS TEXT), 8, '0') AS invoice_number FROM invoices;
-- invoice_id=123 → '00000123'
```

### SPACE — Generate Spaces

```sql
SELECT 'Hello' || SPACE(5) || 'World';   -- 'Hello     World'
```

### SOUNDEX — Phonetic Encoding

```sql
SELECT SOUNDEX('Smith');    -- S530
SELECT SOUNDEX('Smyth');    -- S530  (same! sounds like Smith)

-- Find phonetically similar names
SELECT name FROM customers WHERE SOUNDEX(name) = SOUNDEX('Smith');
-- Returns: Smith, Smyth, Smit, etc.
```

### FORMAT — Number/Date Formatting

```sql
-- MySQL: FORMAT(number, decimals)
SELECT FORMAT(1234567.891, 2);    -- '1,234,567.89'

-- SQL Server: FORMAT(value, format_string)
SELECT FORMAT(1234567.891, 'N2');         -- '1,234,567.89'
SELECT FORMAT(GETDATE(), 'dd/MM/yyyy');   -- '15/01/2025'

-- PostgreSQL: TO_CHAR
SELECT TO_CHAR(1234567.891, 'FM9,999,999.99');  -- '1,234,567.89'
SELECT TO_CHAR(NOW(), 'DD/MM/YYYY');             -- '15/01/2025'
```

---

## 🔗 Useful Combinations

```sql
-- Clean and standardize email addresses
UPDATE users
SET email = LOWER(TRIM(email));

-- Extract domain from email
SELECT
    email,
    SUBSTRING(email, POSITION('@' IN email) + 1) AS domain
FROM users;
-- 'alice@gmail.com' → 'gmail.com'

-- Create URL slug from title
SELECT
    title,
    LOWER(REPLACE(TRIM(title), ' ', '-')) AS slug
FROM articles;
-- 'Hello World Article' → 'hello-world-article'

-- Mask middle of phone number
SELECT
    phone,
    LEFT(phone, 3) || REPEAT('*', LENGTH(phone) - 6) || RIGHT(phone, 3) AS masked
FROM customers;
-- '9876543210' → '987****210'

-- Format full name: "SMITH, John"
SELECT
    UPPER(last_name) || ', ' || first_name AS formatted_name
FROM employees
ORDER BY last_name;
```

---

## 📋 Cross-RDBMS Quick Reference

| Function | PostgreSQL | MySQL | SQL Server |
|----------|-----------|-------|------------|
| Length | `LENGTH(s)` | `LENGTH(s)` | `LEN(s)` |
| Find position | `POSITION(x IN s)` | `INSTR(s, x)` | `CHARINDEX(x, s)` |
| Substring | `SUBSTRING(s,p,n)` | `SUBSTRING(s,p,n)` | `SUBSTRING(s,p,n)` |
| Concat | `CONCAT()` / `\|\|` | `CONCAT()` | `CONCAT()` / `+` |
| Repeat | `REPEAT(s, n)` | `REPEAT(s, n)` | `REPLICATE(s, n)` |
| Pad left | `LPAD(s, n, c)` | `LPAD(s, n, c)` | (use REPLICATE+STUFF) |
| Char from code | `CHR(n)` | `CHAR(n)` | `CHAR(n)` |
| Format | `TO_CHAR()` | `FORMAT()` | `FORMAT()` |
| Reverse | `REVERSE(s)` | `REVERSE(s)` | `REVERSE(s)` |
| Case insensitive LIKE | `ILIKE` | LIKE (default ci) | LIKE (depends on collation) |

---

## 🔑 Key Takeaways

| Function | Use Case |
|----------|---------|
| `LENGTH/LEN` | Validate or compare string sizes |
| `UPPER/LOWER` | Normalize for case-insensitive comparison |
| `TRIM/LTRIM/RTRIM` | Clean whitespace from user input |
| `SUBSTRING` | Extract parts of a string |
| `CONCAT / CONCAT_WS` | Build formatted strings; WS ignores NULLs |
| `REPLACE` | Sanitize or transform content |
| `POSITION/CHARINDEX` | Find where a substring appears |
| `LPAD/RPAD` | Format codes and numbers with padding |
| `SOUNDEX` | Fuzzy phonetic matching |
| `REVERSE` | Palindrome checks, creative queries |

---

**← Previous:** [07 — DQL: SELECT & Filtering](./07-dql-select.md)
**Next →** [09 — Date & Time Functions](./09-datetime-functions.md)
