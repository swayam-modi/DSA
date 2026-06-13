# 33 — Advanced Topics

> **Goal:** Master partitioning, sharding, replication, migrations, and full-text search for production-scale databases.

---

## 🗂️ Table Partitioning

Partitioning divides a large table into smaller, more manageable pieces that are stored and queried independently but appear as one table.

### Why Partition?

```
Table with 500M rows:
  Full scan = read 500M rows
  With partitioning by year:
    2023 partition = 50M rows
    2024 partition = 50M rows
    Query: WHERE year = 2024 → reads ONLY 50M rows!
    
Benefits:
  ✅ Faster queries (partition pruning)
  ✅ Faster maintenance (DROP old partition vs DELETE millions of rows)
  ✅ Parallel query on separate partitions
  ✅ Cold data can be moved to cheaper storage
```

### Horizontal Partitioning (Range Partitioning)

Split rows by value range (most common for time-series data).

```sql
-- PostgreSQL: Range partitioning by date
CREATE TABLE orders (
    id          BIGSERIAL,
    customer_id INT,
    total       DECIMAL(12,2),
    created_at  TIMESTAMP NOT NULL
) PARTITION BY RANGE (created_at);

-- Create partitions per year
CREATE TABLE orders_2023 PARTITION OF orders
    FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');

CREATE TABLE orders_2024 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE orders_2025 PARTITION OF orders
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

-- Query: automatically routed to correct partition
SELECT * FROM orders WHERE created_at >= '2024-06-01';
-- Only reads orders_2024 partition!

-- Drop old data efficiently
DROP TABLE orders_2023;   -- instant! vs DELETE of 50M rows (hours)

-- Add new partition for 2026
CREATE TABLE orders_2026 PARTITION OF orders
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

### List Partitioning

Split rows by specific values.

```sql
CREATE TABLE orders (
    id     BIGSERIAL,
    region VARCHAR(20) NOT NULL,
    total  DECIMAL
) PARTITION BY LIST (region);

CREATE TABLE orders_us    PARTITION OF orders FOR VALUES IN ('US');
CREATE TABLE orders_eu    PARTITION OF orders FOR VALUES IN ('EU', 'UK');
CREATE TABLE orders_asia  PARTITION OF orders FOR VALUES IN ('IN', 'CN', 'JP');
CREATE TABLE orders_other PARTITION OF orders DEFAULT;
```

### Hash Partitioning

Distribute rows evenly using a hash function.

```sql
CREATE TABLE events (
    id         BIGSERIAL,
    user_id    INT NOT NULL,
    event_type VARCHAR(50)
) PARTITION BY HASH (user_id);

CREATE TABLE events_p0 PARTITION OF events FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE events_p1 PARTITION OF events FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE events_p2 PARTITION OF events FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE events_p3 PARTITION OF events FOR VALUES WITH (MODULUS 4, REMAINDER 3);
-- user_id % 4 = 0 goes to events_p0, etc.
```

### Vertical Partitioning

Split columns into separate tables (not SQL-native, done at design time).

```
Wide table (20 columns):
  users: id, name, email, password_hash, ...10 frequently used cols..., ...10 rarely used cols

Vertical partition:
  users:        id, name, email, password_hash   (hot data — always loaded)
  user_details: user_id, bio, preferences, ...    (cold data — loaded on demand)
  
Result: hot queries load less data per row → more rows fit in memory → faster
```

---

## 🌐 Sharding

**Sharding** = horizontal partitioning across **multiple database servers**.

```
Without Sharding:                    With Sharding:
One server, 1B rows                  4 servers, 250M rows each

┌─────────────┐                     ┌──────────┐ ┌──────────┐
│  DB Server  │                     │ Shard 0  │ │ Shard 1  │
│  1B users   │         →           │ 250M rows│ │ 250M rows│
└─────────────┘                     └──────────┘ └──────────┘
   Bottleneck!                      ┌──────────┐ ┌──────────┐
                                    │ Shard 2  │ │ Shard 3  │
                                    │ 250M rows│ │ 250M rows│
                                    └──────────┘ └──────────┘
```

**Shard Key:** The column used to determine which shard a row goes to.
- Range: user_id 0–250M → Shard 0, etc.
- Hash: `hash(user_id) % 4` → Shard 0-3

**Challenges:**
- Cross-shard queries (JOIN across shards = expensive)
- Re-sharding (adding a new shard = data migration)
- Distributed transactions (2PC complexity)

**When to shard:** Last resort after vertical scaling, read replicas, and partitioning are exhausted.

---

## 📡 Replication

**Replication** copies data from one database to one or more others in real or near-real time.

### Master-Slave (Primary-Replica)

```
Master (Primary)                     Slave (Replica)
┌─────────────────┐                 ┌─────────────────┐
│ Reads + Writes  │──── WAL/Binlog ►│   Reads only    │
│ (one instance)  │                 │ (one or more)   │
└─────────────────┘                 └─────────────────┘

