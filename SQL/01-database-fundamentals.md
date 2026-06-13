# 01 — Database Fundamentals

> **Goal:** Understand what a database is, why we need one, and how database systems are structured.

---

## 📖 What is a Database?

A **database** is an organized, structured collection of data that can be easily accessed, managed, and updated. Think of it like a digital filing cabinet — but one that can store millions of records and let you search through them in milliseconds.

```
Real Life Analogy
─────────────────────────────────────────────────
  📂 File Cabinet          🗄️  Database
  ─────────────────        ─────────────────────
  Folders        →         Tables
  Papers         →         Rows (Records)
  Paper Fields   →         Columns (Attributes)
  Index Card     →         Index
  Cabinet Label  →         Schema / Database Name
```

---

## 🆚 Database vs File System

| Feature | File System | Database System |
|---------|-------------|-----------------|
| Data Redundancy | High (files duplicated) | Low (normalized) |
| Data Inconsistency | Common | Controlled |
| Data Access | Manual / sequential | Structured Query Language |
| Concurrent Access | Poor | Built-in locking |
| Security | OS-level only | Fine-grained permissions |
| Backup & Recovery | Manual | Automated |
| Data Integrity | None | Enforced via constraints |
| Relationships | No native support | Native (foreign keys) |

### Why File Systems Fail at Scale

```
Problem: Student records stored in CSV files

students.csv
───────────────────────────────────────────────
ID, Name,     Course,   Teacher,    Teacher_Email
1,  Alice,    Math,     Dr. Smith,  smith@uni.edu
2,  Bob,      Math,     Dr. Smith,  smith@uni.edu   ← Redundancy!
3,  Carol,    Physics,  Dr. Jones,  jones@uni.edu

If Dr. Smith changes email → must update EVERY row!
If update is partial → inconsistency! 🔴
```

A database avoids this via normalization (covered in Chapter 29).

---

## 🏗️ DBMS vs RDBMS

### DBMS — Database Management System
Software that manages a database. Provides basic CRUD operations.
- **Examples:** Early file-based systems, some NoSQL stores

### RDBMS — Relational Database Management System
A DBMS that organizes data into **tables** with **relationships** between them, enforcing rules (constraints, integrity).
- **Examples:** PostgreSQL, MySQL, Oracle, SQL Server, SQLite

```
DBMS
├── Hierarchical DBMS  (IBM IMS)
├── Network DBMS       (CODASYL)
├── Object DBMS        (db4o)
└── RDBMS              (PostgreSQL, MySQL, Oracle, SQL Server)
         └── Uses SQL as query language
         └── Enforces ACID properties
         └── Supports Joins, Keys, Constraints
```

---

## 🏛️ Database Architecture

### Three-Schema Architecture (ANSI/SPARC)

```
┌─────────────────────────────────────────────┐
│           External Level (View Level)        │
│   User 1 View │ User 2 View │ User 3 View   │
│   (only sees  │ (sees order │ (sees reports)│
│    customers) │  details)   │               │
└──────────────────────┬──────────────────────┘
                       │ Logical Mapping
┌──────────────────────▼──────────────────────┐
│         Conceptual Level (Logical Level)     │
│                                              │
│   Tables, Columns, Constraints, Relationships│
│   (The complete logical structure)           │
└──────────────────────┬──────────────────────┘
                       │ Physical Mapping
┌──────────────────────▼──────────────────────┐
│           Internal Level (Physical Level)    │
│                                              │
│   Actual storage: data files, indexes,       │
│   pages, blocks on disk                      │
└─────────────────────────────────────────────┘
```

**Why three levels?**
- **Data Independence** — Change physical storage without affecting applications.
- **Security** — Show only what each user needs.
- **Abstraction** — Hide complexity.

---

## 🖥️ Client-Server Architecture

Most modern databases run in a **client-server** model:

```
┌─────────────┐         Network          ┌─────────────────┐
│   CLIENT    │  ──── SQL Query ────►    │   DB SERVER     │
│             │                          │                 │
│  App / IDE  │  ◄─── Result Set ─────  │  PostgreSQL /   │
│  psql / DBI │                          │  MySQL / etc.   │
└─────────────┘                          └────────┬────────┘
                                                  │
                                         ┌────────▼────────┐
                                         │   STORAGE       │
                                         │  Data Files     │
                                         │  Log Files      │
                                         │  Index Files    │
                                         └─────────────────┘
```

### Flow of a Query

```
1. Client sends SQL:   SELECT * FROM users WHERE id = 5;
         │
         ▼
2. Parser           — Validates SQL syntax
         │
         ▼
3. Query Planner    — Decides HOW to fetch (which indexes, join order)
         │
         ▼
4. Executor         — Runs the plan against storage
         │
         ▼
5. Result returned to client
```

---

## 🧩 Database Components

