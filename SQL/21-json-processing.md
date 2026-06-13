# 21 — JSON Processing

> **Goal:** Store, query, modify, and export JSON data within the database.

---

## 📖 Why JSON in a Relational Database?

Modern applications often need to store **semi-structured data** (API responses, user preferences, dynamic attributes) alongside relational data. Native JSON support lets you do this without a separate NoSQL database.

```
Use cases:
  • Store user preferences without adding 50 columns
  • Cache API responses from external services
  • Store dynamic product attributes (color, size, specs)
  • Log structured event data
  • Flexible metadata on any entity
```

---

## ✅ JSON vs JSONB (PostgreSQL)

| | JSON | JSONB |
|--|------|-------|
| Storage | Text (as-is) | Binary parsed |
| Size | Smaller | Slightly larger |
| Write speed | Faster | Slightly slower |
| Read speed | Slower (re-parse) | Faster |
| Indexable | No | Yes (GIN index) |
| Key ordering | Preserved | Not preserved |
| Duplicate keys | Allowed (last wins) | Not allowed |

> ✅ **Always use JSONB** in PostgreSQL for better query performance.

---

## 🏗️ Create Table with JSON

```sql
-- PostgreSQL
CREATE TABLE user_profiles (
    id          SERIAL PRIMARY KEY,
    user_id     INT UNIQUE REFERENCES users(id),
    preferences JSONB,               -- user settings
    metadata    JSONB DEFAULT '{}'   -- flexible extra data
);

INSERT INTO user_profiles (user_id, preferences, metadata)
VALUES (
    1,
    '{"theme": "dark", "language": "en", "notifications": {"email": true, "sms": false}}',
    '{"signup_source": "google", "beta_tester": true}'
);
```

---

## 📖 Reading JSON Data

### PostgreSQL JSONB Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `->` | Get JSON field by key (returns JSON) | `preferences->'theme'` → `"dark"` |
| `->>` | Get JSON field as text | `preferences->>'theme'` → `dark` |
| `#>` | Get nested JSON by path | `preferences#>'{notifications,email}'` |
| `#>>` | Get nested JSON as text | `preferences#>>'{notifications,email}'` |
| `@>` | Contains | `preferences @> '{"theme":"dark"}'` |
| `<@` | Is contained by | ... |
| `?` | Key exists | `preferences ? 'theme'` |
| `?&` | All keys exist | `preferences ?& array['theme','language']` |
| `?\|` | Any key exists | `preferences ?\| array['theme','color']` |

```sql
-- Extract single field (as JSON)
SELECT preferences->'theme' FROM user_profiles WHERE user_id = 1;
-- Returns: "dark"  (JSON string with quotes)

-- Extract single field (as text)
SELECT preferences->>'theme' FROM user_profiles WHERE user_id = 1;
-- Returns: dark   (plain text)

-- Nested path (returns JSON)
SELECT preferences#>'{notifications,email}' FROM user_profiles WHERE user_id = 1;
-- Returns: true

-- Nested path (as text)
SELECT preferences#>>'{notifications,email}' FROM user_profiles;

-- Check if key exists
SELECT user_id FROM user_profiles WHERE preferences ? 'theme';

-- Check if JSONB contains a sub-document
SELECT user_id FROM user_profiles
WHERE preferences @> '{"theme": "dark"}';
-- Uses GIN index!

-- Filter by JSON value
SELECT * FROM user_profiles
WHERE preferences->>'language' = 'en';
-- Note: no index for ->> without functional index
-- Better: WHERE preferences @> '{"language": "en"}'  (uses GIN index)
```

---

## ✏️ Modifying JSON (PostgreSQL: jsonb_set)

```sql
-- jsonb_set(target, path, new_value, create_missing)
UPDATE user_profiles
SET preferences = jsonb_set(preferences, '{theme}', '"light"')
WHERE user_id = 1;

-- Set nested key
UPDATE user_profiles
SET preferences = jsonb_set(preferences, '{notifications,sms}', 'true')
WHERE user_id = 1;

-- Add new key (create_missing = true)
UPDATE user_profiles
SET preferences = jsonb_set(preferences, '{font_size}', '14', true)
WHERE user_id = 1;

-- Remove a key
UPDATE user_profiles
SET preferences = preferences - 'font_size'
WHERE user_id = 1;

-- Remove nested key
UPDATE user_profiles
SET preferences = preferences #- '{notifications,sms}'
WHERE user_id = 1;

-- Merge/concat two JSONB objects
UPDATE user_profiles
SET preferences = preferences || '{"color_scheme": "blue"}'::jsonb
WHERE user_id = 1;
```

---

## 📊 JSON Validation

```sql
-- PostgreSQL: inserting invalid JSON raises an error automatically
INSERT INTO user_profiles (user_id, preferences)
VALUES (2, 'not valid json');
-- ERROR: invalid input syntax for type json

-- SQL Server: ISJSON()
SELECT ISJSON('{"key": "value"}');   -- 1 (valid)
SELECT ISJSON('not json');           -- 0 (invalid)

-- Validate before insert (SQL Server)
INSERT INTO events (payload)
SELECT '{"type": "click"}'
WHERE ISJSON('{"type": "click"}') = 1;
```

---

## 🔀 JSON Functions (PostgreSQL)

