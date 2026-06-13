# 🗄️ SQL & Relational Database — Complete Learning Guide

> A comprehensive, beginner-to-advanced reference covering every concept you need to master SQL and Relational Databases.

---

## 📚 Table of Contents

| #  | Topic | File |
|----|-------|------|
| 01 | Database Fundamentals | [01-database-fundamentals.md](./01-database-fundamentals.md) |
| 02 | Relational Database Concepts | [02-relational-concepts.md](./02-relational-concepts.md) |
| 03 | Data Integrity | [03-data-integrity.md](./03-data-integrity.md) |
| 04 | Data Types | [04-data-types.md](./04-data-types.md) |
| 05 | Database Objects (DDL) | [05-database-objects-ddl.md](./05-database-objects-ddl.md) |
| 06 | DML — Insert, Update, Delete | [06-dml.md](./06-dml.md) |
| 07 | DQL — SELECT & Filtering | [07-dql-select.md](./07-dql-select.md) |
| 08 | String Functions | [08-string-functions.md](./08-string-functions.md) |
| 09 | Date & Time Functions | [09-datetime-functions.md](./09-datetime-functions.md) |
| 10 | Mathematical Functions | [10-math-functions.md](./10-math-functions.md) |
| 11 | Aggregate Functions & Grouping | [11-aggregate-functions.md](./11-aggregate-functions.md) |
| 12 | Window Functions | [12-window-functions.md](./12-window-functions.md) |
| 13 | Joins | [13-joins.md](./13-joins.md) |
| 14 | Subqueries | [14-subqueries.md](./14-subqueries.md) |
| 15 | Views | [15-views.md](./15-views.md) |
| 16 | Set Operators | [16-set-operators.md](./16-set-operators.md) |
| 17 | CTEs & Derived Tables | [17-ctes-derived-tables.md](./17-ctes-derived-tables.md) |
| 18 | Indexing | [18-indexing.md](./18-indexing.md) |
| 19 | Query Optimization | [19-query-optimization.md](./19-query-optimization.md) |
| 20 | Variables & Control Flow | [20-variables-control-flow.md](./20-variables-control-flow.md) |
| 21 | JSON Processing | [21-json-processing.md](./21-json-processing.md) |
| 22 | Stored Procedures | [22-stored-procedures.md](./22-stored-procedures.md) |
| 23 | User Defined Functions | [23-user-defined-functions.md](./23-user-defined-functions.md) |
| 24 | Triggers | [24-triggers.md](./24-triggers.md) |
| 25 | Cursors | [25-cursors.md](./25-cursors.md) |
| 26 | Transactions | [26-transactions.md](./26-transactions.md) |
| 27 | Isolation Levels | [27-isolation-levels.md](./27-isolation-levels.md) |
| 28 | Deadlocks | [28-deadlocks.md](./28-deadlocks.md) |
| 29 | Normalization | [29-normalization.md](./29-normalization.md) |
| 30 | Security | [30-security.md](./30-security.md) |
| 31 | Backup & Recovery | [31-backup-recovery.md](./31-backup-recovery.md) |
| 32 | Database Design | [32-database-design.md](./32-database-design.md) |
| 33 | Advanced Topics | [33-advanced-topics.md](./33-advanced-topics.md) |
| 34 | Practice Problems | [34-practice-problems.md](./34-practice-problems.md) |
| 35 | Interview Preparation | [35-interview-prep.md](./35-interview-prep.md) |

---

## 🗺️ Learning Path

```
Beginner ──────────────────────────────────────────────────────► Advanced
   │                                                                  │
   ▼                                                                  ▼
01 Fundamentals        →    07 SELECT/DQL       →    12 Window Fns
02 Relational Concepts →    08-10 Functions     →    17 CTEs
03 Data Integrity      →    11 Aggregates       →    18 Indexing
04 Data Types          →    13 Joins            →    19 Optimization
05 DDL                 →    14 Subqueries       →    22-25 Proc/Triggers
06 DML                 →    15-16 Views/Sets    →    26-28 Transactions
                                                →    29 Normalization
                                                →    33 Advanced Topics
```

---

## 💡 How to Use This Guide

1. **Follow the numbered order** if you're a complete beginner.
2. **Jump to specific topics** if you're brushing up for interviews.
3. Each file has:
   - 📖 Concept explanation
   - 🗺️ Diagrams (ASCII/Mermaid where applicable)
   - 💻 Syntax reference
   - ✅ Working examples (PostgreSQL-compatible, with MySQL/SQL Server notes)
   - ⚠️ Common mistakes
   - 🔑 Key takeaways

---

## 🛠️ Setup — Run SQL Locally

### Option 1: PostgreSQL (Recommended)
```bash
# Install
sudo apt install postgresql postgresql-contrib

# Start
sudo service postgresql start

# Connect
psql -U postgres
```

### Option 2: MySQL
```bash
sudo apt install mysql-server
sudo service mysql start
mysql -u root -p
```

### Option 3: SQLite (Zero setup)
```bash
sudo apt install sqlite3
sqlite3 mydb.db
```

### Option 4: Online Editors (No install)
- [DB Fiddle](https://www.db-fiddle.com/)
- [SQLiteOnline](https://sqliteonline.com/)
- [Mode SQL Tutorial](https://mode.com/sql-tutorial/)

---

*Last Updated: 2026 | Covers: PostgreSQL, MySQL, SQL Server*
