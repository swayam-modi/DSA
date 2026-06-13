# 30 — Security

> **Goal:** Secure your database with authentication, authorization, roles, permissions, and auditing.

---

## 📖 Database Security Layers

```
Application Layer  → Input validation, parameterized queries (prevent SQL injection)
Network Layer      → SSL/TLS, firewall rules, VPN
Database Layer     → Authentication, Authorization, Encryption
OS Layer           → File permissions, OS user access, disk encryption
```

---

## 🔐 Authentication

### PostgreSQL

```sql
-- pg_hba.conf: controls who can connect
-- Format: TYPE DATABASE USER ADDRESS METHOD

# Local connections
local   all         postgres                    trust
local   all         all                         md5

# Network connections  
host    myapp       app_user    192.168.1.0/24  scram-sha-256
host    all         all         0.0.0.0/0       reject

-- Create a user with password
CREATE USER app_user WITH PASSWORD 'strong_password_here';
CREATE USER readonly_user WITH PASSWORD 'another_password' NOLOGIN;

-- Change password
ALTER USER app_user WITH PASSWORD 'new_password';

-- Limit connections
CREATE USER api_user WITH PASSWORD 'pass' CONNECTION LIMIT 10;
```

### SQL Server

```sql
-- Create login (server-level)
CREATE LOGIN app_login WITH PASSWORD = 'StrongP@ssw0rd123';

-- Create database user mapped to login
CREATE USER app_user FOR LOGIN app_login;

-- Windows Authentication
CREATE LOGIN [DOMAIN\UserName] FROM WINDOWS;
```

---

## 🎭 Roles

Roles are **groups of permissions** that can be granted to users.

### PostgreSQL

```sql
-- Create a role (group)
CREATE ROLE readonly;
CREATE ROLE readwrite;
CREATE ROLE admin;

-- Grant permissions to role
GRANT SELECT                     ON ALL TABLES IN SCHEMA public TO readonly;
GRANT SELECT, INSERT, UPDATE     ON ALL TABLES IN SCHEMA public TO readwrite;
GRANT ALL PRIVILEGES             ON ALL TABLES IN SCHEMA public TO admin;

-- Assign role to user
GRANT readonly  TO reporting_user;
GRANT readwrite TO app_user;
GRANT admin     TO dba_user;

-- Remove role from user
REVOKE readonly FROM reporting_user;

-- Create user with role
CREATE USER api_user WITH PASSWORD 'pass';
GRANT readwrite TO api_user;

-- List roles
\du    -- psql command
SELECT rolname, rolcanlogin FROM pg_roles;
```

### SQL Server

```sql
-- Built-in database roles:
-- db_owner, db_datareader, db_datawriter, db_securityadmin, etc.

-- Add user to built-in role
EXEC sp_addrolemember 'db_datareader', 'app_user';
EXEC sp_addrolemember 'db_datawriter', 'app_user';

-- Create custom role
CREATE ROLE SalesRole;
GRANT SELECT ON Sales.Orders TO SalesRole;
ALTER ROLE SalesRole ADD MEMBER app_user;
```

---

## 🔑 Permissions (GRANT / REVOKE)

### GRANT — Give Permission

```sql
-- PostgreSQL
-- Grant SELECT on specific table
GRANT SELECT ON employees TO reporting_user;

-- Grant multiple privileges
GRANT SELECT, INSERT, UPDATE ON orders TO app_user;

-- Grant all privileges on table
GRANT ALL ON products TO admin_user;

-- Grant on all tables in schema
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_role;

-- Grant on specific columns only
GRANT SELECT (id, name, dept) ON employees TO hr_user;
-- hr_user can only see id, name, dept — not salary!

-- Grant EXECUTE on function/procedure
GRANT EXECUTE ON FUNCTION calculate_tax(DECIMAL) TO app_user;

-- Grant with grant option (user can re-grant to others)
GRANT SELECT ON employees TO manager WITH GRANT OPTION;
```

```sql
-- SQL Server
GRANT SELECT ON employees TO app_user;
GRANT SELECT, INSERT ON orders TO app_user;
GRANT EXECUTE ON PROCEDURE usp_GetOrders TO app_user;
GRANT SELECT ON SCHEMA::Sales TO sales_user;   -- entire schema
```

### REVOKE — Remove Permission

