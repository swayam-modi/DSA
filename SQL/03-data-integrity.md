# 03 — Data Integrity

> **Goal:** Understand how databases enforce the correctness and consistency of data through integrity rules.

---

## 📖 What is Data Integrity?

**Data Integrity** means that data is **accurate, consistent, and trustworthy** throughout its lifecycle. It's the database's job to **enforce rules** so bad data can never be inserted.

```
Without Integrity:                  With Integrity (RDBMS):
──────────────────────────────────────────────────────────
order.customer_id = 9999            FK constraint checks:
(customer 9999 doesn't exist!)  →   "Does customer 9999 exist?"
                                    → No → REJECT INSERT ❌
                                    → Yes → ALLOW INSERT ✅
```

There are 4 categories of data integrity:

```
Data Integrity
├── 1. Entity Integrity       — Each row is uniquely identifiable
├── 2. Referential Integrity  — Relationships between tables are valid
├── 3. Domain Integrity       — Column values are of the right type/range
└── 4. User-Defined Integrity — Custom business rules
```

---

## 1️⃣ Entity Integrity

Every table must have a **Primary Key**, and the PK must be **unique** and **never null**.

### Primary Key Rules

| Rule | Reason |
|------|--------|
| Must be UNIQUE | Can't have two rows with the same identity |
| Must be NOT NULL | NULL means "unknown" — can't identify unknown rows |
| Only one per table | A table has one identity |
| Should be stable | Changing a PK cascades everywhere |

```
✅ Valid:                        ❌ Violations:
users table                      users table
┌────┬───────┐                   ┌──────┬───────┐
│ id │ name  │                   │ id   │ name  │
├────┼───────┤                   ├──────┼───────┤
│  1 │ Alice │                   │  1   │ Alice │
│  2 │ Bob   │                   │  1   │ Bob   │ ← DUPLICATE PK ❌
│  3 │ Carol │                   │ NULL │ Carol │ ← NULL PK ❌
└────┴───────┘                   └──────┴───────┘
```

```sql
-- PK constraint enforces both rules automatically
CREATE TABLE users (
    id   SERIAL PRIMARY KEY,  -- NOT NULL + UNIQUE enforced automatically
    name VARCHAR(100) NOT NULL
);

-- Test violations:
INSERT INTO users (id, name) VALUES (1, 'Alice');
INSERT INTO users (id, name) VALUES (1, 'Bob');    -- ❌ ERROR: duplicate key
INSERT INTO users (id, name) VALUES (NULL, 'Carol'); -- ❌ ERROR: null value
```

---

## 2️⃣ Referential Integrity

A **Foreign Key** value must either:
1. Match an existing **Primary Key** in the referenced table, OR
2. Be **NULL** (if nullable FK is allowed)

```
customers                  orders
┌────┬──────────┐          ┌────┬─────────────┐
│ id │ name     │          │ id │ customer_id │
├────┼──────────┤          ├────┼─────────────┤
│  1 │ Alice    │◄─────────│  1 │      1      │ ✅ Alice exists
│  2 │ Bob      │◄─────────│  2 │      2      │ ✅ Bob exists
└────┴──────────┘          │  3 │      9      │ ❌ customer 9 not found!
                           └────┴─────────────┘
```

```sql
CREATE TABLE orders (
    id          INT PRIMARY KEY,
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Violation test:
INSERT INTO orders (id, customer_id) VALUES (3, 9);
-- ERROR: insert or update on table "orders" violates foreign key constraint
-- Detail: Key (customer_id)=(9) is not present in table "customers".
```

---

### Cascading Actions

What happens to child rows when the parent row is **deleted** or **updated**?

```
ON DELETE / ON UPDATE → action
```

#### CASCADE
Delete/update in parent **propagates** to children.

```
DELETE customers WHERE id = 1;
  → All orders with customer_id = 1 are also deleted automatically.
```

```
BEFORE CASCADE DELETE:             AFTER CASCADE DELETE:
customers       orders             customers       orders
┌───┬───────┐  ┌───┬─────────┐    ┌───┬───────┐  ┌───┬─────────┐
│ 1 │ Alice │  │ 1 │    1    │    │ 2 │ Bob   │  │ 3 │    2    │
│ 2 │ Bob   │  │ 2 │    1    │    └───┴───────┘  └───┴─────────┘
└───┴───────┘  │ 3 │    2    │
               └───┴─────────┘
   DELETE Alice (id=1) →  orders 1 and 2 auto-deleted
```

```sql
FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
```

#### SET NULL
Child FK set to NULL when parent is deleted.

```sql
FOREIGN KEY (manager_id) REFERENCES employees(id)
    ON DELETE SET NULL
-- If manager is deleted, employees now have manager_id = NULL
```

#### SET DEFAULT
Child FK set to a default value.

```sql
FOREIGN KEY (dept_id) REFERENCES departments(id)
    ON DELETE SET DEFAULT  -- dept_id = default value
```

