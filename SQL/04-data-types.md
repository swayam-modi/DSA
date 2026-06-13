# 04 — Data Types

> **Goal:** Know the available data types in SQL so you can design your columns correctly and efficiently.

---

## 📖 Why Data Types Matter

Choosing the right data type:
- **Saves storage** (TINYINT vs BIGINT for age)
- **Enforces domain integrity** (DATE rejects "hello")
- **Enables fast comparisons** (INT comparison is faster than VARCHAR)
- **Prevents silent bugs** (storing 3.999 as INT becomes 3)

---

## 🔢 Numeric Types

### Integer Types

| Type | Storage | Range | Use Case |
|------|---------|-------|----------|
| `TINYINT` | 1 byte | 0–255 (unsigned) / -128 to 127 | Boolean-like, status codes |
| `SMALLINT` | 2 bytes | -32,768 to 32,767 | Age, small counts |
| `INT` / `INTEGER` | 4 bytes | -2.1B to 2.1B | Most IDs, quantities |
| `BIGINT` | 8 bytes | -9.2×10¹⁸ to 9.2×10¹⁸ | High-volume IDs, timestamps in ms |

```sql
CREATE TABLE products (
    id       SERIAL      PRIMARY KEY,   -- auto-increment INT
    stock    SMALLINT    NOT NULL DEFAULT 0,
    sold     INT         NOT NULL DEFAULT 0,
    category TINYINT,
    views    BIGINT      DEFAULT 0
);
```

### Decimal / Fixed-Point Types

| Type | Storage | Description |
|------|---------|-------------|
| `DECIMAL(p, s)` | Varies | Exact. p=precision (total digits), s=scale (decimals) |
| `NUMERIC(p, s)` | Varies | Same as DECIMAL in most RDBMS |

```sql
-- DECIMAL(10, 2) means: up to 10 total digits, 2 after decimal
-- Max value: 99999999.99

CREATE TABLE financials (
    price       DECIMAL(10, 2),    -- e.g. 12345678.99
    tax_rate    DECIMAL(5,  4),    -- e.g. 0.0875 (8.75%)
    total       NUMERIC(15, 2)     -- e.g. 9999999999999.99
);
```

> ✅ Use DECIMAL/NUMERIC for money — never FLOAT/DOUBLE (floating point errors!)

### Floating-Point Types

| Type | Storage | Precision | Use Case |
|------|---------|-----------|----------|
| `FLOAT` / `REAL` | 4 bytes | ~7 decimal digits | Scientific calculations |
| `DOUBLE` / `DOUBLE PRECISION` | 8 bytes | ~15 decimal digits | Statistics, ML features |

```sql
CREATE TABLE measurements (
    temperature  FLOAT,       -- ok for sensors
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION
);

-- ⚠️ Floating point pitfall:
SELECT 0.1 + 0.2;  -- Returns 0.30000000000000004 (not 0.3!)
-- Use DECIMAL for money!
```

---

## 🔤 Character / String Types

| Type | Description | Use Case |
|------|-------------|----------|
| `CHAR(n)` | Fixed-length, padded with spaces | Country codes, status codes |
| `VARCHAR(n)` | Variable-length, up to n chars | Names, emails, titles |
| `TEXT` | Unlimited length | Long descriptions, content |

```
CHAR(5) stores 'AB' as 'AB   ' (padded to 5 chars) — wastes space
VARCHAR(5) stores 'AB' as 'AB' (only 2 chars used) — efficient
TEXT stores unlimited characters — no length limit
```

```sql
CREATE TABLE articles (
    id           SERIAL       PRIMARY KEY,
    slug         VARCHAR(200) UNIQUE NOT NULL,    -- URL slug
    title        VARCHAR(500) NOT NULL,
    country_code CHAR(2)      NOT NULL,            -- 'US', 'IN'
    content      TEXT,                             -- full article body
    status       CHAR(1)      DEFAULT 'D'          -- D=Draft, P=Published
);
```

### CHAR vs VARCHAR vs TEXT

```
Storage comparison for value 'Hello' (5 chars):
┌──────────────┬──────────────┬─────────────────────────────────┐
│ Type         │ Stored As    │ Notes                           │
├──────────────┼──────────────┼─────────────────────────────────┤
│ CHAR(20)     │ 'Hello     ' │ Padded to 20 chars — 20 bytes  │
│ VARCHAR(20)  │ 'Hello'      │ 5 chars stored + 1-2 len bytes │
│ TEXT         │ 'Hello'      │ No limit, stored externally     │
└──────────────┴──────────────┴─────────────────────────────────┘
```

---

## 📅 Date & Time Types

| Type | Format | Example | Storage |
|------|--------|---------|---------|
| `DATE` | YYYY-MM-DD | 2024-12-25 | 4 bytes |
| `TIME` | HH:MM:SS | 14:30:00 | 8 bytes |
| `DATETIME` | YYYY-MM-DD HH:MM:SS | 2024-12-25 14:30:00 | 8 bytes |
| `TIMESTAMP` | YYYY-MM-DD HH:MM:SS±TZ | 2024-12-25 14:30:00+05:30 | 8 bytes |
| `INTERVAL` | duration | '2 years 3 months' | 16 bytes (PG) |

```sql
CREATE TABLE events (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(200),
    event_date  DATE           NOT NULL,           -- just the date
    start_time  TIME           NOT NULL,           -- time of day
    created_at  TIMESTAMP      DEFAULT NOW(),      -- with timezone
    duration    INTERVAL                           -- e.g. '2 hours'
);

-- Examples:
INSERT INTO events (title, event_date, start_time, duration)
VALUES ('Conference', '2025-03-15', '09:00:00', '8 hours');
```