```sql
-- PostgreSQL
REVOKE SELECT ON employees FROM reporting_user;
REVOKE ALL ON products FROM old_user;

-- Revoke inherited via GRANT OPTION
REVOKE GRANT OPTION FOR SELECT ON employees FROM manager;

-- SQL Server
REVOKE SELECT ON employees FROM app_user;

-- DENY: explicitly block even if granted through role (SQL Server)
DENY SELECT ON salary_details TO temp_contractor;
-- Even if temp_contractor is in a role with SELECT, DENY wins!
```

---

## 🔒 Row-Level Security (PostgreSQL)

Restrict which **rows** a user can see, enforced at the database level.

```sql
-- Enable Row Level Security on table
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- Policy: users can only see their own orders
CREATE POLICY orders_user_policy ON orders
    FOR SELECT
    USING (customer_id = current_setting('app.current_user_id')::INT);

-- Policy: admins see everything
CREATE POLICY orders_admin_policy ON orders
    FOR ALL
    TO admin_role
    USING (TRUE);

-- Set the app user context (called from application on connection)
SET app.current_user_id = '42';
SELECT * FROM orders;   -- only returns orders where customer_id = 42!
```

---

## 🔍 Auditing

Track who did what and when.

```sql
-- Audit log table
CREATE TABLE audit_log (
    id           BIGSERIAL   PRIMARY KEY,
    table_name   VARCHAR(50) NOT NULL,
    action       VARCHAR(10) NOT NULL,   -- INSERT, UPDATE, DELETE
    user_name    TEXT        NOT NULL,
    old_data     JSONB,
    new_data     JSONB,
    query_text   TEXT,
    client_ip    INET,
    created_at   TIMESTAMP   DEFAULT NOW()
);

-- Audit trigger (PostgreSQL)
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_log (table_name, action, user_name, old_data, new_data, client_ip)
    VALUES (
        TG_TABLE_NAME,
        TG_OP,
        current_user,
        CASE WHEN TG_OP = 'DELETE' OR TG_OP = 'UPDATE' THEN row_to_json(OLD) END,
        CASE WHEN TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN row_to_json(NEW) END,
        inet_client_addr()
    );
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Attach to sensitive tables
CREATE TRIGGER audit_employees
AFTER INSERT OR UPDATE OR DELETE ON employees
FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();

CREATE TRIGGER audit_accounts
AFTER INSERT OR UPDATE OR DELETE ON accounts
FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
```

### PostgreSQL: pg_audit Extension

```sql
-- Enable comprehensive auditing
CREATE EXTENSION pgaudit;

-- In postgresql.conf:
-- pgaudit.log = 'write,ddl'    -- log writes and DDL
-- pgaudit.log_relation = on    -- log each relation accessed

-- Now all writes and DDL are logged to the PostgreSQL log
```

---

## 💉 SQL Injection Prevention

SQL injection is the #1 database security threat.

```
NEVER build queries with string concatenation from user input!

❌ VULNERABLE (Node.js example):
const query = "SELECT * FROM users WHERE username = '" + username + "'";
// If username = "'; DROP TABLE users; --"
// Query becomes: SELECT * FROM users WHERE username = ''; DROP TABLE users; --'
// → CATASTROPHIC!

✅ SAFE: Use parameterized queries
const query = "SELECT * FROM users WHERE username = $1";
await db.query(query, [username]);
// username is always treated as data, never SQL code
```

```sql
-- SQL Server: parameterized query
EXEC sp_executesql
    N'SELECT * FROM users WHERE username = @username',
    N'@username VARCHAR(50)',
    @username = @inputUsername;
```

---

## 🔑 Key Takeaways

| Security Layer | Tools |
|---------------|-------|
| Authentication | pg_hba.conf, passwords, SCRAM-SHA-256 |
| Authorization | GRANT, REVOKE, DENY, Roles |
| Column-level | GRANT SELECT (col1, col2) |
| Row-level | Row Level Security (RLS) policies |
| Auditing | Triggers, pgaudit extension |
| SQL Injection | Parameterized queries — ALWAYS |
| Encryption | SSL connections, encrypted storage |

**Security Best Practices:**
1. **Principle of least privilege** — grant only what's needed
2. Use **parameterized queries** — never string concatenation
3. **Separate database users** per application (app_user, readonly_user, admin)
4. **Audit sensitive tables** (accounts, personal data, salaries)
5. Enable **SSL/TLS** for all connections
6. **Rotate passwords** regularly
7. **Never use the superuser** (postgres/sa) for application connections

---

**← Previous:** [29 — Normalization](./29-normalization.md)
**Next →** [31 — Backup & Recovery](./31-backup-recovery.md)
