# 31 — Backup & Recovery

> **Goal:** Understand backup strategies, restore procedures, and point-in-time recovery.

---

## 📖 Why Backups Matter

```
"Backups are like seatbelts — you never need them until you suddenly need them."

Scenarios requiring backups:
  ❌ Accidental DROP TABLE by developer
  ❌ Hardware failure / disk corruption
  ❌ Ransomware attack
  ❌ Data corruption bug in application
  ❌ Datacenter fire/flood
  ❌ Mistaken UPDATE without WHERE clause
```

**Key metrics:**
- **RPO (Recovery Point Objective):** How much data loss is acceptable? (e.g., last 1 hour)
- **RTO (Recovery Time Objective):** How quickly must we recover? (e.g., within 2 hours)

---

## 📦 Backup Types

### Full Backup

A **complete copy** of the entire database at one point in time.

```bash
# PostgreSQL: pg_dump (logical backup)
pg_dump mydb > mydb_full_2025-01-15.sql

# pg_dumpall: backup all databases + roles
pg_dumpall > all_databases_2025-01-15.sql

# Binary/physical backup (pg_basebackup)
pg_basebackup -D /backup/base -Ft -z -P

# MySQL: mysqldump
mysqldump mydb > mydb_full_2025-01-15.sql
mysqldump --all-databases > all_dbs_2025-01-15.sql

# SQL Server: T-SQL
BACKUP DATABASE mydb
TO DISK = '/backup/mydb_full_2025-01-15.bak'
WITH FORMAT, COMPRESSION;
```

**Frequency:** Daily or weekly.
**Retention:** Keep multiple weeks.

---

### Differential Backup

Backs up all changes since the **last full backup**.

```bash
# SQL Server: differential backup
BACKUP DATABASE mydb
TO DISK = '/backup/mydb_diff_2025-01-17.bak'
WITH DIFFERENTIAL, COMPRESSION;

# Restore: need FULL + latest DIFFERENTIAL
```

**Frequency:** Daily (between full backups).
**Restore:** Full + latest differential.

---

### Incremental Backup

Backs up only changes since the **last backup** (any type).

```bash
# PostgreSQL: WAL archiving (continuous incremental)
# In postgresql.conf:
# archive_mode = on
# archive_command = 'cp %p /backup/wal/%f'
# wal_level = replica

# MySQL: binary log (binlog) = incremental
FLUSH LOGS;   -- force new binlog file

# SQL Server: transaction log backup
BACKUP LOG mydb
TO DISK = '/backup/mydb_log_2025-01-17-1400.bak'
WITH COMPRESSION;
```

**Frequency:** Hourly (or continuous for WAL).
**Restore:** Full + all incrementals in sequence.

---

### Backup Strategy Comparison

```
Backup         │ Size    │ Backup Time │ Restore Time │ Restore Sequence
───────────────┼─────────┼─────────────┼──────────────┼──────────────────────
Full           │ Large   │ Slow        │ Fast         │ Just full
Differential   │ Medium  │ Medium      │ Medium       │ Full + diff
Incremental    │ Small   │ Fast        │ Slow         │ Full + all incrementals
```

**Best practice: Use all three:**
```
Weekly Full → Daily Differential → Hourly Incremental
         ↑ gives max flexibility for recovery at any point
```

---

## 🔄 Restore Operations

### PostgreSQL Restore

```bash
# Restore from pg_dump (logical)
psql mydb < mydb_full_2025-01-15.sql

# Restore to a different database
createdb newdb
psql newdb < mydb_full_2025-01-15.sql

# pg_restore (for custom/directory format)
pg_restore -d mydb /backup/mydb.dump

# Physical restore from pg_basebackup
# 1. Stop PostgreSQL
# 2. Replace $PGDATA with backup
# 3. Configure recovery.conf (PostgreSQL 11 and earlier)
#    or standby.signal + postgresql.auto.conf (PostgreSQL 12+)
# 4. Start PostgreSQL → replay WAL
```

### SQL Server Restore

```sql
-- Restore full backup
RESTORE DATABASE mydb
FROM DISK = '/backup/mydb_full.bak'
WITH NORECOVERY;   -- leave in restoring state (more backups to apply)

-- Apply differential
RESTORE DATABASE mydb
FROM DISK = '/backup/mydb_diff.bak'
WITH NORECOVERY;

-- Apply all log backups in sequence
RESTORE LOG mydb FROM DISK = '/backup/mydb_log_1.bak' WITH NORECOVERY;
RESTORE LOG mydb FROM DISK = '/backup/mydb_log_2.bak' WITH NORECOVERY;

-- Final recovery (brings DB online)
RESTORE DATABASE mydb WITH RECOVERY;
```

---

## ⏱️ Point-in-Time Recovery (PITR)

Restore the database to **any specific moment** in the past.

```
Timeline:
  Full backup    Incremental WALs         Incident!
  (Jan 15)       (Jan 15 → Jan 17)        (Jan 17 14:30)
     │──────────────────────────────────────│
                                            │
     └──► Restore full backup              │
           Apply WAL up to 14:29:59        │
           → Database at 14:29:59!         │
```

### PostgreSQL PITR

```bash
# 1. Restore base backup
pg_restore /backup/base.tar

# 2. Configure recovery target time
# In postgresql.conf or recovery.conf:
restore_command = 'cp /backup/wal/%f %p'
recovery_target_time = '2025-01-17 14:29:59'
recovery_target_action = 'promote'   # or 'pause'

# 3. Create recovery signal file
touch $PGDATA/recovery.signal

# 4. Start PostgreSQL → it replays WAL up to target time
pg_ctl start

# 5. Verify data is at correct point, then promote:
SELECT pg_wal_replay_resume();
```

---

## 📋 Backup Best Practices

```
The 3-2-1 Backup Rule:
  3 copies of the data
  2 different storage media (disk + tape, local + cloud)
  1 copy offsite (different geographic location)

Validation:
  ✅ Test your backups regularly! (Untested backup = no backup)
  ✅ Practice restores in a staging environment
  ✅ Verify backup integrity after creation

Automation:
  ✅ Automate backups (cron, pg_cron, SQL Server Agent)
  ✅ Monitor backup success/failure (alert on failure)
  ✅ Log backup size and duration (detect anomalies)

Retention:
  ✅ Keep daily backups for 7-30 days
  ✅ Keep weekly backups for 3-6 months
  ✅ Keep monthly backups for 1-7 years (compliance)
```

---

## 🔑 Key Takeaways

| Backup Type | What It Contains | Restore Sequence |
|------------|-----------------|-----------------|
| Full | Complete DB snapshot | Just the full backup |
| Differential | Changes since last FULL | Full + latest differential |
| Incremental | Changes since last backup | Full + ALL incrementals |

**Recovery concepts:**
- **RPO**: How old can restored data be? → determines backup frequency
- **RTO**: How fast must recovery complete? → determines backup strategy
- **PITR**: Restore to any point in time using WAL/binary logs

**Never skip testing restores!** A backup you've never tested may not restore correctly when you actually need it.

---

**← Previous:** [30 — Security](./30-security.md)
**Next →** [32 — Database Design](./32-database-design.md)