#### RESTRICT
**Prevents** delete/update if child rows exist. Checked immediately.

```sql
FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE RESTRICT
-- Cannot delete a customer who has orders
```

#### NO ACTION
Same as RESTRICT but checking is **deferred** to end of transaction (in PostgreSQL).

```sql
FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE NO ACTION  -- default behavior
```

#### Summary Table

| Action | What happens to child when parent changes |
|--------|------------------------------------------|
| CASCADE | Child is also deleted/updated |
| SET NULL | Child FK becomes NULL |
| SET DEFAULT | Child FK becomes its DEFAULT value |
| RESTRICT | Error immediately — block the operation |
| NO ACTION | Error at end of transaction — block |

---

## 3️⃣ Domain Integrity

Ensures that column values fall within a **valid domain** (type, range, or set of allowed values).

### Data Types (Domain Enforcement)

The database rejects values that don't match the column's type.

```sql
CREATE TABLE products (
    id       INT             PRIMARY KEY,
    name     VARCHAR(100)    NOT NULL,
    price    DECIMAL(10,2),  -- only numbers with 2 decimal places
    stock    INT,            -- only whole numbers
    launched DATE            -- only valid dates
);

-- Violation:
INSERT INTO products (id, name, price) VALUES (1, 'Widget', 'cheap');
-- ERROR: invalid input syntax for type numeric: "cheap"
```

### CHECK Constraints

```sql
CREATE TABLE employees (
    id         INT  PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    salary     DECIMAL(10,2) CHECK (salary >= 0),
    experience INT           CHECK (experience >= 0 AND experience <= 50),
    gender     CHAR(1)       CHECK (gender IN ('M', 'F', 'O')),
    hire_date  DATE          CHECK (hire_date >= '2000-01-01')
);
```

### Validation Rules (Application vs DB Level)

```
Defense in Depth:
┌─────────────────┐
│  Browser/Client │  → Basic input validation (JS)
├─────────────────┤
│  Application    │  → Business logic validation
│  (Node.js/etc)  │
├─────────────────┤
│  Database       │  → Data types, NOT NULL, CHECK, FK  ← ultimate guard
└─────────────────┘
```

> ✅ **Always validate at the database layer too.** Apps can have bugs; the DB is the last line of defense.

---

## 4️⃣ User-Defined Integrity

Business rules that go beyond standard constraint types. Implemented via:

### Stored Procedures
Enforce complex logic before insert/update.

```sql
-- Example: cannot hire someone under 18
CREATE OR REPLACE FUNCTION check_hire_age()
RETURNS TRIGGER AS $$
BEGIN
    IF (CURRENT_DATE - NEW.birth_date) < INTERVAL '18 years' THEN
        RAISE EXCEPTION 'Employee must be at least 18 years old';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_hire_age
BEFORE INSERT ON employees
FOR EACH ROW EXECUTE FUNCTION check_hire_age();
```

### Custom CHECK Constraints
```sql
-- Shipping date must be after order date
CREATE TABLE shipments (
    id          INT PRIMARY KEY,
    order_date  DATE NOT NULL,
    ship_date   DATE,
    CHECK (ship_date IS NULL OR ship_date >= order_date)
);
```

### Application-Level Rules
Some rules can only be implemented in application code:
- "A user can't review their own product"
- "Only admin users can set salary > 500,000"
- "Discount can only be applied once per customer per month"

---

## 🗺️ Full Integrity Flow

```
Application sends:  INSERT INTO orders (customer_id, amount) VALUES (5, -100);
                                                                         │
                    ┌────────────────────────────────────────────────────▼────┐
                    │                   Database Engine                        │
                    │                                                          │
                    │  1. Entity Integrity Check:                              │
                    │     → Is the PK valid (not null, not duplicate)?        │
                    │                                                          │
                    │  2. Referential Integrity Check:                         │
                    │     → Does customer_id=5 exist in customers? ✅         │
                    │                                                          │
                    │  3. Domain Integrity Check:                              │
                    │     → Is amount a valid DECIMAL? ✅                     │
                    │     → CHECK (amount > 0) → -100 fails! ❌              │
                    │                                                          │
                    │  → ROLLBACK → Return error to application               │
                    └──────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Takeaways

| Integrity Type | Mechanism | Prevents |
|----------------|-----------|---------|
| Entity | PRIMARY KEY | Duplicate or null identity |
| Referential | FOREIGN KEY + CASCADE rules | Orphan records, invalid references |
| Domain | Data types + CHECK | Wrong type, out-of-range values |
| User-Defined | Triggers, procedures, app logic | Business rule violations |

**Cascade Actions:**
- `CASCADE` — propagate changes to children
- `SET NULL` — null out the FK
- `RESTRICT/NO ACTION` — block the parent change
- `SET DEFAULT` — reset FK to its default

---

**← Previous:** [02 — Relational Concepts](./02-relational-concepts.md)
**Next →** [04 — Data Types](./04-data-types.md)
