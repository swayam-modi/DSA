# 02 — Relational Database Concepts

> **Goal:** Understand the building blocks of relational databases — tables, keys, relationships, and constraints.

---

## 📖 Tables

A **table** is the fundamental storage unit in an RDBMS. It is a 2D structure made of **rows** and **columns**.

```
Table: employees
┌────┬──────────┬───────────┬────────────┬──────────┐
│ id │ name     │ dept_id   │ salary     │ hired_on │
├────┼──────────┼───────────┼────────────┼──────────┤
│  1 │ Alice    │     10    │  75000.00  │ 2020-01-15│
│  2 │ Bob      │     20    │  82000.00  │ 2019-06-01│
│  3 │ Carol    │     10    │  91000.00  │ 2021-03-22│
└────┴──────────┴───────────┴────────────┴──────────┘
  ▲              ▲                                  ▲
Column(Attribute) Column                         Column

Each horizontal line = 1 Row (Record / Tuple)
Each vertical line  = 1 Column (Attribute / Field)
```

### Rows (Records / Tuples)
- Each row is **one entity instance** (e.g., one employee).
- Also called a **record** or **tuple**.
- Order of rows does not matter in a relational table.

### Columns (Attributes / Fields)
- Each column has a **name** and a **data type**.
- All values in a column must match that type.
- Order of columns does not matter logically (but matters in `SELECT *`).

### Schema
The **schema** defines the table's **structure** — column names, data types, constraints — without containing data.

```sql
-- Schema definition (structure only)
CREATE TABLE employees (
    id        INT           PRIMARY KEY,
    name      VARCHAR(100)  NOT NULL,
    dept_id   INT,
    salary    DECIMAL(10,2),
    hired_on  DATE
);
```

### Metadata
**Metadata** = data about data. The database catalog stores:
- Table names
- Column names and types
- Constraints
- Indexes
- Users and permissions

```sql
-- View metadata in PostgreSQL
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'employees';
```

---

## 🔑 Keys

Keys uniquely identify rows and establish relationships between tables.

### Key Hierarchy Diagram

```
All Columns in a Table
│
├── Superkey            (any set of columns that uniquely identify a row)
│   │
│   ├── Candidate Key   (minimal superkey — no redundant columns)
│   │   │
│   │   ├── Primary Key (chosen candidate key — one per table)
│   │   │
│   │   └── Alternate Key (candidate keys not chosen as PK)
│   │
│   └── (non-minimal superkeys)
│
├── Foreign Key         (references PK/Unique in another table)
│
├── Composite Key       (key made of multiple columns)
│
├── Unique Key          (unique but allows one NULL)
│
├── Surrogate Key       (system-generated: AUTO_INCREMENT, UUID)
│
└── Natural Key         (from real-world data: SSN, email)
```

---

### Primary Key (PK)

The **primary key** uniquely identifies each row. Rules:
- Must be **UNIQUE** — no two rows share the same PK value
- Must be **NOT NULL** — PK cannot be null
- One per table

```sql
CREATE TABLE students (
    student_id  INT         PRIMARY KEY,   -- simple PK
    name        VARCHAR(50) NOT NULL,
    email       VARCHAR(100) UNIQUE
);

-- Or composite PK
CREATE TABLE enrollments (
    student_id  INT,
    course_id   INT,
    enrolled_on DATE,
    PRIMARY KEY (student_id, course_id)    -- composite PK
);
```

```
students
┌────────────┬───────┬──────────────────┐
│ student_id │ name  │ email            │
├────────────┼───────┼──────────────────┤
│    1 ◄──PK │ Alice │ alice@email.com  │
│    2       │ Bob   │ bob@email.com    │
│    3       │ Carol │ carol@email.com  │
└────────────┴───────┴──────────────────┘
     ▲
  Unique + Not Null
```

---

### Foreign Key (FK)

A **foreign key** is a column (or set of columns) in one table that **references the Primary Key of another table**. It enforces **referential integrity**.

```
departments                    employees
┌───────┬──────────┐           ┌────┬───────┬─────────┐
│ id PK │ name     │           │ id │ name  │ dept_id │
├───────┼──────────┤           ├────┼───────┼─────────┤
│  10   │ Finance  │◄──────────│  1 │ Alice │   10   │
│  20   │ IT       │◄──────────│  2 │ Bob   │   20   │
└───────┴──────────┘           │  3 │ Carol │   10   │
                               └────┴───────┴─────────┘
                                              ▲
                                           FK → departments.id
```

```sql
CREATE TABLE employees (
    id       INT PRIMARY KEY,
    name     VARCHAR(100),
    dept_id  INT,
    FOREIGN KEY (dept_id) REFERENCES departments(id)
);
```

> ⚠️ You cannot insert a `dept_id` value that doesn't exist in `departments.id` — the FK constraint blocks it.