```sql
-- Build JSON from columns
SELECT json_build_object(
    'id', id,
    'name', name,
    'salary', salary
) FROM employees;

-- Build JSON array
SELECT json_agg(name ORDER BY name) AS names FROM employees;
-- ["Alice","Bob","Carol",...]

-- JSON array of objects
SELECT json_agg(json_build_object('id', id, 'name', name))
FROM employees WHERE dept = 'Engineering';

-- Aggregate to JSONB
SELECT jsonb_agg(name) FROM employees;

-- Extract all keys
SELECT jsonb_object_keys(preferences) FROM user_profiles WHERE user_id = 1;
-- theme, language, notifications

-- Expand JSON object to key-value pairs
SELECT * FROM jsonb_each('{"a":1,"b":2}');
-- key | value
-- ────┼───────
--  a  |  1
--  b  |  2
```

---

## 🔷 JSON to Rows (Unnesting)

### PostgreSQL: jsonb_array_elements

```sql
-- orders with JSON items array
-- orders.items = '[{"product":"Laptop","qty":1},{"product":"Mouse","qty":2}]'

SELECT
    o.id AS order_id,
    item->>'product' AS product,
    (item->>'qty')::INT AS qty
FROM orders o,
LATERAL jsonb_array_elements(o.items) AS item;

-- Result:
-- order_id | product | qty
-- ─────────┼─────────┼────
--     1    | Laptop  |  1
--     1    | Mouse   |  2
```

### SQL Server: OPENJSON

```sql
-- Parse JSON array into rows
SELECT *
FROM OPENJSON('[{"name":"Alice","age":30},{"name":"Bob","age":25}]')
WITH (
    name VARCHAR(50) '$.name',
    age  INT         '$.age'
);

-- name  | age
-- ──────┼────
-- Alice | 30
-- Bob   | 25

-- OPENJSON on a table column
SELECT o.id, item.product, item.qty
FROM orders o
CROSS APPLY OPENJSON(o.items_json)
WITH (
    product VARCHAR(100) '$.product',
    qty     INT          '$.qty'
) AS item;
```

### MySQL: JSON_TABLE

```sql
SELECT *
FROM JSON_TABLE(
    '[{"product":"Laptop","qty":1},{"product":"Mouse","qty":2}]',
    '$[*]' COLUMNS (
        product VARCHAR(100) PATH '$.product',
        qty     INT          PATH '$.qty'
    )
) AS jt;
```

---

## 📤 Exporting as JSON

### PostgreSQL: row_to_json / json_build_object

```sql
-- Convert entire row to JSON
SELECT row_to_json(e) FROM employees e WHERE id = 1;
-- {"id":1,"name":"Alice","dept":"Engineering",...}

-- Build custom JSON structure
SELECT json_build_object(
    'employee', json_build_object('id', id, 'name', name),
    'dept', dept,
    'compensation', json_build_object('salary', salary)
) FROM employees WHERE id = 1;
```

### SQL Server: FOR JSON

```sql
-- FOR JSON PATH: control structure
SELECT id, name, salary
FROM employees
FOR JSON PATH;
-- [{"id":1,"name":"Alice","salary":90000}, ...]

-- FOR JSON AUTO: automatic nesting based on JOINs
SELECT c.name, o.id, o.total
FROM customers c
JOIN orders o ON c.id = o.customer_id
FOR JSON AUTO;
```

### MySQL: JSON_ARRAYAGG / JSON_OBJECTAGG

```sql
SELECT JSON_ARRAYAGG(name) FROM employees;
-- ["Alice", "Bob", "Carol", ...]

SELECT JSON_OBJECTAGG(id, name) FROM employees;
-- {1: "Alice", 2: "Bob", ...}
```

---

## 🏎️ Indexing JSON (PostgreSQL GIN)

```sql
-- GIN index for @> containment queries (most common)
CREATE INDEX idx_profiles_prefs ON user_profiles USING GIN (preferences);

-- Now this query uses the index:
SELECT * FROM user_profiles WHERE preferences @> '{"theme": "dark"}';

-- Functional index for specific path (for ->> queries)
CREATE INDEX idx_profiles_theme ON user_profiles ((preferences->>'theme'));

-- Now this uses the index:
SELECT * FROM user_profiles WHERE preferences->>'theme' = 'dark';
```

---

## 🔑 Key Takeaways

| Operation | PostgreSQL | MySQL | SQL Server |
|-----------|-----------|-------|------------|
| Type | `JSONB` (preferred) / `JSON` | `JSON` | `NVARCHAR(MAX)` + functions |
| Read field | `->>` operator | `JSON_VALUE()` | `JSON_VALUE()` |
| Read nested | `#>>`  | `JSON_EXTRACT()` | `JSON_QUERY()` |
| Modify | `jsonb_set()` | `JSON_SET()` | `JSON_MODIFY()` |
| Contains | `@>` | `JSON_CONTAINS()` | (LIKE or OPENJSON) |
| Array to rows | `jsonb_array_elements()` | `JSON_TABLE()` | `OPENJSON()` |
| Index | GIN index | Functional index | Computed column + index |
| Validate | Auto on INSERT | `JSON_VALID()` | `ISJSON()` |

---

**← Previous:** [20 — Variables & Control Flow](./20-variables-control-flow.md)
**Next →** [22 — Stored Procedures](./22-stored-procedures.md)