```
Database System
│
├── Storage Engine
│   ├── Data Files        (.mdf / .dbf / base files)
│   ├── Log Files         (WAL / redo log / transaction log)
│   ├── Index Files       (b-tree structures)
│   └── Temp Files        (for sorts, hashes)
│
├── Query Processor
│   ├── Parser            (SQL → parse tree)
│   ├── Optimizer         (find best execution plan)
│   └── Executor          (run the plan)
│
├── Transaction Manager
│   ├── Concurrency Control (locks, MVCC)
│   └── Recovery Manager    (WAL, checkpoints)
│
├── Buffer Manager
│   └── Caches disk pages in RAM (buffer pool)
│
└── Catalog / Data Dictionary
    └── Metadata about tables, indexes, users, etc.
```

---

## 📁 Database Files

### Data Files
Store actual table data and indexes.
- **PostgreSQL:** Base files in `$PGDATA/base/<db_oid>/`
- **MySQL (InnoDB):** `.ibd` files per table
- **SQL Server:** `.mdf` (primary), `.ndf` (secondary)

### Log Files (WAL — Write-Ahead Log)
Record every change **before** it hits the data files.
Used for crash recovery.

```
Transaction commits:
  1. Write to WAL log first  ──► ensures durability (D in ACID)
  2. Mark committed
  3. Eventually flush to data file (checkpoint)

If crash occurs:
  → Replay WAL from last checkpoint to recover
```

- **PostgreSQL:** WAL files in `$PGDATA/pg_wal/`
- **MySQL:** Binary log (binlog) + InnoDB redo log
- **SQL Server:** `.ldf` (log data file)

### Control Files
Store database metadata: DB name, creation time, SCN (System Change Number), locations of other files.
- Critical — losing them = losing the DB
- Always back up!

### Temporary Files
Used for:
- Sorting large result sets
- Hash joins
- Temporary tables

---

## 🗃️ Types of Databases

### 1. Relational Databases (RDBMS)
Data stored in tables with rows and columns. SQL-based.

```
┌──────────────┐      ┌──────────────┐
│   users      │      │   orders     │
├──────────────┤      ├──────────────┤
│ id │ name   │      │ id │ user_id │
│  1 │ Alice  │◄─────│  1 │   1    │
│  2 │ Bob    │      │  2 │   1    │
└──────────────┘      └──────────────┘
```
**Examples:** PostgreSQL, MySQL, Oracle, SQL Server, SQLite

---

### 2. NoSQL Databases

#### Document Database
Data stored as JSON/BSON documents. Schema-flexible.
```json
// MongoDB document
{
  "_id": "user_1",
  "name": "Alice",
  "orders": [
    { "product": "Laptop", "price": 999 }
  ]
}
```
**Examples:** MongoDB, CouchDB, Firestore

#### Key-Value Database
Simplest model. Key → Value pairs. Extremely fast.
```
"session:abc123" → { userId: 1, expiresAt: ... }
"cache:product:5" → { name: "Laptop", price: 999 }
```
**Examples:** Redis, DynamoDB, Memcached

#### Column-Family Database
Data stored in columns grouped into "column families". Great for time-series and analytics.
```
Row Key  │ personal:name  │ personal:age  │ work:company
─────────┼────────────────┼───────────────┼──────────────
user_1   │ Alice          │ 30            │ Google
user_2   │ Bob            │                │ Amazon
```
**Examples:** Apache Cassandra, HBase

#### Graph Database
Data as nodes and edges. Perfect for relationship-heavy data.
```
(Alice) ─── FRIENDS_WITH ──► (Bob)
   │                           │
   └──── WORKS_AT ──► (Google) ◄── WORKS_AT ───┘
```
**Examples:** Neo4j, Amazon Neptune

---

### Comparison: Which to Use?

| Use Case | Best Choice |
|----------|-------------|
| Structured business data, transactions | RDBMS |
| Flexible schemas, content management | Document |
| Caching, sessions, leaderboards | Key-Value |
| Social networks, recommendations | Graph |
| IoT, analytics, time-series | Column-Family |

---

### 3. Distributed Databases
Data spread across multiple machines/data centers.

```
         ┌──────────────┐
         │  Load Balancer│
         └──────┬───────┘
        ┌───────┼───────┐
        ▼       ▼       ▼
    [Node 1] [Node 2] [Node 3]
    (US-East)(EU-West)(Asia)
```

**Examples:** CockroachDB, Google Spanner, Cassandra

### 4. Data Warehouse
Optimized for **analytical queries** (OLAP) on large historical datasets.

```
OLTP (transactional) vs OLAP (analytical)
─────────────────────────────────────────────────
OLTP:  "INSERT a new order"         → fast writes
OLAP:  "Total revenue by region Q4" → complex aggregations

Data Warehouse = Star/Snowflake schema + columnar storage
```
**Examples:** Amazon Redshift, Google BigQuery, Snowflake

---

## 🔑 Key Takeaways

- A database provides **structured storage, concurrent access, integrity, and security** over raw files.
- **RDBMS** uses tables + SQL + constraints + relationships.
- The **three-schema architecture** decouples physical storage from logical and user views.
- **Client-server** is the dominant deployment model.
- **Data files** store records; **log files** ensure crash recovery.
- Choose the database type based on your **data shape and access patterns**.

---

**Next →** [02 — Relational Database Concepts](./02-relational-concepts.md)