---

### Candidate Key

Any **minimal set of columns** that can uniquely identify a row. A table may have multiple candidate keys.

```
students table:
  student_id  → unique, not null → Candidate Key ✅
  email       → unique, not null → Candidate Key ✅
  phone       → unique, not null → Candidate Key ✅

We choose student_id as PRIMARY KEY.
email and phone become ALTERNATE KEYS.
```

---

### Alternate Key

Candidate keys that were **not selected** as the Primary Key. Usually enforced with `UNIQUE NOT NULL`.

```sql
CREATE TABLE students (
    student_id  INT         PRIMARY KEY,   -- chosen PK
    email       VARCHAR(100) UNIQUE NOT NULL,  -- alternate key
    phone       VARCHAR(20)  UNIQUE NOT NULL   -- alternate key
);
```

---

### Composite Key

A key made of **two or more columns** combined. Neither column alone is unique, but the combination is.

```
enrollments
┌────────────┬───────────┬────────────┐
│ student_id │ course_id │ grade      │
├────────────┼───────────┼────────────┤
│     1      │    101    │ A          │
│     1      │    102    │ B+         │  ← student 1 appears twice
│     2      │    101    │ A-         │  ← course 101 appears twice
└────────────┴───────────┴────────────┘
        └──────────────┘
         Composite PK: (student_id + course_id) is unique
```

```sql
PRIMARY KEY (student_id, course_id)
```

---

### Unique Key

A **UNIQUE** constraint ensures all values in a column are distinct. Unlike PK:
- Allows **one NULL** value (in most RDBMS)
- A table can have **multiple** unique constraints

```sql
CREATE TABLE users (
    id       INT         PRIMARY KEY,
    username VARCHAR(50) UNIQUE,    -- unique key
    email    VARCHAR(100) UNIQUE    -- unique key
);
```

---

### Surrogate Key

A **system-generated** identifier with no business meaning. Common approach.

```sql
-- PostgreSQL (SERIAL / GENERATED ALWAYS)
CREATE TABLE products (
    id   SERIAL PRIMARY KEY,         -- auto-incrementing integer
    name VARCHAR(100)
);

-- Or UUID
CREATE TABLE products (
    id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100)
);
```

**Advantages:**
- Stable (doesn't change if business data changes)
- Simple integer JOINs are fast
- No PII in the key

---

### Natural Key

A key derived from **real-world meaningful data**.

| Example Table | Natural Key |
|---------------|-------------|
| countries | country_code ('US', 'IN') |
| products | ISBN (books) |
| persons | SSN (Social Security Number) |

```sql
CREATE TABLE countries (
    country_code CHAR(2) PRIMARY KEY,  -- natural key: 'US', 'IN', 'GB'
    name         VARCHAR(100)
);
```

**Caution:** Natural keys can change (SSN reassigned, email changed), breaking referential integrity.

---

## 🔗 Relationships

Relationships define how tables connect. There are three types.

### One-to-One (1:1)

One record in Table A corresponds to **exactly one** record in Table B.

```
users                          user_profiles
┌────┬──────────┐              ┌────────────┬──────┬────────────┐
│ id │ username │              │ user_id FK │ bio  │ avatar_url │
├────┼──────────┤              ├────────────┼──────┼────────────┤
│  1 │ alice    │─────────────►│     1      │ ...  │ ...        │
│  2 │ bob      │─────────────►│     2      │ ...  │ ...        │
└────┴──────────┘              └────────────┴──────┴────────────┘
```

```sql
CREATE TABLE user_profiles (
    user_id    INT PRIMARY KEY,
    bio        TEXT,
    avatar_url VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**Use case:** Split rarely-accessed data into a separate table for performance.

---

### One-to-Many (1:N)

One record in Table A corresponds to **many** records in Table B.

```
departments (1)          employees (N)
┌────┬──────────┐        ┌────┬───────┬─────────┐
│ id │ name     │        │ id │ name  │ dept_id │
├────┼──────────┤        ├────┼───────┼─────────┤
│ 10 │ Finance  │──┬────►│  1 │ Alice │   10   │
│ 20 │ IT       │  └────►│  3 │ Carol │   10   │
└────┴──────────┘        │  2 │ Bob   │   20   │
                         └────┴───────┴─────────┘
```

The FK goes on the **"many" side** (employees).

**Use case:** Most common relationship. Orders → Order Items, Users → Posts, etc.

---

### Many-to-Many (M:N)

One record in A relates to many in B, and vice versa. Requires a **junction table** (also called bridge, pivot, or linking table).

```
students (M)            enrollments (junction)      courses (N)
┌────┬───────┐          ┌────────────┬──────────┐   ┌──────────┬───────────┐
│ id │ name  │          │ student_id │ course_id│   │ id       │ title     │
├────┼───────┤          ├────────────┼──────────┤   ├──────────┼───────────┤
│  1 │ Alice │────┬────►│     1      │   101    │◄──│  101     │ Math      │
│  2 │ Bob   │    └────►│     1      │   102    │◄──│  102     │ Physics   │
└────┴───────┘   ┌─────►│     2      │   101    │   └──────────┴───────────┘
                 └─────►│     2      │   102    │
                         └────────────┴──────────┘
```

```sql
CREATE TABLE enrollments (
    student_id  INT,
    course_id   INT,
    enrolled_on DATE DEFAULT CURRENT_DATE,
    grade       CHAR(2),
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id)  REFERENCES courses(id)
);
```

**Use case:** Students ↔ Courses, Products ↔ Orders, Actors ↔ Movies.

---

## 🛡️ Constraints

Constraints enforce **rules** on data at the column or table level.

### NOT NULL

Ensures a column **cannot contain NULL**.

```sql
CREATE TABLE employees (
    id    INT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,   -- name is required
    email VARCHAR(100)             -- email is optional
);

