# 32 — Database Design

> **Goal:** Learn the process of designing a solid, scalable relational database from requirements to implementation.

---

## 📖 Database Design Process

```
Business Requirements
        │
        ▼
Conceptual Model (ER Diagram)
  → Identify entities, attributes, relationships
        │
        ▼
Logical Model (Table structure)
  → Define tables, primary keys, foreign keys, constraints
        │
        ▼
Physical Model (Implementation)
  → Choose data types, add indexes, partitioning
        │
        ▼
Review & Normalize
  → Apply 3NF, remove redundancy
        │
        ▼
Implement in SQL (DDL)
```

---

## 🗺️ ER Diagrams (Entity-Relationship)

An **ER Diagram** visually represents entities (tables), their attributes (columns), and relationships.

### ER Notation (Chen's Notation)

```
Entity         Attribute      Relationship
┌──────────┐   ────────       ┌───────────┐
│ CUSTOMER │──( name )       │  PLACES   │
│          │──( email )      └───────────┘
│          │──( id [PK])
└──────────┘

Full example:
  ┌──────────┐         ┌────────────┐        ┌─────────┐
  │ CUSTOMER │────┬────│   ORDER    │────┬───│ PRODUCT │
  └──────────┘  1:N    └────────────┘  M:N   └─────────┘
                                  │
                            ┌─────────────┐
                            │ ORDER_ITEM  │ (junction)
                            └─────────────┘
```

### Crow's Foot Notation (Most Common in Practice)

```
one-to-one:       ──────│
one-to-many:      ──────<
many-to-many:     >──────<

customers               orders
┌──────────────┐        ┌──────────────┐
│ PK id        │───────<│ PK id        │
│    name      │        │ FK customer_id│
│    email     │        │    total     │
└──────────────┘        └──────────────┘
      1               N (one customer → many orders)
```

---

## 🎯 Entity Relationship Modeling

### Step 1: Identify Entities

```
Business: Online Bookstore
Entities:
  - Customer (who buys)
  - Book (what's sold)
  - Order (purchase event)
  - Author (who wrote the book)
  - Category (book genre)
  - Review (customer feedback)
```

### Step 2: Identify Attributes

```
Customer: id, name, email, phone, address, created_at
Book:     id, isbn, title, price, stock, description, published_date
Order:    id, customer_id, total, status, created_at
Author:   id, name, bio
Category: id, name, slug
Review:   id, customer_id, book_id, rating, comment, created_at
```

### Step 3: Identify Relationships

```
Customer → Order:   1:N (one customer, many orders)
Order → Book:       M:N (one order has many books; one book in many orders)
Book → Author:      M:N (one book has multiple authors; author writes many books)
Book → Category:    M:N (one book in multiple categories)
Customer → Review:  1:N (one customer writes many reviews)
Book → Review:      1:N (one book has many reviews)
```

### Step 4: Resolve M:N Relationships (Junction Tables)

```
order_items: (order_id, book_id, qty, unit_price)     ← Order × Book
book_authors: (book_id, author_id, role)               ← Book × Author
book_categories: (book_id, category_id)                ← Book × Category
```

---

## 🏗️ Schema Design — Bookstore Example