### TIMESTAMP vs DATETIME

| | TIMESTAMP | DATETIME |
|--|-----------|----------|
| Timezone | Stored as UTC, converted on read | Stored as-is, no timezone |
| Range | 1970–2038 (MySQL) / 4713 BC–5874897 AD (PG) | Much wider range |
| Best for | "when did this happen" (audit) | "what time in local context" |

```sql
-- PostgreSQL: TIMESTAMPTZ stores UTC, displays in session timezone
SET timezone = 'Asia/Kolkata';
SELECT NOW();  -- 2025-03-15 14:30:00+05:30

SET timezone = 'UTC';
SELECT NOW();  -- 2025-03-15 09:00:00+00:00  (same moment, different display)
```

---

## ✅ Boolean Types

| Type | Values | Notes |
|------|--------|-------|
| `BOOLEAN` | TRUE / FALSE / NULL | PostgreSQL native |
| `BIT` | 1 / 0 | SQL Server, MySQL |
| `TINYINT(1)` | 1 / 0 | MySQL (no native BOOLEAN) |

```sql
-- PostgreSQL
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    is_active   BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    is_admin    BOOLEAN DEFAULT FALSE
);

INSERT INTO users DEFAULT VALUES;
SELECT id, is_active FROM users WHERE is_active = TRUE;
SELECT id, is_active FROM users WHERE is_active;         -- same as = TRUE
SELECT id, is_active FROM users WHERE NOT is_active;     -- WHERE is_active = FALSE
```

---

## 🗂️ Binary Types

Store raw binary data (images, files, blobs).

| Type | RDBMS | Use Case |
|------|-------|----------|
| `BLOB` | MySQL, SQLite | Binary large objects |
| `BYTEA` | PostgreSQL | Binary data |
| `VARBINARY(n)` | SQL Server, MySQL | Variable-length binary |
| `IMAGE` | SQL Server (legacy) | Use VARBINARY(MAX) instead |

```sql
-- PostgreSQL
CREATE TABLE files (
    id          SERIAL PRIMARY KEY,
    filename    VARCHAR(255) NOT NULL,
    content     BYTEA,           -- raw binary file content
    mime_type   VARCHAR(100)
);

-- MySQL
CREATE TABLE files (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    filename    VARCHAR(255) NOT NULL,
    content     LONGBLOB     -- up to 4GB
);
```

> ⚠️ **Best Practice:** Store files on disk/S3 and only save the file **path/URL** in the DB. Storing large BLOBs in the DB degrades performance.

---

## 🔷 JSON Types

Modern RDBMS support native JSON storage and querying.

| Type | RDBMS | Notes |
|------|-------|-------|
| `JSON` | PostgreSQL, MySQL 5.7+ | Stored as text, validated on insert |
| `JSONB` | PostgreSQL | Stored as binary — faster queries, supports indexes |

```sql
-- PostgreSQL: JSONB (preferred)
CREATE TABLE orders (
    id         SERIAL  PRIMARY KEY,
    customer   JSONB   NOT NULL,     -- {"name": "Alice", "email": "..."}
    items      JSONB   NOT NULL,     -- [{"product": "Laptop", "qty": 1}]
    metadata   JSONB
);

-- Insert
INSERT INTO orders (customer, items) VALUES
    ('{"name": "Alice", "email": "alice@email.com"}',
     '[{"product": "Laptop", "qty": 1, "price": 999.99}]');

-- Query JSON fields
SELECT customer->>'name' AS customer_name
FROM orders;

-- Filter on JSON field
SELECT * FROM orders
WHERE customer->>'email' = 'alice@email.com';

-- Index on JSON field (JSONB only)
CREATE INDEX idx_customer_email ON orders ((customer->>'email'));
```

---

## 🗺️ Data Type Decision Guide

```
What kind of data?
│
├── Whole numbers?
│   ├── Small (age, rating 1-5)     → SMALLINT / TINYINT
│   ├── Normal (IDs, quantities)    → INT
│   └── Large (timestamps, rows)    → BIGINT
│
├── Decimal numbers?
│   ├── Money / exact               → DECIMAL(p, s)  ← ALWAYS for money
│   └── Scientific / approximate   → FLOAT / DOUBLE
│
├── Text?
│   ├── Fixed length (code, gender) → CHAR(n)
│   ├── Variable, bounded (name)    → VARCHAR(n)
│   └── Long / unbounded (content)  → TEXT
│
├── Date / Time?
│   ├── Date only                   → DATE
│   ├── Time only                   → TIME
│   ├── Date + Time (no TZ)         → DATETIME
│   └── Date + Time + TZ (audit)    → TIMESTAMP / TIMESTAMPTZ
│
├── True/False?                     → BOOLEAN
│
├── Raw binary (file)?              → BYTEA / BLOB (or store path)
│
└── Structured sub-document?        → JSONB (PostgreSQL) / JSON
```

---

## 🔑 Key Takeaways

| Rule | Reason |
|------|--------|
| Use `DECIMAL` for money | FLOAT has rounding errors |
| Use `VARCHAR` over `CHAR` for variable text | Saves space |
| Use `TIMESTAMPTZ` for audit timestamps | Timezone-aware |
| Use `BOOLEAN` not `TINYINT` | Clarity |
| Don't store files as BLOBs in production | Performance |
| Use `JSONB` over `JSON` in PostgreSQL | Binary = faster queries + indexable |
| Match type size to your data | Don't use BIGINT for age |

---

**← Previous:** [03 — Data Integrity](./03-data-integrity.md)
**Next →** [05 — Database Objects (DDL)](./05-database-objects-ddl.md)