-- Violation:
INSERT INTO employees (id, name) VALUES (1, NULL);
-- ERROR: null value in column "name" violates not-null constraint
```

---

### UNIQUE

Ensures all values in a column (or combination) are distinct.

```sql
CREATE TABLE users (
    id       INT PRIMARY KEY,
    email    VARCHAR(100) UNIQUE,          -- single column unique
    username VARCHAR(50)  UNIQUE
);

-- Multi-column unique (combination must be unique)
CREATE TABLE bookings (
    room_id   INT,
    book_date DATE,
    UNIQUE (room_id, book_date)  -- same room can't be booked twice on same day
);
```

---

### PRIMARY KEY

Combines NOT NULL + UNIQUE. One per table.

```sql
CREATE TABLE products (
    product_id INT PRIMARY KEY,
    name       VARCHAR(100)
);
```

---

### FOREIGN KEY

Links to another table's PK. Prevents orphan records.

```sql
CREATE TABLE orders (
    order_id    INT PRIMARY KEY,
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE CASCADE          -- delete orders if customer deleted
        ON UPDATE CASCADE          -- update if customer id changes
);
```

---

### CHECK

Validates data using a custom boolean expression.

```sql
CREATE TABLE employees (
    id     INT PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    salary DECIMAL(10,2) CHECK (salary > 0),          -- must be positive
    age    INT           CHECK (age >= 18 AND age <= 65),
    status VARCHAR(10)   CHECK (status IN ('active', 'inactive', 'suspended'))
);
```

---

### DEFAULT

Provides a **default value** when none is supplied.

```sql
CREATE TABLE orders (
    id           INT PRIMARY KEY,
    status       VARCHAR(20) DEFAULT 'pending',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    is_active    BOOLEAN     DEFAULT TRUE
);

-- Insert without specifying status or created_at:
INSERT INTO orders (id) VALUES (1);
-- status = 'pending', created_at = now() automatically
```

---

### All Constraints Summary

```sql
CREATE TABLE accounts (
    id          SERIAL        PRIMARY KEY,              -- PK = NOT NULL + UNIQUE
    username    VARCHAR(50)   NOT NULL UNIQUE,          -- NOT NULL + UNIQUE
    email       VARCHAR(100)  NOT NULL UNIQUE,          -- NOT NULL + UNIQUE
    age         INT           CHECK (age >= 13),        -- CHECK
    balance     DECIMAL(15,2) DEFAULT 0.00,             -- DEFAULT
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    role_id     INT           REFERENCES roles(id)      -- FOREIGN KEY
);
```

---

## 🔑 Key Takeaways

| Concept | Purpose |
|---------|---------|
| Table | Stores data in rows & columns |
| Row | One entity instance |
| Column | One attribute with a type |
| Schema | Structure/definition of a table |
| Primary Key | Uniquely identifies a row (NOT NULL + UNIQUE) |
| Foreign Key | Links tables, enforces referential integrity |
| Candidate Key | Minimal unique identifier; one becomes PK |
| Composite Key | Multiple columns together form a unique key |
| Surrogate Key | System-generated ID (SERIAL, UUID) |
| Natural Key | Real-world meaningful identifier |
| 1:1 | One record ↔ one record |
| 1:N | One record ↔ many records (FK on the "many" side) |
| M:N | Many ↔ many (requires junction table) |
| NOT NULL | Column value required |
| UNIQUE | No duplicate values allowed |
| CHECK | Custom validation rule |
| DEFAULT | Fallback value if none provided |

---

**← Previous:** [01 — Database Fundamentals](./01-database-fundamentals.md)
**Next →** [03 — Data Integrity](./03-data-integrity.md)