```sql
-- Categories
CREATE TABLE categories (
    id      SERIAL       PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    slug    VARCHAR(100) NOT NULL UNIQUE
);

-- Authors
CREATE TABLE authors (
    id      SERIAL       PRIMARY KEY,
    name    VARCHAR(150) NOT NULL,
    bio     TEXT
);

-- Books
CREATE TABLE books (
    id             SERIAL          PRIMARY KEY,
    isbn           VARCHAR(17)     UNIQUE NOT NULL,
    title          VARCHAR(300)    NOT NULL,
    description    TEXT,
    price          DECIMAL(10,2)   NOT NULL CHECK (price > 0),
    stock          INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
    published_date DATE,
    is_active      BOOLEAN         DEFAULT TRUE,
    created_at     TIMESTAMP       DEFAULT NOW()
);

-- Book-Author junction (M:N)
CREATE TABLE book_authors (
    book_id    INT NOT NULL REFERENCES books(id)   ON DELETE CASCADE,
    author_id  INT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    role       VARCHAR(50) DEFAULT 'author',   -- author, editor, translator
    PRIMARY KEY (book_id, author_id)
);

-- Book-Category junction (M:N)
CREATE TABLE book_categories (
    book_id     INT NOT NULL REFERENCES books(id)      ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

-- Customers
CREATE TABLE customers (
    id          SERIAL          PRIMARY KEY,
    email       VARCHAR(150)    UNIQUE NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    phone       VARCHAR(20),
    password_hash TEXT          NOT NULL,
    is_active   BOOLEAN         DEFAULT TRUE,
    created_at  TIMESTAMP       DEFAULT NOW()
);

-- Orders
CREATE TABLE orders (
    id          SERIAL          PRIMARY KEY,
    customer_id INT             NOT NULL REFERENCES customers(id),
    status      VARCHAR(20)     NOT NULL DEFAULT 'pending'
                                CHECK (status IN ('pending','paid','shipped','delivered','cancelled')),
    total       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    address     TEXT            NOT NULL,
    created_at  TIMESTAMP       DEFAULT NOW(),
    updated_at  TIMESTAMP       DEFAULT NOW()
);

-- Order Items (M:N junction between orders and books)
CREATE TABLE order_items (
    id          SERIAL          PRIMARY KEY,
    order_id    INT             NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id     INT             NOT NULL REFERENCES books(id),
    qty         INT             NOT NULL CHECK (qty > 0),
    unit_price  DECIMAL(10,2)   NOT NULL  -- snapshot price at time of purchase
);

-- Reviews
CREATE TABLE reviews (
    id          SERIAL          PRIMARY KEY,
    customer_id INT             NOT NULL REFERENCES customers(id),
    book_id     INT             NOT NULL REFERENCES books(id),
    rating      SMALLINT        NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP       DEFAULT NOW(),
    UNIQUE (customer_id, book_id)   -- one review per customer per book
);

-- Indexes
CREATE INDEX idx_books_isbn           ON books(isbn);
CREATE INDEX idx_orders_customer      ON orders(customer_id);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_order_items_order    ON order_items(order_id);
CREATE INDEX idx_order_items_book     ON order_items(book_id);
CREATE INDEX idx_reviews_book         ON reviews(book_id);
CREATE INDEX idx_book_categories_cat  ON book_categories(category_id);
```

---

## 📛 Naming Conventions

Consistent naming makes the schema self-documenting.

```
Recommended conventions:
──────────────────────────────────────────────────────────────
Tables:         Plural, lowercase, underscore  → orders, customers, order_items
Columns:        Singular, lowercase, underscore → customer_id, created_at
Primary Keys:   'id' (simple)                  → id
Foreign Keys:   referenced_table_singular + _id → customer_id, product_id
Junction Tables: table1_table2 (alphabetical)  → book_authors, order_items
Indexes:        idx_tablename_column(s)         → idx_orders_customer_id
Constraints:    pk_, fk_, uq_, chk_ prefix     → fk_orders_customer, uq_email
Views:          v_ prefix or descriptive        → active_customers, v_order_summary
```

---

## 🎨 Data Modeling Best Practices

```
1. Design for the domain, not the UI
   → Tables should reflect business entities, not screen layouts

2. Use surrogate keys (SERIAL/UUID) as PKs
   → Natural keys change; surrogate keys don't

3. Add created_at and updated_at to every table
   → Invaluable for debugging, auditing, and time-series queries

4. Store prices at time of purchase, not references
   → Products change price; historical orders must keep original price
   (see unit_price in order_items above)

5. Don't store computed values
   → Store qty and unit_price; compute subtotal = qty * unit_price

6. Use CHECK constraints proactively
   → Let the DB enforce business rules (rating 1-5, price > 0)

7. Think about soft deletes early
   → is_active = FALSE instead of DELETE (preserve history)

8. Design for growth
   → Use BIGSERIAL for high-volume tables
   → Consider partitioning for time-series tables
```

---

## 🔑 Key Takeaways

| Design Phase | Deliverable |
|-------------|------------|
| Conceptual | ER Diagram (entities + relationships) |
| Logical | Table definitions, keys, constraints |
| Physical | Data types, indexes, partitioning |
| Implementation | DDL scripts |

**ER Diagram → Relational schema mapping:**
- Entity → Table
- Attribute → Column
- 1:N → FK on the "many" side
- M:N → Junction table with two FKs
- 1:1 → FK on either side (put on the less-frequent table)

---

**← Previous:** [31 — Backup & Recovery](./31-backup-recovery.md)
**Next →** [33 — Advanced Topics](./33-advanced-topics.md)