Benefits:
  ✅ Scale reads (direct reports/analytics to replica)
  ✅ Failover (promote replica if master fails)
  ✅ Backups from replica (no load on master)

Limitations:
  ❌ Replication lag (replica may be slightly behind)
  ❌ Only ONE write node
```

```sql
-- PostgreSQL streaming replication
-- On primary (postgresql.conf):
wal_level = replica
max_wal_senders = 5
hot_standby = on

-- On replica:
primary_conninfo = 'host=primary_host user=replicator password=...'
-- plus a standby.signal file

-- Check replication lag
SELECT client_addr, state, sent_lsn, write_lsn, flush_lsn, replay_lsn,
       replay_lsn - sent_lsn AS lag_bytes
FROM pg_stat_replication;
```

### Multi-Master

All nodes accept writes. Conflict resolution is complex.

```
Node 1 ◄──► Node 2 ◄──► Node 3
(write)      (write)      (write)

Each node replicates to the others.
Conflict: both Node 1 and Node 2 update same row → need resolution strategy.
Examples: CockroachDB, Galera Cluster (MySQL), AWS Aurora Multi-Master
```

---

## 🚚 Database Migration

Moving or transforming a database schema with minimal downtime.

```sql
-- Safe migration pattern:
-- 1. Add new column (nullable or with default — non-breaking)
ALTER TABLE orders ADD COLUMN notes TEXT;

-- 2. Backfill (in batches to avoid lock contention)
UPDATE orders SET notes = '' WHERE notes IS NULL AND id BETWEEN 1 AND 10000;
UPDATE orders SET notes = '' WHERE notes IS NULL AND id BETWEEN 10001 AND 20000;
-- ... repeat

-- 3. Add constraint (after backfill)
ALTER TABLE orders ALTER COLUMN notes SET NOT NULL;

-- NEVER: ALTER TABLE orders ADD COLUMN notes TEXT NOT NULL;
-- (fails immediately if table has rows, or locks table for duration of backfill)
```

### Migration Tools
- **Flyway** — versioned SQL migration scripts
- **Liquibase** — XML/YAML/SQL migration files
- **Alembic** (Python/SQLAlchemy)
- **Knex.js** / **TypeORM** (Node.js)
- **Prisma Migrate**

---

## 🔍 Full-Text Search

Search through text content efficiently.

### PostgreSQL Full-Text Search

```sql
-- Create a tsvector column for searchable text
ALTER TABLE articles ADD COLUMN search_vector tsvector;

-- Populate it
UPDATE articles
SET search_vector = to_tsvector('english', title || ' ' || content);

-- Create GIN index for fast full-text search
CREATE INDEX idx_articles_search ON articles USING GIN(search_vector);

-- Search query
SELECT id, title
FROM articles
WHERE search_vector @@ to_tsquery('english', 'database & optimization')
ORDER BY ts_rank(search_vector, to_tsquery('english', 'database & optimization')) DESC;

-- Auto-update with trigger
CREATE TRIGGER articles_search_update
BEFORE INSERT OR UPDATE ON articles
FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(search_vector, 'pg_catalog.english', title, content);

-- With ranking
SELECT
    title,
    ts_rank(search_vector, query) AS rank
FROM articles,
     to_tsquery('english', 'SQL & performance') AS query
WHERE search_vector @@ query
ORDER BY rank DESC
LIMIT 10;
```

### MySQL FULLTEXT Search

```sql
-- Create FULLTEXT index
ALTER TABLE articles ADD FULLTEXT INDEX idx_article_fulltext (title, content);

-- Natural language search
SELECT * FROM articles
WHERE MATCH(title, content) AGAINST ('database performance' IN NATURAL LANGUAGE MODE);

-- Boolean mode (more control)
SELECT * FROM articles
WHERE MATCH(title, content)
AGAINST ('+database +optimization -slow' IN BOOLEAN MODE);
-- + = must include, - = must exclude
```

---

## 🔑 Key Takeaways

| Topic | Key Concept |
|-------|-----------|
| Range Partitioning | Split by value ranges (dates, IDs) |
| List Partitioning | Split by specific values (region, category) |
| Hash Partitioning | Even distribution via hash function |
| Vertical Partitioning | Split hot/cold columns into separate tables |
| Sharding | Horizontal scaling across multiple DB servers |
| Streaming Replication | Copy WAL to replicas for read scaling + failover |
| Multi-Master | All nodes write — complex conflict resolution |
| Migration | Additive changes first; never remove/rename in one step |
| Full-Text Search | GIN index + tsvector in PostgreSQL; FULLTEXT in MySQL |

---

**← Previous:** [32 — Database Design](./32-database-design.md)
**Next →** [34 — Practice Problems](./34-practice-problems.md)
