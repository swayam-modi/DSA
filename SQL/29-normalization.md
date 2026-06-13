# 29 — Normalization

> **Goal:** Design databases that eliminate redundancy, reduce anomalies, and ensure data integrity through normalization.

---

## 📖 What is Normalization?

**Normalization** is the process of organizing a database to reduce data **redundancy** and improve **data integrity** by applying a series of rules called **Normal Forms (NF)**.

```
Unnormalized:                          Normalized (3NF):
┌────┬──────┬──────┬────────┬────────┐  ┌─────────┐  ┌──────────┐  ┌────────┐
│ ID │ Name │ Dept │DeptMgr │ Skills │  │students │  │ courses  │  │ grades │
├────┼──────┼──────┼────────┼────────┤  ├─────────┤  ├──────────┤  ├────────┤
│  1 │ Alice│ Math │ Dr.Lee │SQL,Java│  │ id,name │  │id,name   │  │stu_id  │
│  2 │ Alice│ Phys │ Dr.Kim │SQL,C++ │  └─────────┘  └──────────┘  │crs_id  │
│  2 │ Alice│ Phys │ Dr.Kim │SQL,C++ │                              │ grade  │
└────┴──────┴──────┴────────┴────────┘                              └────────┘
Problems:                              Clean, no redundancy!
  - Duplicate data
  - Update anomalies
  - Delete anomalies
  - Insert anomalies
```

---

## ❌ Anomalies (Why We Normalize)

```
Table: StudentCourse (unnormalized)
┌──────┬──────────┬────────┬─────────────┐
│ SID  │ SName    │ Course │ Instructor  │
├──────┼──────────┼────────┼─────────────┤
│  1   │ Alice    │ Math   │ Dr. Smith   │
│  1   │ Alice    │ Physics│ Dr. Jones   │
│  2   │ Bob      │ Math   │ Dr. Smith   │
│  3   │ Carol    │ Physics│ Dr. Jones   │
└──────┴──────────┴────────┴─────────────┘

1. UPDATE Anomaly: Dr. Smith changes name → update EVERY row where Course=Math
2. DELETE Anomaly: Delete Carol → lose information that Physics has Dr. Jones
3. INSERT Anomaly: Can't add a new course without a student enrolled in it
```

---

## 1NF — First Normal Form

**Rules:**
1. Each cell must contain a **single, atomic value** (no lists, arrays, or repeating groups)
2. Each column must have a **unique name**
3. The table must have a **Primary Key**
4. Each row must be **unique**

```
VIOLATION (non-atomic):
┌────┬───────┬──────────────────┐
│ ID │ Name  │ Skills           │
├────┼───────┼──────────────────┤
│  1 │ Alice │ SQL, Java, Python│  ← multiple values in one cell!
│  2 │ Bob   │ SQL, C++         │
└────┴───────┴──────────────────┘

FIX (1NF — atomic values):
┌────┬───────┬────────┐
│ ID │ Name  │ Skill  │
├────┼───────┼────────┤
│  1 │ Alice │ SQL    │
│  1 │ Alice │ Java   │
│  1 │ Alice │ Python │
│  2 │ Bob   │ SQL    │
│  2 │ Bob   │ C++    │
└────┴───────┴────────┘
PK: (ID, Skill)
```

---

## 2NF — Second Normal Form

**Rules:**
1. Must be in **1NF**
2. **No partial dependency** — every non-key column must depend on the **WHOLE** primary key (only matters for composite PKs)

```
1NF table (PK = StudentID + CourseID):
┌───────────┬──────────┬───────────────┬───────────┐
│ StudentID │ CourseID │ StudentName   │ CourseName│
├───────────┼──────────┼───────────────┼───────────┤
│     1     │   101    │ Alice         │ Math      │
│     1     │   102    │ Alice         │ Physics   │  ← Alice duplicated!
│     2     │   101    │ Bob           │ Math      │  ← Math duplicated!
└───────────┴──────────┴───────────────┴───────────┘

Partial dependencies:
  StudentName depends only on StudentID (not full composite PK)
  CourseName  depends only on CourseID  (not full composite PK)
  → These must be moved to separate tables!

2NF FIX: Split into 3 tables
┌────────────┬───────────────┐  ┌──────────┬───────────┐  ┌───────────┬──────────┬───────┐
│ StudentID  │ StudentName   │  │ CourseID │ CourseName│  │ StudentID │ CourseID │ Grade │
├────────────┼───────────────┤  ├──────────┼───────────┤  ├───────────┼──────────┼───────┤
│     1      │ Alice         │  │   101    │ Math      │  │     1     │   101    │   A   │
│     2      │ Bob           │  │   102    │ Physics   │  │     1     │   102    │   B+  │
└────────────┴───────────────┘  └──────────┴───────────┘  │     2     │   101    │   A-  │
                                                           └───────────┴──────────┴───────┘
Each non-key column depends on the WHOLE PK ✅
```

---

## 3NF — Third Normal Form

**Rules:**
1. Must be in **2NF**
2. **No transitive dependency** — non-key columns must NOT depend on other non-key columns

```
2NF table:
┌──────────┬─────────────┬────────┬────────────┐
│ EmpID    │ Name        │ DeptID │ DeptName   │
├──────────┼─────────────┼────────┼────────────┤
│    1     │ Alice       │   10   │ Finance    │  DeptName depends on
│    2     │ Bob         │   20   │ IT         │  DeptID, not on EmpID!
│    3     │ Carol       │   10   │ Finance    │  → Transitive dependency!
└──────────┴─────────────┴────────┴────────────┘

EmpID → DeptID → DeptName  (transitive: EmpID → DeptName via DeptID)

3NF FIX: Remove transitive dependency
┌──────────┬─────────────┬────────┐  ┌────────┬────────────┐
│ EmpID    │ Name        │ DeptID │  │ DeptID │ DeptName   │
├──────────┼─────────────┼────────┤  ├────────┼────────────┤
│    1     │ Alice       │   10   │  │   10   │ Finance    │
│    2     │ Bob         │   20   │  │   20   │ IT         │
│    3     │ Carol       │   10   │  └────────┴────────────┘
└──────────┴─────────────┴────────┘
DeptName now only depends on DeptID (its primary key) ✅
```

---

## BCNF — Boyce-Codd Normal Form

A **stricter version of 3NF**. For every functional dependency X → Y, X must be a **superkey**.

```
3NF but NOT BCNF violation example:
Students can have multiple subjects, taught by one teacher per subject.
Each teacher teaches only one subject.

┌──────────┬─────────┬─────────┐
│ Student  │ Subject │ Teacher │
├──────────┼─────────┼─────────┤
│ Alice    │ Math    │ Smith   │
│ Alice    │ Physics │ Jones   │
│ Bob      │ Math    │ Smith   │
└──────────┴─────────┴─────────┘

PK: (Student, Subject)
FD: Teacher → Subject  (each teacher teaches one subject)
→ Teacher is not a superkey but determines Subject → BCNF violation!

BCNF FIX:
┌──────────┬─────────┐  ┌─────────┬─────────┐
│ Student  │ Teacher │  │ Teacher │ Subject │
├──────────┼─────────┤  ├─────────┼─────────┤
│ Alice    │ Smith   │  │ Smith   │ Math    │
│ Alice    │ Jones   │  │ Jones   │ Physics │
│ Bob      │ Smith   │  └─────────┴─────────┘
└──────────┴─────────┘
```

---

## 4NF — Fourth Normal Form

**Rules:**
1. Must be in **BCNF**
2. **No multi-valued dependencies** — a table should not have two independent multi-valued facts about the same entity

```
VIOLATION: Employee can have multiple skills AND multiple hobbies (independent!)
┌──────────┬────────┬────────┐
│ Employee │ Skill  │ Hobby  │
├──────────┼────────┼────────┤
│ Alice    │ SQL    │ Chess  │  ← SQL has no relationship to Chess
│ Alice    │ SQL    │ Guitar │  ← redundant: Alice-SQL repeated
│ Alice    │ Java   │ Chess  │  ← redundant: Chess repeated
│ Alice    │ Java   │ Guitar │
└──────────┴────────┴────────┘

4NF FIX: Separate independent facts
┌──────────┬────────┐  ┌──────────┬────────┐
│ Employee │ Skill  │  │ Employee │ Hobby  │
├──────────┼────────┤  ├──────────┼────────┤
│ Alice    │ SQL    │  │ Alice    │ Chess  │
│ Alice    │ Java   │  │ Alice    │ Guitar │
└──────────┴────────┘  └──────────┴────────┘
```

---

## 5NF — Fifth Normal Form

No **join dependencies** — the table cannot be decomposed into smaller tables and re-joined to get the same result. Rarely needed in practice.

---

## ↩️ Denormalization

Sometimes intentionally **violating normalization** to improve read performance.

```
Normalized (3NF):                  Denormalized (for reporting):
  orders → order_items → products    order_report table:
  (3 JOINs for every report)         product_name stored directly in orders
                                     (no JOIN needed for name)

Use denormalization when:
✅ Read-heavy workloads (data warehouse, reporting)
✅ Known hot query paths that are too slow
✅ Materialized views (denormalized snapshot)

Don't denormalize:
❌ OLTP transactional systems (writes become complex)
❌ Frequently changing reference data (duplicates become stale)
```

---

## 🎯 Practical Design Approach

```
1. Start with 3NF for OLTP systems
   → Covers 99% of typical application needs
   → Prevents update anomalies
   → Enforces referential integrity

2. Apply BCNF when multiple candidate keys conflict

3. Apply 4NF when you notice independent multi-valued facts

4. Denormalize selectively for reporting/analytics
   → Use materialized views or read replicas
   → Don't denormalize the core transactional tables
```

---

## 🔑 Key Takeaways

| Normal Form | Prevents | Key Rule |
|------------|---------|---------|
| 1NF | Repeating groups | Atomic values, unique PK |
| 2NF | Partial dependencies | All columns depend on WHOLE PK |
| 3NF | Transitive dependencies | No non-key column depends on another non-key |
| BCNF | Non-superkey determinants | Every FD left side is a superkey |
| 4NF | Multi-valued dependencies | No independent multi-value facts in one table |
| 5NF | Join dependencies | Cannot be decomposed further without losing info |

**The practical goal:** Achieve **3NF** for transactional databases. Consider BCNF/4NF for complex schemas. Denormalize deliberately for reporting.

---

**← Previous:** [28 — Deadlocks](./28-deadlocks.md)
**Next →** [30 — Security](./30-security.md)
