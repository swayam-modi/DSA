# Designing Data-Intensive Applications (DDIA)

### By Martin Kleppmann — Comprehensive Study Notes

> _"Data is not information, information is not knowledge, knowledge is not understanding, understanding is not wisdom."_ — Clifford Stoll

---

## Table of Contents

- [Part I: Foundations of Data Systems](#part-i-foundations-of-data-systems)
  - [Chapter 1: Reliability, Scalability, and Maintainability](#chapter-1-reliability-scalability-and-maintainability)
  - [Chapter 2: Data Models and Query Languages](#chapter-2-data-models-and-query-languages)
  - [Chapter 3: Storage and Retrieval](#chapter-3-storage-and-retrieval)
  - [Chapter 4: Encoding and Evolution](#chapter-4-encoding-and-evolution)
- [Part II: Distributed Data](#part-ii-distributed-data)
  - [Chapter 5: Replication](#chapter-5-replication)
  - [Chapter 6: Partitioning](#chapter-6-partitioning)
  - [Chapter 7: Transactions](#chapter-7-transactions)
  - [Chapter 8: The Trouble with Distributed Systems](#chapter-8-the-trouble-with-distributed-systems)
  - [Chapter 9: Consistency and Consensus](#chapter-9-consistency-and-consensus)
- [Part III: Derived Data](#part-iii-derived-data)
  - [Chapter 10: Batch Processing](#chapter-10-batch-processing)
  - [Chapter 11: Stream Processing](#chapter-11-stream-processing)
  - [Chapter 12: The Future of Data Systems](#chapter-12-the-future-of-data-systems)

---

# Part I: Foundations of Data Systems

---

## Chapter 1: Reliability, Scalability, and Maintainability

### Core Concept: What is a Data-Intensive Application?

A **data-intensive** application is one where data is the primary challenge — the quantity, complexity, or speed at which it changes — rather than raw CPU power (compute-intensive).

Modern data-intensive apps are built from standard building blocks:

- **Databases** — store data so it can be found again
- **Caches** — remember expensive operation results
- **Search indexes** — filter/search data by keyword
- **Stream processors** — handle async message passing
- **Batch processors** — periodically crunch large datasets

```
Typical Data System Architecture:
┌─────────────────────────────────────────────────┐
│                   Application                    │
│                                                  │
│  ┌──────────┐   ┌──────────┐   ┌─────────────┐  │
│  │  Cache   │   │  Search  │   │  Database   │  │
│  │(Memcached│   │  Index   │   │ (Primary    │  │
│  │  Redis)  │   │(Elastic- │   │  Store)     │  │
│  └──────────┘   │  search) │   └─────────────┘  │
│       ↑         └──────────┘          ↑          │
│       │               ↑               │          │
│       └───────────────┴───────────────┘          │
│          Application code keeps these in sync    │
└─────────────────────────────────────────────────┘
```

---

### 1.1 Reliability

**Definition:** The system should continue to work **correctly** even when things go wrong (hardware faults, software bugs, human errors).

- **Fault** ≠ **Failure**: A _fault_ is one component deviating from its spec. A _failure_ is when the whole system stops providing service.
- Goal: Build **fault-tolerant** systems — prevent faults from causing failures.
- Key insight: Sometimes increase fault rate deliberately (e.g., Netflix Chaos Monkey) to exercise fault-tolerance machinery.

#### Types of Faults

**1. Hardware Faults**

- Hard disks: MTTF ~10–50 years → In a 10,000-disk cluster, expect ~1 disk failure/day
- Solution: **Redundancy** — RAID, dual power supplies, hot-swap CPUs, diesel generators
- Modern trend: Multi-machine redundancy + software fault-tolerance (rolling upgrades, no downtime)

**2. Software Errors (Systematic faults)**

- Much harder to anticipate — correlated across nodes
- Examples:
  - A bug causing all instances to crash on bad input (e.g., Linux leap-second bug in 2012)
  - Runaway processes consuming shared resources (CPU, memory, disk)
  - Cascading failures
- No quick fix: careful design, testing, process isolation, monitoring, crash-and-restart

**3. Human Errors**

- Leading cause of outages: configuration errors by operators
- Solutions:
  - Well-designed abstractions that make the right thing easy
  - Sandboxes / non-production environments
  - Thorough testing (unit, integration, manual)
  - Quick rollback mechanisms
  - Detailed monitoring and alerting (telemetry)

---

### 1.2 Scalability

**Definition:** As the system grows (data volume, traffic, complexity), there should be **reasonable ways to deal with that growth**.

Scalability is not a binary property — "Is X scalable?" is not meaningful. Better question: "How does performance change when we add load parameter Y?"

#### Describing Load: Load Parameters

Load parameters depend on the system architecture:

- Web server: requests per second
- Database: ratio of reads to writes
- Chat app: simultaneously active users
- Cache: hit rate

**Twitter Home Timeline Example (from the book):**

Two approaches to generating home timelines:

```
Approach 1: Pull on read
┌────────────────────────────────────────────────────┐
│ SELECT tweets FROM tweets                          │
│ JOIN follows ON tweets.sender = follows.followee   │
│ WHERE follows.follower = current_user              │
│ ORDER BY tweets.timestamp DESC LIMIT 1000          │
│                                                    │
│ Problem: Very expensive for users following        │
│ many people — joins large tables at query time.    │
└────────────────────────────────────────────────────┘

Approach 2: Push on write (fan-out)
┌────────────────────────────────────────────────────┐
│ When user posts a tweet:                           │
│   For each follower → insert tweet into their      │
│   home timeline cache (mailbox)                    │
│                                                    │
│ On read: just fetch from pre-computed cache        │
│                                                    │
│ Problem: Users with millions of followers          │
│ (celebrities) cause huge fan-out on write          │
└────────────────────────────────────────────────────┘

Twitter's Solution: Hybrid
- Regular users → fan-out on write (approach 2)
- Celebrities (many followers) → fetched and merged at read time (approach 1)
```

#### Describing Performance

**Latency vs Response Time:**

- **Response time**: What the client sees — includes network delay, queueing, processing
- **Latency**: Time a request is waiting to be handled

**Why percentiles matter more than averages:**

```
Response Time Distribution Example:
Req Count
  │         ████
  │        ██████
  │       ████████
  │     ████████████
  │  ██████████████████
  │██████████████████████████
  └────────────────────────────→ Response Time (ms)
  10ms  50ms  100ms 200ms 500ms 2000ms

  mean = ~150ms
  p50  = 100ms (median — half faster, half slower)
  p95  = 500ms (5% of requests take longer)
  p99  = 2000ms (1% of requests take longer)
  p999 = (1 in 1000 are the "tail latencies")
```

- **Tail latency amplification**: A request requiring 100 backend calls — the slowest one determines the total response time
- Use **percentiles** (p50, p95, p99, p999) to understand performance
- Track rolling window percentiles (e.g., last 10 minutes)

#### Approaches for Coping with Load

| Approach                           | Description                                      | When to Use                              |
| ---------------------------------- | ------------------------------------------------ | ---------------------------------------- |
| **Vertical scaling** (scale up)    | Move to more powerful machine                    | Simpler, but has limits and is expensive |
| **Horizontal scaling** (scale out) | Distribute load across more machines             | More flexibility, but complex            |
| **Elastic**                        | Automatically add/remove resources based on load | Cloud environments                       |
| **Manual**                         | Human decides when to scale                      | Simpler, avoids surprises                |

> **Key insight**: Architecture for 10x scale is usually very different from 1x scale. An architecture good for 100k req/s (each 1kB) differs from one good for 3 req/min (each 2GB).

---

### 1.3 Maintainability

**Definition:** Over time, many different people will work on the system, and they should be able to work on it **productively**.

Three design principles:

#### Operability: Making Life Easy for Operations

- Good monitoring and visibility into runtime behavior
- Support for automation and standard tooling
- Avoiding dependency on individual machines
- Good documentation and predictable behavior
- Self-healing where appropriate, manual control when needed

#### Simplicity: Managing Complexity

- **Accidental complexity**: Not inherent to the problem, but from implementation choices
- **Essential complexity**: Inherent in the problem domain
- Best tool for removing accidental complexity: **abstraction**
  - High-level languages abstract over machine code
  - SQL abstracts over on-disk data structures

#### Evolvability: Making Change Easy

- Requirements _will_ change: new facts, new use cases, business changes, platform changes, regulations
- Agile methodologies help at small scale (code files, single application)
- **Evolvability** = agility at data system level
- Closely linked to simplicity and abstraction

---

### Chapter 1 Summary

```
Core Properties of Data Systems
┌──────────────────┬──────────────────────────────────────────────────────────┐
│ Property         │ Key Points                                               │
├──────────────────┼──────────────────────────────────────────────────────────┤
│ Reliability      │ Works correctly despite hardware faults, software bugs,  │
│                  │ human errors. Fault-tolerant ≠ failure-tolerant.         │
├──────────────────┼──────────────────────────────────────────────────────────┤
│ Scalability      │ Strategies for keeping performance good under load        │
│                  │ growth. Describe load with parameters, performance with   │
│                  │ percentiles.                                              │
├──────────────────┼──────────────────────────────────────────────────────────┤
│ Maintainability  │ Operability (routine tasks easy), Simplicity (managing   │
│                  │ complexity via abstraction), Evolvability (easy change).  │
└──────────────────┴──────────────────────────────────────────────────────────┘
```

---

## Chapter 2: Data Models and Query Languages

### Core Concept: Why Data Models Matter

Data models profoundly affect:

- How software is written
- How we think about the problem

Each layer hides complexity below via a **clean data model**:

1. Application objects / data structures
2. General-purpose data model (JSON, tables, graphs)
3. Database internal representation (bytes on disk)
4. Hardware (electrical signals, magnetic fields)

---

### 2.1 Relational Model vs. Document Model

#### Relational Model (SQL)

- Proposed by Edgar Codd in 1970
- Data organized into **relations** (tables) of **tuples** (rows)
- Dominated databases for 25–30 years
- Originally for: business transaction processing, batch processing

#### The Rise of NoSQL (2010s)

Driving forces:

- Need for **greater scalability** (huge datasets, high write throughput)
- Preference for **free/open source** over commercial products
- **Specialized queries** not well-served by relational model
- Desire for **more flexible/dynamic schemas**

Result: **Polyglot persistence** — using relational alongside non-relational stores

#### Object-Relational Mismatch (Impedance Mismatch)

```
Application code uses objects/classes
         ↕  ← awkward translation (ORM frameworks help but don't solve it)
Database uses tables, rows, columns
```

**Example: LinkedIn Profile**

- Simple in JSON (document model): one self-contained document
- Complex in relational: multiple tables (users, positions, education, contact_info), requires JOINs

```json
{
  "user_id": 251,
  "first_name": "Bill",
  "last_name": "Gates",
  "positions": [
    {
      "job_title": "Co-chair",
      "organization": "Bill & Melinda Gates Foundation"
    },
    { "job_title": "Co-founder", "organization": "Microsoft" }
  ],
  "education": [
    { "school_name": "Harvard University", "start": 1973, "end": 1975 }
  ]
}
```

**Advantages of document model for résumé-like data:**

- Better **locality**: all info in one place, one query
- Explicit **tree structure** for one-to-many relationships
- No impedance mismatch

#### Many-to-One and Many-to-Many Relationships

**Normalization** (using IDs instead of text):

- Avoids duplication (only one place to update)
- Enables localization, consistent style, easier search

**Problem with document model**:

- Joins are not needed for one-to-many trees — but document databases have _weak join support_
- As applications evolve, data becomes _more interconnected_ — document model starts to struggle

#### Relational vs. Document Databases Today

| Aspect             | Document                   | Relational                           |
| ------------------ | -------------------------- | ------------------------------------ |
| Schema flexibility | Schema-on-read (implicit)  | Schema-on-write (explicit, enforced) |
| Data locality      | Good for hierarchical data | Poor — multiple tables/joins         |
| Many-to-many       | Poor (weak join support)   | Good (joins are easy)                |
| Shredding          | None needed                | Required for nested data             |
| Best for           | Self-contained documents   | Interconnected data                  |

**Schema-on-read** (document): Structure implicit, only interpreted when data is read. Like dynamic type checking.
**Schema-on-write** (relational): Structure explicit, enforced on write. Like static type checking.

> When is schema-on-read better? When items don't all have same structure, or structure is determined by external systems.

---

### 2.2 Query Languages

#### Imperative vs. Declarative

```
Imperative (how to compute):
sharks = []
for animal in animals:
    if animal.family == "Sharks":
        sharks.append(animal)

Declarative (what you want):
SELECT * FROM animals WHERE family = 'Sharks'
```

**Declarative advantages:**

- Concise, easier to work with
- Database can optimize automatically (can use indexes, parallel execution)
- Hides implementation details
- CSS (declarative) vs. JavaScript DOM manipulation (imperative) — CSS wins for styling

#### MapReduce Querying

MongoDB's MapReduce — somewhere between declarative and imperative:

```javascript
db.observations.mapReduce(
  function map() {
    // Called once per document
    var year = this.observationTimestamp.getFullYear();
    var month = this.observationTimestamp.getMonth() + 1;
    emit(year + "-" + month, this.numAnimals);
  },
  function reduce(key, values) {
    // Called for each unique key
    return Array.sum(values);
  },
  { query: { family: "Sharks" }, out: "monthlySharkReport" },
);
```

**Limitation**: Requires two carefully coordinated functions; harder than single SQL query.
MongoDB 2.2+ added aggregation pipeline (declarative alternative).

---

### 2.3 Graph-Like Data Models

Best when many-to-many relationships are very common.

**A graph consists of:**

- **Vertices** (nodes, entities)
- **Edges** (relationships, arcs)

**Use cases:**

- Social graphs (people, friendships)
- Web graphs (pages, hyperlinks)
- Road/rail networks (junctions, roads)
- Facebook's social graph (people, locations, events, comments — heterogeneous!)

```
Graph Data Example:
  [Lucy]──BORN_IN──▶[Idaho]──WITHIN──▶[USA]──WITHIN──▶[N.America]
    │
  LIVES_IN
    │
    ▼
  [London]──WITHIN──▶[England]──WITHIN──▶[Europe]
    │
  MARRIED_TO
    │
    ▼
  [Alain]──BORN_IN──▶[Beaune]──WITHIN──▶[France]──WITHIN──▶[Europe]
```

#### Property Graph Model

Each **vertex** has:

- Unique ID
- Set of outgoing/incoming edges
- Collection of properties (key-value pairs)

Each **edge** has:

- Unique ID
- Tail vertex (start), Head vertex (end)
- Label (relationship type)
- Collection of properties

**Key advantages:**

1. No schema restricting which vertices can connect
2. Efficiently traverse both directions
3. Different relationship types in one graph (clean model)

**Cypher query language** (for Neo4j):

```cypher
-- Find people who emigrated from US to Europe:
MATCH
  (person) -[:BORN_IN]->  () -[:WITHIN*0..]-> (us:Location {name:'United States'}),
  (person) -[:LIVES_IN]-> () -[:WITHIN*0..]-> (eu:Location {name:'Europe'})
RETURN person.name
```

#### Triple-Stores and SPARQL

All information as `(subject, predicate, object)`:

- `(Jim, likes, bananas)` — property
- `(Lucy, marriedTo, Alain)` — edge

```sparql
-- SPARQL equivalent of Cypher query:
PREFIX : <urn:example:>
SELECT ?personName WHERE {
  ?person :name ?personName.
  ?person :bornIn  / :within* / :name "United States".
  ?person :livesIn / :within* / :name "Europe".
}
```

#### Datalog (Foundation)

Rules defined as predicates, then queried:

```datalog
within_recursive(Location, Name) :- name(Location, Name).
within_recursive(Location, Name) :- within(Location, Via), within_recursive(Via, Name).
migrated(Name, BornIn, LivingIn) :-
  name(Person, Name), born_in(Person, BornLoc), within_recursive(BornLoc, BornIn),
  lives_in(Person, LivingLoc), within_recursive(LivingLoc, LivingIn).
?- migrated(Who, 'United States', 'Europe').
```

#### Graph Databases vs. Network Model (CODASYL)

| Aspect   | CODASYL (network)                     | Graph Database                            |
| -------- | ------------------------------------- | ----------------------------------------- |
| Schema   | Strict (which record nested in which) | None — any vertex to any vertex           |
| Access   | Only via predefined access paths      | Any vertex by ID, or via index            |
| Ordering | Children are ordered sets             | No ordering                               |
| Queries  | Imperative only                       | Declarative (Cypher/SPARQL) or imperative |

---

### Chapter 2 Summary: Data Model Comparison

```
Data Model Selection Guide:
┌──────────────────┬────────────────────────────────────────────────┐
│ Model            │ Best For                                       │
├──────────────────┼────────────────────────────────────────────────┤
│ Relational       │ Interconnected data, joins needed, analytics,   │
│                  │ structured data, ACID transactions              │
├──────────────────┼────────────────────────────────────────────────┤
│ Document         │ Self-contained documents, one-to-many           │
│                  │ relationships, schema flexibility, locality     │
├──────────────────┼────────────────────────────────────────────────┤
│ Graph            │ Many-to-many relationships everywhere,          │
│                  │ social networks, recommendation engines,        │
│                  │ knowledge graphs, anything highly connected     │
└──────────────────┴────────────────────────────────────────────────┘
```

---

## Chapter 3: Storage and Retrieval

### Core Concept: How Databases Store and Find Data

The storage engine is the heart of a database. Different engines are optimized for different workloads (OLTP vs. OLAP). Understanding storage internals helps you choose the right tool.

**The simplest possible database (two bash functions):**

```bash
db_set() { echo "$1,$2" >> database }
db_get() { grep "^$1," database | last | cut -d',' -f2 }
```

- `db_set`: O(1) — just append
- `db_get`: O(n) — must scan entire file
- Problem: writes fast, reads slow as DB grows

Solution: **indexes** — additional data structures derived from primary data that speed up reads at the cost of slower writes.

---

### 3.1 Hash Indexes

**Bitcask** (Riak's default storage engine) approach:

```
In-memory hash map:
Key → (file_offset, value_size)

key1 → {file: 1, offset: 0,   size: 12}
key2 → {file: 1, offset: 12,  size: 8 }
key3 → {file: 2, offset: 0,   size: 15}

On-disk: Append-only log segments
┌─────────────────────────────────┐
│ Segment 1:                      │
│ [key1=val1][key2=val2][key1=v2] │ (key1 appears twice — latest wins)
└─────────────────────────────────┘
┌─────────────────────────────────┐
│ Segment 2:                      │
│ [key3=val3][key2=v3][key1=v3]   │
└─────────────────────────────────┘
```

**Compaction & Merging**: Background process merges segments, keeping only latest value per key, producing a smaller merged segment.

**Practical considerations:**

- **File format**: Binary (more efficient than CSV)
- **Deletes**: Write a _tombstone_ record; compaction removes key
- **Crash recovery**: Rebuild hash map from disk, or store snapshots
- **Partially written records**: Use checksums to detect/ignore incomplete writes
- **Concurrency**: One writer thread, multiple readers safe

**Limitations of hash indexes:**

- Hash map must fit in RAM — can't support many keys
- Range queries are inefficient: cannot scan keys in order

---

### 3.2 SSTables and LSM-Trees

**Key innovation**: Require keys to be **sorted by key** in each segment file → **Sorted String Tables (SSTables)**

```
SSTable Structure (sorted by key):
┌──────────────────────────────────────────────┐
│ handbag  → value1                             │
│ handsome → value2                             │
│ handful  → value3   ← keys sorted alphabetically
│ handle   → value4                             │
│ handover → value5                             │
└──────────────────────────────────────────────┘
```

**Advantages of SSTables over hash indexes:**

1. **Merging** is efficient (like mergesort — just compare heads of each segment)
2. **No full index needed in memory** — sparse index sufficient (keep some keys, find nearby by scanning)
3. **Compression** — group key-value pairs into blocks, save I/O and space

#### LSM-Tree Write Path

```
Write Path (LSM-Tree):

New write
    │
    ▼
┌──────────────────┐
│   Memtable       │  ← In-memory balanced BST (e.g., red-black tree)
│   (sorted in RAM)│     Writes go here first
└──────────────────┘
    │  When memtable exceeds threshold (a few MB)
    ▼
┌──────────────────┐
│   SSTable on disk│  ← Flushed to disk as SSTable (Level 0)
│   (Level 0)      │     Already sorted because memtable was sorted
└──────────────────┘
    │  Background compaction
    ▼
┌──────────────────┐
│  Merged SSTables │  ← Compact and merge SSTables in background
│  (Level 1, 2...) │     Each level larger than previous
└──────────────────┘

Crash Safety: Write-ahead log (WAL) on disk for memtable recovery
```

#### LSM-Tree Read Path

```
Read Path (LSM-Tree):

Query for key
    │
    ├──▶ Check memtable (RAM) — most recent
    │
    ├──▶ Check Level 0 SSTable (most recent on disk)
    │
    ├──▶ Check Level 1 SSTable
    │
    └──▶ Check Level 2 SSTable ... (oldest)

Bloom Filters: Used to quickly tell if a key is absent in an SSTable
              (avoids reading files that definitely don't contain the key)
```

**Compaction Strategies:**

- **Size-tiered** (HBase, Cassandra): Smaller, newer SSTables merged into larger, older ones
- **Leveled** (LevelDB, RocksDB, Cassandra): Key range split into levels, each level has limited size; more disk I/O but less space amplification

---

### 3.3 B-Trees

The most widely used indexing structure — standard in most relational databases.

**Key properties:**

- Break data into fixed-size **pages** (traditionally 4KB)
- Read or write one page at a time
- Each page contains keys and references to child pages
- **Branching factor**: Typically several hundred references per page

```
B-Tree Structure:
┌───────────────────────────────────────┐
│           Root Page                    │
│  [100] │ [200] │ [300] │ [400]        │  ← keys
└──┬──────┬──────┬──────┬──────┬────────┘
   ↓      ↓      ↓      ↓      ↓
 <100  100-200 200-300 300-400  >400     ← child page ranges

Leaf Page (contains actual values):
┌────────────────────────────────────────┐
│  key=150, value=...                    │
│  key=163, value=...                    │
│  key=177, value=...                    │
│  key=199, value=...                    │
└────────────────────────────────────────┘
```

**Updating a value**: Find the leaf page, update in place, write page back.

**Inserting a new key**: Find the right page, insert. If page is too full → **split into two half-full pages**, update parent.

**Crash Safety**: B-trees use a **Write-Ahead Log (WAL)** — every modification written to log before applying to tree.

**Concurrency**: Multiple threads → protect with **latches** (lightweight locks).

#### B-Tree vs LSM-Tree Comparison

```
┌─────────────────────┬──────────────────────┬──────────────────────┐
│ Aspect              │ B-Tree               │ LSM-Tree             │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Write performance   │ Lower (update in-    │ Higher (sequential   │
│                     │ place, random I/O)   │ writes, batch)       │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Read performance    │ Generally good and   │ Can be slower (check │
│                     │ predictable          │ multiple SSTables)   │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Write amplification │ At least 2x (WAL +   │ Multiple compactions │
│                     │ tree page)           │ but sequential       │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Space usage         │ Fragmentation        │ Compact (no          │
│                     │ (splits)             │ fragmentation)       │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Each key location   │ Exactly one place    │ May exist in         │
│                     │ in index             │ multiple segments    │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Transaction         │ Good (locks on ranges│ More complex         │
│ semantics           │ of keys on tree)     │                      │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Compaction impact   │ None                 │ Can affect read/     │
│                     │                      │ write performance    │
├─────────────────────┼──────────────────────┼──────────────────────┤
│ Best for            │ Read-heavy, OLTP,    │ Write-heavy, time-   │
│                     │ predictable perf     │ series, logging      │
└─────────────────────┴──────────────────────┴──────────────────────┘
```

**Advantages of LSM-Trees:**

- Higher write throughput (sequential writes vs. random writes)
- Better compression, less fragmentation
- Can sustain higher write throughput on SSDs

**Downsides of LSM-Trees:**

- Compaction can interfere with reads/writes at high percentiles
- At high write throughput, compaction may not keep up (need monitoring)
- Multiple copies of same key in different segments

---

### 3.4 Other Indexing Structures

#### Secondary Indexes

- Created with `CREATE INDEX` in relational DBs
- Not unique (multiple rows with same key)
- Both B-trees and LSM-trees can be used

#### Clustered vs. Non-clustered Indexes

- **Heap file**: Stores actual row data separately from index
- **Clustered index**: Stores actual row data directly within index (InnoDB primary key is always clustered)
- **Covering index**: Stores some columns of a table within the index (allows index-only queries)

#### Multi-column Indexes

- **Concatenated index**: Combines fields (e.g., `(lastname, firstname)` → phone number)
- **Multi-dimensional index**: For geospatial queries (R-trees, space-filling curves like Hilbert curves)

#### Full-text Search and Fuzzy Indexes

- Lucene uses SSTable-like structure for term dictionary
- Levenshtein automaton for edit-distance search

#### In-memory Databases

- Performance advantage: Not from avoiding disk reads (OS caches), but from **avoiding encoding overhead**
- Durability: Battery-powered RAM, write log to disk, periodic snapshots, replication
- Products: VoltDB, MemSQL, Oracle TimesTen, Redis, Memcached

---

### 3.5 OLTP vs. OLAP

**OLTP** (Online Transaction Processing): Interactive user-facing transactions
**OLAP** (Online Analytic Processing): Analytics, business intelligence

```
┌──────────────────────┬──────────────────────┬──────────────────────┐
│ Property             │ OLTP                 │ OLAP                 │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Read pattern         │ Small # records,     │ Aggregate over huge  │
│                      │ fetched by key       │ number of records    │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Write pattern        │ Random-access,       │ Bulk import (ETL) or │
│                      │ low-latency          │ event stream         │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Used by              │ End users via apps   │ Internal analysts    │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Data represents      │ Latest state         │ History of events    │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Dataset size         │ GB to TB             │ TB to PB             │
└──────────────────────┴──────────────────────┴──────────────────────┘
```

#### Data Warehousing

```
ETL Flow:
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  OLTP DB 1   │   │  OLTP DB 2   │   │  OLTP DB 3   │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │     Extract       │                  │
       └────────────┬──────┘──────────────────┘
                    │  Transform
                    ▼
             ┌─────────────┐
             │  Data       │  ← Analysts query here freely
             │  Warehouse  │     without affecting OLTP
             └─────────────┘
```

**Why separate warehouses?** OLTP databases are critical; ad-hoc analytics queries can harm OLTP performance. Data warehouse can be optimized differently.

#### Star Schema for Analytics

```
Star Schema:
           dim_date
               │
               │ FK
               │
dim_store ──FK─┼─FK── dim_product
               │
          fact_sales  ← Center of star (billions of rows)
               │
               │ FK
               │
          dim_customer
```

- **Fact table**: Each row = one event (e.g., one sale). Very wide, very tall.
- **Dimension tables**: Who, what, where, when, how, why of events
- **Snowflake schema**: Dimensions broken into sub-dimensions (more normalized, harder for analysts)

---

### 3.6 Column-Oriented Storage

**Problem**: Analytical queries often access only 4-5 columns out of 100+ column fact tables.

```
Row-Oriented Storage:
Row 1: [date=2015, product=bananas, store=NYC, qty=10, revenue=5.00, ...]
Row 2: [date=2015, product=apples,  store=LA,  qty=5,  revenue=3.00, ...]
→ Must load all columns even if query only needs date and product

Column-Oriented Storage:
date:     [2015, 2015, 2016, ...]   ← Stored separately per column
product:  [bananas, apples, ...]
store:    [NYC, LA, ...]
qty:      [10, 5, ...]
revenue:  [5.00, 3.00, ...]
→ Query reads ONLY the columns it needs — much less I/O
```

**Column Compression:**

- Consecutive values in a column are often similar/repetitive → compress well
- **Bitmap encoding**: For low-cardinality columns (e.g., product in a store → ~100k products)
  ```
  product=bananas: 1 0 0 1 0 1 0 0 ...  (1 where row has bananas)
  product=apples:  0 1 1 0 0 0 1 0 ...
  ```
- **Run-length encoding**: `0, 0, 0, 1, 1, 1, 1, 0, 0` → `3×0, 4×1, 2×0`
- Columnar compression enables **vectorized processing** (SIMD operations on compressed data)

**Sort Order in Column Storage:**

- Rows cannot be sorted independently per column (must reorder all columns together)
- Sort by most commonly queried column (e.g., date)
- **Benefit**: Long runs of repeated values → even better compression
- **Multiple sort orders**: Some databases store same data in multiple sort orders (replicas sorted differently for different query patterns)

**Writing to Column-Oriented Storage:**

- Updates in place are difficult (compressed column blocks)
- LSM-tree approach: Writes go to in-memory store (row or column), periodically merged with column files on disk

**Aggregation: Data Cubes and Materialized Views:**

- **Materialized view**: Precomputed query result stored on disk (not a virtual view)
- **Data cube** (OLAP cube): Grid of aggregates grouped by dimensions

```
Data Cube (2D slice):
             Product
           A    B    C    Σ
Date  Mon: 5    3    2    10
      Tue: 7    4    1    12
      Wed: 3    8    6    17
        Σ: 15   15   9    39

Advantage: Certain queries (e.g., total sales on a date) are very fast
Disadvantage: Not flexible — can't filter by arbitrary combinations
```

---

## Chapter 4: Encoding and Evolution

### Core Concept: Schema Evolution

Applications change over time. The encoding format used for data must support **forward** and **backward compatibility**:

- **Backward compatibility**: New code reads old data ✓ (easy — know old formats)
- **Forward compatibility**: Old code reads new data ✓ (harder — must ignore unknown fields)

---

### 4.1 Formats for Encoding Data

#### Language-Specific Formats

Java `Serializable`, Python `pickle`, Ruby `Marshal`

- **Problems**: Tied to one language, security vulnerabilities, versioning issues, efficiency

#### JSON, XML, and CSV

| Format | Pros                           | Cons                                    |
| ------ | ------------------------------ | --------------------------------------- |
| JSON   | Human-readable, widespread     | No schema, ambiguous numbers, no binary |
| XML    | Human-readable, mature         | Verbose, complex                        |
| CSV    | Simple, spreadsheet-compatible | No schema, ambiguous values, no binary  |

**JSON/XML problems:**

- Numbers: JSON doesn't distinguish int from float; `2^53` issues in JavaScript
- No binary string support (must base64 encode binary data)
- No schema enforcement (optional in XML, JSON Schema exists but not widely used)

#### Binary Variants of JSON/XML

- MessagePack, BSON, BJSON, UBJSON, BISON, Smile
- More compact than textual JSON, but no significant schema advantage
- Can't save field names — still include them in binary

#### Thrift and Protocol Buffers (Protobuf)

**Key innovation: Field tags** — numeric identifiers instead of field names

```
Protobuf schema:
message Person {
    required string user_name       = 1;
    optional int64  favorite_number = 2;
    repeated string interests       = 3;
}

Encoded binary (conceptually):
[field=1, type=string, "Martin"][field=2, type=int64, 1337][field=3, type=string, "daydreaming"][field=3, type=string, "hacking"]
```

**Schema evolution:**

- Add new fields with new tag numbers → old code ignores unknown tags (forward compatible)
- Old code reads new data: missing fields get defaults (backward compatible)
- Cannot change field type or remove required fields
- **Thrift**: Has BinaryProtocol and CompactProtocol
- **Protobuf**: Single protocol (more compact)

#### Avro

```
Avro Schema (JSON representation):
{
  "type": "record",
  "name": "Person",
  "fields": [
    {"name": "userName",       "type": "string"},
    {"name": "favoriteNumber", "type": ["null", "long"], "default": null},
    {"name": "interests",      "type": {"type": "array", "items": "string"}}
  ]
}

No tag numbers! Field order in encoded data must match schema.
```

**Avro schema evolution (Reader/Writer schema):**

```
Writer's Schema (when data was written):
  {name: "favoriteNumber", type: "int"}

Reader's Schema (current code):
  {name: "favoriteNumber", type: "long"}
         ↕
Avro resolves differences — type coercion, field reordering, defaults for added fields
```

**Key insight**: Avro has no field tags — relies on schema being present at decode time. Very friendly to **dynamically generated schemas** (e.g., from database tables).

**How does reader know writer's schema?**

- **Large file with many records**: Include schema once at beginning
- **Database records**: Include version number per record, store schemas in a registry
- **Network connection**: Negotiate schema at connection setup

#### Comparison of Binary Encodings

```
┌──────────────────┬────────────────────────────────────────────────┐
│ Format           │ Key Characteristics                            │
├──────────────────┼────────────────────────────────────────────────┤
│ Thrift           │ Field tags in schema, BinaryProtocol or        │
│                  │ CompactProtocol, code generation required       │
├──────────────────┼────────────────────────────────────────────────┤
│ Protocol Buffers │ Field tags, single protocol, required/optional/ │
│                  │ repeated fields, code generation required       │
├──────────────────┼────────────────────────────────────────────────┤
│ Avro             │ No field tags, schema resolution at read time,  │
│                  │ great for dynamic schema generation, most       │
│                  │ compact, no code generation required            │
└──────────────────┴────────────────────────────────────────────────┘
```

**Merits of schemas over schemaless:**

- Much more compact (no field names in encoded data)
- Schema is documentation, always up to date
- Forward/backward compatibility checks before deployment
- Code generation enables compile-time type checking

---

### 4.2 Modes of Dataflow

How does data flow between processes?

#### Dataflow Through Databases

```
Backward compatibility (required): Future self must read what you wrote now
Forward compatibility (required):  Old code running alongside new must handle
                                   new fields gracefully (preserve unknown fields)

Data Outlives Code: Code gets replaced in minutes; database data persists for years
```

**Schema migration**: Most relational DBs support simple schema changes (add column with null default) without rewriting data. Old rows get null for new column on read.

#### Dataflow Through Services: REST and RPC

**Web services** = using HTTP as transport

**REST** (Representational State Transfer):

- Design philosophy, not a protocol
- Uses HTTP features, URLs for resources, standard methods
- Simple data formats, cache control, content negotiation
- Better for public APIs

**SOAP**:

- XML-based protocol, independent from HTTP
- Complex WS-\* standards ecosystem
- WSDL for API description, code generation
- Still used in enterprises, fallen out of favor elsewhere

**Remote Procedure Call (RPC) Problems:**

```
Local function call:          Network request:
✓ Predictable timing          ✗ Unpredictable — can fail for many reasons
✓ Returns or throws           ✗ Can timeout — don't know if it happened
✓ Idempotent by default       ✗ Must handle retries (non-idempotent issues)
✓ Parameters by reference     ✗ Must serialize all parameters
✓ Same language types         ✗ Must translate types across languages
```

**Modern RPC frameworks** (gRPC, Finagle, Rest.li): More explicit about async nature, use futures/promises, support service discovery.

**Compatibility for services**: Servers updated first, clients second. Need backward compat on requests, forward compat on responses.

#### Message-Passing Dataflow

Between databases (or direct RPC) and messaging systems:

**Message brokers** (RabbitMQ, ActiveMQ, Kafka, etc.):

- Sender publishes to queue/topic
- Broker routes to consumer(s)
- Decoupled: sender doesn't need receiver's address
- Buffers messages if receiver is slow/unavailable
- Retry on failure
- Fan-out: one message → many consumers

**Advantages over RPC:**

- Works even if receiver is temporarily unavailable
- Can buffer messages (no need to worry about overloading receiver)
- Sender need not know who receives
- Multiple consumers per topic
- Easier to retry failed deliveries

**Actor model** (Erlang OTP, Akka, Orleans):

- Each actor = single-threaded with private state
- Actors communicate only by messages
- Less about RPCs, more about concurrent state management
- Good for distributed actor frameworks (location transparency)

---

### Chapter 4 Summary

```
Encoding Format Decision Tree:
                    ┌─────────────────────────┐
                    │ Is human readability     │
                    │ required?                │
                    └──────────┬──────────────┘
                               │
              ┌────────────────┴─────────────────┐
              ▼                                   ▼
             YES                                  NO
     ┌────────────────┐                  ┌────────────────────┐
     │ JSON or XML    │                  │ Need dynamic schema │
     │ (prefer JSON   │                  │ generation?         │
     │  for simplicity│                  └────────┬───────────┘
     └────────────────┘                           │
                                   ┌──────────────┴────────────────┐
                                   ▼                               ▼
                                  YES                              NO
                          ┌─────────────┐              ┌───────────────────┐
                          │   Avro      │              │ Protobuf or Thrift │
                          └─────────────┘              └───────────────────┘
```

Forward and backward compatibility are critical for **evolvability** — the ability to change different parts of a system independently without breaking everything.

---

# Part II: Distributed Data

> When there is no shared memory — only messages over unreliable networks — the challenges multiply.

**Why distribute data across multiple machines?**

- **Scalability**: Single machine can't handle the read/write load
- **Fault tolerance/high availability**: If one machine fails, others take over
- **Latency**: Geographically distributed data centers serve users from nearest location

Two main approaches:

1. **Replication**: Keep copies of same data on multiple nodes
2. **Partitioning** (sharding): Split data into subsets, each on different nodes

---

## Chapter 5: Replication

### Core Concept: Why Replicate?

- **High availability**: System works when machines go down
- **Reduced latency**: Place data geographically close to users
- **Increased read throughput**: Scale read queries across replicas

Three main algorithms for replicating changes:

1. **Single-leader** (most common)
2. **Multi-leader**
3. **Leaderless**

---

### 5.1 Leaders and Followers (Single-Leader Replication)

```
Single-Leader Replication:

     +-------------+
     |   Leader    | <- All writes go here
     |  (Primary)  |
     +------+------+
            | Replication log / change stream
    +-------+-------+
    v       v       v
+--------+ +--------+ +--------+
|Follower| |Follower| |Follower|  <- Reads can be served from any replica
|   1    | |   2    | |   3    |
+--------+ +--------+ +--------+

Clients:
  Write -> Leader only
  Read  -> Any replica (follower may serve stale data)
```

Used by: PostgreSQL, MySQL, Oracle Data Guard, SQL Server AlwaysOn, MongoDB, Kafka

#### Synchronous vs. Asynchronous Replication

```
Synchronous:  Leader waits for follower 1 ACK before confirming to client
              - Guaranteed up-to-date follower
              - If follower is slow/down, leader blocks

Fully async:  Leader doesn't wait for any follower ACK
              - Fast writes
              - May lose data if leader fails before replicating

Semi-sync (common): One follower sync, rest async
```

#### Handling Node Outages

**Follower failure**: Catch up from last processed transaction in log.

**Leader failure (Failover)**:

1. Determine leader has failed (timeout)
2. Choose new leader (election or controller node)
3. Reconfigure system to route writes to new leader

**Failover problems**:

- New leader may not have received all writes from old leader -> data loss
- Two nodes may believe they are leader -> **split brain** -> data corruption
- Timeout trade-off: too long = slow failover; too short = unnecessary failovers

---

### 5.2 Replication Log Implementations

| Method                  | How                         | Pros                | Cons                                       |
| ----------------------- | --------------------------- | ------------------- | ------------------------------------------ |
| **Statement-based**     | Log SQL statements          | Simple              | Nondeterministic functions (NOW(), RAND()) |
| **WAL shipping**        | Ship low-level byte changes | Exact               | Coupled to storage engine internals        |
| **Logical (row-based)** | Log row-level changes       | Decoupled, easy CDC | More data to log                           |
| **Trigger-based**       | Custom code on change       | Flexible            | Overhead, error-prone                      |

---

### 5.3 Problems with Replication Lag

**Eventual consistency**: Replicas converge _eventually_ if writes stop.

Three consistency guarantees to aim for:

#### 1. Read-Your-Writes (Read-Your-Own-Writes)

```
Problem: User writes -> Leader
         User reads -> Follower (not yet replicated)
         User thinks their update was lost!

Solutions:
- Always read user's own profile from leader
- Track last-write timestamp; read from leader for 1 min after write
- Client includes timestamp in read; route to up-to-date replica
```

#### 2. Monotonic Reads

```
Problem: User reads replica A (up-to-date) -> sees comment
         User reads replica B (lagging)    -> comment disappears!
         Data appears to go backward in time.

Solution: Route same user's reads to same replica
          (e.g., hash(user_id) % num_replicas)
```

#### 3. Consistent Prefix Reads

```
Problem (in partitioned databases):
User A: "What time is it?" (T=1)
User B: "It is 5 o'clock"  (T=2)

Another user reads B's answer before A's question -> nonsensical!

Solution: Causally related writes go to same partition,
          or use sequence numbers to track causal dependencies.
```

---

### 5.4 Multi-Leader Replication

```
Multi-Leader (Active-Active) Replication:

Datacenter 1              Datacenter 2
+------------------+    +------------------+
|  +------------+  |    |  +------------+  |
|  |  Leader 1  |  |<-->|  |  Leader 2  |  |
|  +-----+------+  |    |  +-----+------+  |
|        |         |    |        |         |
|  +-----+------+  |    |  +-----+------+  |
|  | Followers  |  |    |  | Followers  |  |
|  +------------+  |    |  +------------+  |
+------------------+    +------------------+
```

**Use cases**: Multi-datacenter, offline clients (calendar apps), collaborative editing

**The big problem: Write Conflicts**

```
Time 0: Both: title="DDIA"
Time 1: User A (DC1): title="DDIA Notes"
Time 2: User B (DC2): title="DDIA Book"
Time 3: Replication -> title=??? CONFLICT!
```

**Conflict resolution strategies**:

- **Conflict avoidance** (best): Route all writes for same record to same leader
- **Last write wins (LWW)**: Based on timestamp -> loses data!
- **Merge values**: Concatenate or union (for sets)
- **Record conflict**: Store all versions, let application/user resolve later
- **CRDTs** (Conflict-free Replicated Data Types): Merge automatically
- **Operational transformation**: For concurrent text editing (Google Docs)

#### Multi-Leader Replication Topologies

```
Circular:     Star:        All-to-all:
A -> B -> C   A -> B <- C   A <-> B
^         |        ^           v     ^
+---------+        D         C <-> D

Circular/Star: One failed node interrupts replication path
All-to-all: Network delays can cause out-of-order delivery
```

---

### 5.5 Leaderless Replication (Dynamo-Style)

No concept of leader. Any replica accepts writes.
Used by: Amazon Dynamo, Cassandra, Riak, Voldemort

```
Leaderless Write (n=3, w=2):
Client ---write---> Replica 1 [OK]
                    Replica 2 [OK]   <- 2/3 success = w=2 -> write successful
                    Replica 3 [X]    <- unavailable

Leaderless Read (n=3, r=2):
Client ---read---> Replica 1: v5 [OK]
                   Replica 2: v5 [OK]  <- use latest version
                   Replica 3: v4 (stale) <- read repair: write v5 back
```

#### Quorum Reads and Writes

```
Quorum condition: w + r > n

n=3, w=2, r=2:  w+r=4 > 3 [OK] (at least 1 replica in read set has latest write)
n=5, w=3, r=3:  w+r=6 > 5 [OK] (tolerates 2 failed nodes)

Workload tuning:
  w=n, r=1: Fast reads, but one failed node blocks all writes
  w=1, r=n: Fast writes, but one failed node blocks all reads
  w=r=ceil((n+1)/2): Tolerates floor(n/2) failures (typical)
```

**Repairing stale replicas**:

- **Read repair**: Client detects stale value, writes fresh value back to stale replica
- **Anti-entropy**: Background process compares replicas, copies missing data

#### Sloppy Quorums and Hinted Handoff

```
Network partition cuts client off from "home" nodes.
Client writes to other reachable nodes instead ("neighbors").
After partition heals, neighbors forward writes to proper home nodes (hinted handoff).

Trade-off:
  + Higher write availability during network partition
  - w+r>n no longer guarantees reading latest value
```

#### Detecting Concurrent Writes and Version Vectors

```
Version vectors: Collection of version numbers from all replicas
                 Used to detect: is value A a successor of B, or concurrent?

Last Write Wins (LWW): Silently discards concurrent writes -> data loss!
Safer: Keep siblings (concurrent values), let application merge them
CRDTs: Data structures that merge concurrent values automatically
```

---

## Chapter 6: Partitioning

### Core Concept: Why Partition?

For very large datasets or very high query throughput, single machine is not enough.
**Partitioning** = each piece of data belongs to exactly one partition.

```
Partitioned Database:
+------------------------------------------------------+
|                   All Data (100 GB)                   |
|                                                      |
|  Partition 1    Partition 2    Partition 3           |
|  (keys A-H)     (keys I-P)    (keys Q-Z)            |
|  Node 1         Node 2        Node 3                |
+------------------------------------------------------+
```

Goal: Distribute data and query load **evenly** — avoid **hot spots**.

---

### 6.1 Partitioning Strategies

#### By Key Range

```
Partition 1: A-C
Partition 2: D-F
Partition 3: G-Z

+ Range scans efficient ("all sensors 9am-10am")
- Hot spots: all today's writes go to today's partition
  Fix: Prefix key with sensor name to distribute across partitions
```

#### By Hash of Key

```
hash("userId_1234") -> partition 4
hash("userId_5678") -> partition 1

+ Even data distribution
- Can't do range queries efficiently

Cassandra compromise: Compound primary key
  - First column hashed (determines partition)
  - Remaining columns sorted within partition
  -> Efficient range queries within a partition
```

#### Skewed Workloads and Hot Spots

Even hash partitioning fails when ONE key is extremely hot (celebrity tweets):

- Add random suffix to key -> split across N partitions
- But reads must now query all N partitions and merge results

---

### 6.2 Partitioning and Secondary Indexes

#### Document-Partitioned (Local) Indexes

```
Partition 0:
  Primary: {id:191 -> {color:red, make:Honda}}
  Secondary: color:red -> [191, 214]
             make:Honda -> [191]

Partition 1:
  Primary: {id:514 -> {color:red, make:Toyota}}
  Secondary: color:red -> [514, 631]

Query "find all red cars": must query ALL partitions (scatter/gather)
-> Expensive, tail latency amplification
```

Used by: MongoDB, Cassandra, Elasticsearch, Riak, SolrCloud, VoltDB

#### Term-Partitioned (Global) Indexes

```
Global secondary index partitioned by term:
Partition 0: terms A-R -> color:red -> [191 (P0), 514 (P1), 631 (P1)]
Partition 1: terms S-Z -> make:Toyota -> [514 (P1)]

Query "find all red cars": read from partition 0 only -> efficient!
Write "new red Honda":    must update multiple index partitions -> slow

Index writes are often asynchronous -> slight staleness possible
```

Used by: DynamoDB, Riak, Oracle data warehouse

---

### 6.3 Rebalancing Partitions

When to rebalance: More data, more queries, machine failures.

| Strategy                  | Description                                                        | Trade-offs                                                               |
| ------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------------ |
| **Fixed # partitions**    | Many more partitions than nodes; steal partitions when adding node | Choose count upfront; too small = large partitions; too large = overhead |
| **Dynamic partitioning**  | Split/merge based on size bounds                                   | Count proportional to data size; good for range partitioning             |
| **Proportional to nodes** | Fixed partitions per node; random splits when adding nodes         | Cassandra's approach                                                     |

**Automatic vs. Manual**: Auto is convenient but can overload network; manual is safer.

---

### 6.4 Request Routing (Service Discovery)

```
Approach 1: Client-side routing (knows partition->node mapping)
            Client ---direct---> correct node

Approach 2: Routing tier (partition-aware load balancer)
            Client -> Routing tier -> correct node

Approach 3: Any-node routing
            Client -> any node -> (if wrong) forward to correct node
            Used by: Cassandra, Riak

ZooKeeper/etcd as partition registry:
  Nodes register; routing tier/client subscribes to changes
  Used by: HBase, SolrCloud, Kafka, Helix
```

---

## Chapter 7: Transactions

### Core Concept: What is a Transaction?

A way to **group several reads and writes into a logical unit** — either the entire group succeeds (commit) or it fails (abort/rollback).

> "The database guarantees that if you commit a transaction, its effects are durable; and if it aborts, any writes it has made are rolled back."

**ACID**: The safety guarantees provided by transactions.

---

### 7.1 ACID Properties

```
A - Atomicity
    "All or nothing": If transaction aborts, all partial writes are rolled back.
    Not about concurrency — about crash safety.

C - Consistency
    The database remains in a "consistent state" after transaction.
    Actually an APPLICATION property, not a database property!
    The database can't know what "consistent" means for your app.

I - Isolation
    Concurrently executing transactions appear to run serially.
    Full isolation = serializability (expensive).
    In practice: weaker isolation levels.

D - Durability
    Once committed, data won't be lost even in crash/power failure.
    Single node: Write to disk + WAL.
    Distributed: Replicate to multiple nodes.
```

**Note**: BASE (Basically Available, Soft state, Eventual consistency) is a marketing term, not a technical one.

#### Single-Object vs Multi-Object Operations

```
Single-object writes: Atomic and isolated by design
  - Atomicity via WAL or COW (copy-on-write)
  - Isolation via per-object locks

Multi-object transactions needed for:
  - Inserting related records across tables (maintain foreign key consistency)
  - Updating denormalized documents (keep multiple data in sync)
  - Updating secondary indexes simultaneously with primary data
  - Email: update new_messages count AND mark email as read (both must succeed)
```

**Handling errors**: Transactions are useless if app doesn't retry on abort correctly.

- Retry may duplicate actions if no idempotency check
- Retry may overwhelm DB if error was due to overload
- Retry makes no sense for some errors (constraint violations)

---

### 7.2 Weak Isolation Levels

Full serializability is expensive. Most databases provide weaker isolation that protects against _some_ anomalies.

#### Read Committed

```
Guarantee 1: No dirty reads
  You only see data that has been committed.
  Prevents seeing partial results of an in-progress transaction.

Guarantee 2: No dirty writes
  Your writes only overwrite data that has been committed.
  Prevents: Alice and Bob both buy same car;
            Alice wins listing, Bob wins invoice.

Implementation:
  - Dirty write prevention: Row-level locks
  - Dirty read prevention: Remember old AND new value;
    serve old value to other readers until new value is committed
```

**Default in**: Oracle 11g, PostgreSQL, SQL Server 2012, MemSQL

**Does NOT prevent**: Read skew (nonrepeatable reads)

#### Snapshot Isolation and Repeatable Read

```
Problem (Read Skew / Nonrepeatable Read):
Alice checks account balances during a bank transfer.
Reads account 1 BEFORE transfer: $500
Reads account 2 AFTER  transfer: $400 (already transferred out)
Alice sees total $900 instead of $1000!

Not a lasting problem for normal queries,
but breaks: long-running backups, analytics, integrity checks.

Solution: Snapshot Isolation
Each transaction reads from a consistent snapshot at transaction start.
Even if data changes, transaction sees original snapshot.
```

**Implementation: MVCC (Multi-Version Concurrency Control)**

```
Each row has:
  created_by: transaction ID that created it
  deleted_by: transaction ID that deleted it (or empty)

Transaction visibility rules:
1. Writes by in-progress (uncommitted) transactions are invisible
2. Writes by aborted transactions are invisible
3. Writes by later-started transactions are invisible
4. All other writes are visible

→ "Readers never block writers, writers never block readers"
```

Called "repeatable read" in PostgreSQL/MySQL, "serializable" in Oracle.

#### Preventing Lost Updates

```
Problem (Read-Modify-Write Race):
T1: read counter (value=42)
T2: read counter (value=42)
T1: write counter=43
T2: write counter=43  <- T1's increment is LOST!

Solutions:
1. Atomic write operations:
   UPDATE counters SET value = value + 1 WHERE key = 'foo';

2. Explicit locking (FOR UPDATE):
   BEGIN;
   SELECT * FROM figures WHERE name='robot' FOR UPDATE;
   -- check rules, then:
   UPDATE figures SET position='c4' WHERE id=1234;
   COMMIT;

3. Automatic lost update detection:
   PostgreSQL, Oracle, SQL Server detect and abort; MySQL/InnoDB does NOT.

4. Compare-and-set:
   UPDATE wiki_pages SET content='new content'
   WHERE id=1234 AND content='old content';
   -- Only updates if content hasn't changed
```

#### Write Skew and Phantoms

```
Write Skew: Two transactions read same objects, then update different objects based on what they read.

Example (On-call doctors):
Currently: Alice on-call=true, Bob on-call=true
Requirement: At least 1 doctor must be on call

T1 (Alice): sees Bob on-call, sets Alice on-call=false
T2 (Bob):   sees Alice on-call, sets Bob on-call=false
Result: Both off-call! Constraint violated.

Neither transaction is a dirty write or lost update.
This is write skew.

Phantom: A write in one transaction changes the result of a
         search query in another transaction.
         → SELECT checks a condition; INSERT or DELETE changes
           which rows satisfy that condition.
```

**Write skew examples**: Meeting room booking, claiming username, double-spending

**Prevention**: Serializable isolation.

---

### 7.3 Serializability

The strongest isolation level. Guarantees transactions have the **same effect as if run serially**.

#### Actual Serial Execution

```
Simply: Run transactions one at a time, in serial order.
        Single thread. No concurrency.

Feasible because:
- RAM became cheap enough to keep entire dataset in memory
- OLTP transactions typically short and access few data items

Used by: VoltDB/H-Store, Redis (single-threaded), Datomic

Throughput limited to single CPU core.
Must keep transactions short (no interactive multi-statement transactions).
Stored procedures: Entire transaction logic submitted as one request.
```

```
Interactive multi-statement (slow):         Stored procedure (fast):
Client -> DB: BEGIN                         Client -> DB: EXEC stored_procedure(params)
Client -> DB: SELECT ...                    DB executes everything within single call
Client -> DB: (application logic)           DB -> Client: result
Client -> DB: UPDATE ...
Client -> DB: COMMIT
(each round-trip adds latency)
```

#### Two-Phase Locking (2PL)

```
Two phases:
  1. Lock acquisition phase: Acquire locks as needed during transaction
  2. Lock release phase: Release all locks after commit/abort (never re-acquire)

Lock types:
  Shared (read) lock: Multiple readers can hold simultaneously
  Exclusive (write) lock: Only one writer, no concurrent readers

Rules:
  - Before reading: Acquire shared lock
  - Before writing: Acquire exclusive lock (or upgrade from shared)
  - Shared lock held by T1? T2 must wait before acquiring exclusive lock
  - Exclusive lock held by T1? T2 must wait for any type of lock

Drawback: Performance — waiting for locks, deadlocks possible
Deadlock detection: DB detects cycles in wait-for graph, aborts one transaction

Predicate locks / Index-range locks:
  Used to prevent phantom reads.
  Lock not just existing rows, but also future rows matching a predicate.
  (E.g., lock all meetings in room 123 from 12:00-1:00, even those not yet inserted)
```

#### Serializable Snapshot Isolation (SSI)

```
Optimistic concurrency control:
- Allow transactions to proceed without blocking
- At commit time, check if isolation was violated
- If yes, abort and retry

Based on snapshot isolation + detection of stale MVCC reads:
- During execution: Track which transactions' reads might be stale
- At commit: If a premise of a transaction was violated (someone else wrote
  to a key that this transaction read), abort.

vs. 2PL: SSI is optimistic (doesn't block), 2PL is pessimistic (blocks)
Best when: Contention between transactions is low → SSI wins
           High contention → 2PL may be better (retries are expensive)
```

---

### Chapter 7 Summary: Transaction Isolation Levels

```
Isolation Level Comparison:
+----------------------+----------+------------+-------------+------------------+
| Isolation Level      | Dirty    | Non-        | Phantom     | Write Skew       |
|                      | Reads    | Repeatable  | Reads       |                  |
|                      |          | Reads       |             |                  |
+----------------------+----------+------------+-------------+------------------+
| Read Uncommitted     | Possible | Possible   | Possible    | Possible         |
| Read Committed       | Prevented| Possible   | Possible    | Possible         |
| Snapshot Isolation   | Prevented| Prevented  | Prevented*  | Possible         |
| Serializable         | Prevented| Prevented  | Prevented   | Prevented        |
+----------------------+----------+------------+-------------+------------------+
* Snapshot isolation prevents some but not all phantom reads

Implementation Methods:
+----------------------+------------------------------------------+
| Method               | Characteristics                          |
+----------------------+------------------------------------------+
| Actual serial exec   | Single-thread, fast for in-memory,       |
|                      | requires stored procedures, limited scale |
+----------------------+------------------------------------------+
| Two-phase locking    | Pessimistic, strong guarantees,          |
|                      | performance penalty, deadlocks possible   |
+----------------------+------------------------------------------+
| SSI                  | Optimistic, snapshot-based, good for     |
|                      | low contention, aborts on conflict        |
+----------------------+------------------------------------------+
```

---

## Chapter 8: The Trouble with Distributed Systems

### Core Concept: Embrace Partial Failure

In a single machine: software is either working or not (deterministic behavior).
In a distributed system: **partial failure** is the norm — some parts fail while others work.

> "We need to build reliable systems from unreliable components."

The fundamental problem: You cannot tell the difference between:

1. The remote node has crashed
2. The network request was lost
3. The network is temporarily slow
4. The remote node is alive but processing is paused (GC)
5. The response was lost on the way back

---

### 8.1 Unreliable Networks

Distributed systems use **asynchronous packet networks** — shared networks where packets can be:

- Lost
- Delayed (unbounded)
- Duplicated
- Out of order

```
Possible Outcomes When You Send a Request:

Request      ----X lost
Request      ---> [Remote] ---> response --X lost
Request      ---> [Remote paused] ... eventually ---> response (very late)
Request      ---> [Remote] ---> response ---> You (success)
Request      ---> [Remote crashed]
Request      ---> [Remote slow] ... response (delayed)
```

**Timeout detection**: The only way to detect failure (but can't distinguish crash from slow).

**Trade-off**:

- Too short: Declare live nodes dead (unnecessary failover, load spikes from retries)
- Too long: Long wait before declaring failure

**In practice**: Jitter + bounded latency = impossible in async networks. Systems like TCP (via Nagle's algorithm, congestion control) add more variability.

**Contrast with telephone circuits** (synchronous networks):

- Circuit-switched: Fixed bandwidth, guaranteed latency, no queueing
- Packet-switched (internet): Statistical multiplexing, variable latency, but much better utilization

---

### 8.2 Unreliable Clocks

Modern computers have two types of clocks:

#### Time-of-Day Clocks

```
System.currentTimeMillis()  (Java)
time.time()  (Python)

- Returns calendar time (seconds since epoch)
- Can jump forward or backward (NTP synchronization)
- NOT suitable for measuring elapsed time
- Subject to leap seconds
```

#### Monotonic Clocks

```
System.nanoTime()  (Java)
time.monotonic()  (Python)

- Always moves forward (monotonically)
- Measures elapsed time reliably
- Absolute value meaningless (different on each boot)
- Suitable for: measuring duration, timeouts
```

#### Clock Synchronization Problems

```
NTP (Network Time Protocol):
- Typical accuracy: within 100ms on well-managed systems
- On public internet: accuracy within 100ms-1s
- Can be worse: firewalls, misconfiguration, leap seconds

Google's TrueTime API (Spanner):
- Returns a confidence interval [earliest, latest]
- Hardware atomic clocks in datacenters
- GPS receivers
- Uncertainty: ~7ms max

Without TrueTime: Don't use timestamps for ordering events!
```

**Why timestamps for ordering are dangerous**:

```
Problem with "Last Write Wins" using timestamps:
T=100: Node A writes key=1 (timestamp 100ms)
T=100: Node B writes key=2 (timestamp 100ms, clock slightly off)
Result: Which write wins? Clock skew means you can't tell.

Even with NTP: Clocks on different nodes can differ by 100ms+
→ LWW with timestamps silently discards writes
```

---

### 8.3 Process Pauses

A thread can be paused at ANY point for:

- **GC (stop-the-world)**: JVM GC can pause for minutes
- **VM live migration**: VM suspended while copying to another host
- **OS context switching**: Another process gets CPU
- **Disk I/O**: Thread waits for slow disk or network filesystem
- **Memory swapping**: Page fault causes disk access
- **SIGSTOP signal**: Process paused by shell/operator

```
The danger:
Code checks "do I still hold the lock?" -> YES
GC pause for 15 seconds (or VM migration, etc.)
Another node declares this node dead, acquires lock
Code continues running, thinks it still holds lock -> CHAOS!

Solution: Fencing tokens
  Lock service issues monotonically increasing token with each lock grant.
  Every write request to storage must include token.
  Storage rejects writes with token older than what it has already processed.

  T=1: Node A acquires lock, gets token=33
  T=2: Node A GC pause... A declared dead
  T=3: Node B acquires lock, gets token=34
  T=4: Node B writes with token=34 -> Storage: accepted (34 > last seen)
  T=5: Node A resumes, writes with token=33 -> Storage: REJECTED (33 < 34)
```

---

### 8.4 Knowledge, Truth, and Lies

**A node cannot trust its own judgment**:

- May be "dead" in the view of others (due to network partition)
- May have been replaced by another leader
- May have stale data

**The majority rules**: Quorum-based decisions.

```
Split Brain scenario:
  Both nodes think they are the leader.
  Both accept writes -> data diverges -> corruption.

Prevention:
  Only one node can hold lock at a time (via ZooKeeper, etc.)
  Use fencing tokens for all writes.
  Accept writes only from the current recognized leader.
```

#### Byzantine Faults

**Normal faults**: Nodes crash, are slow, network drops messages — nodes are _honest_ (not lying).

**Byzantine faults**: Nodes may _lie_ — send arbitrary/corrupted responses, claim to receive messages they didn't, etc.

```
Byzantine Generals Problem:
n generals, some are traitors. They communicate only by messenger.
Need to agree on a battle plan even though traitors send conflicting messages.

Byzantine fault tolerance requires > 2/3 of nodes to be honest.
Cost: Very complex protocols, 3x+ overhead.
Not practical for most datacenter systems (all nodes controlled by same org).
```

**Byzantine fault tolerance is needed for**:

- Aerospace systems (radiation flipping bits)
- Blockchain/peer-to-peer networks (untrusting parties)

**For most systems**: Trust nodes, handle crash/network faults, use checksums for corruption detection.

---

### 8.5 System Models

Abstract away from messy reality to reason about distributed algorithms.

**Timing models**:

```
Synchronous: Bounded network delay, bounded process pauses, bounded clock drift.
             Unrealistic for most systems.

Partially synchronous: System behaves asynchronously for some bounded period,
                       then returns to synchronous. More realistic.

Asynchronous: No timing assumptions at all. Most conservative.
              Many algorithms can't work here (e.g., can't use timeouts).
```

**Node failure models**:

```
Crash-stop: Node fails by crashing, never comes back.
Crash-recovery: Node may crash and later recover. Must persist state to disk.
Byzantine: Node may do anything, including lying.
```

**Most practical algorithms assume**: Partially synchronous + crash-recovery

**Liveness vs. Safety properties**:

```
Safety: "Nothing bad happens" (e.g., two nodes don't both think they are leader)
        Must ALWAYS hold.

Liveness: "Something good eventually happens" (e.g., request eventually answered)
          Allowed to fail temporarily.

Algorithm correctness: Hold safety properties always; hold liveness under good conditions.
```

---

### Chapter 8 Summary

```
Core Challenges in Distributed Systems:
+---------------------------+-----------------------------------------------+
| Challenge                 | Impact                                        |
+---------------------------+-----------------------------------------------+
| Unreliable networks       | Can't distinguish crash from slow node;       |
|                           | must use timeouts (imperfect)                 |
+---------------------------+-----------------------------------------------+
| Unreliable clocks         | Can't use timestamps for event ordering;      |
|                           | clock drift makes LWW dangerous               |
+---------------------------+-----------------------------------------------+
| Process pauses            | Node may pause mid-operation; fencing         |
|                           | tokens needed for lease-based protocols       |
+---------------------------+-----------------------------------------------+
| Partial failures          | Some parts work, others don't; quorum         |
|                           | decisions protect against single-node lies    |
+---------------------------+-----------------------------------------------+
| Byzantine faults          | Very complex; only needed for untrusting      |
|                           | environments (blockchain, aerospace)          |
+---------------------------+-----------------------------------------------+
```

---

## Chapter 9: Consistency and Consensus

### Core Concept: What guarantees can distributed systems provide?

Previous chapters: Problems with distributed systems.
This chapter: How to handle those problems — consistency guarantees and consensus algorithms.

---

### 9.1 Consistency Guarantees

**Eventual consistency**: "If you wait long enough, all replicas converge."

- Very weak guarantee
- No guarantee about _when_ convergence happens
- Better stated as "convergent consistency"

**Stronger guarantees** require more coordination → more latency, less availability.

---

### 9.2 Linearizability

The strongest consistency model for single-object operations.

```
Linearizability definition:
"Make a distributed system appear as if there is only one copy of the data,
 and all operations are atomic."

Key property: Once a read returns a new value,
              all subsequent reads (by any client) must also return that new value.

Non-linearizable read scenario:
  T=0: x=0
  T=1: Writer writes x=1 (sent to leader)
  T=2: Reader A reads x -> gets 1 (leader)
  T=3: Reader B reads x -> gets 0 (stale replica)  ← NOT linearizable!
       After Reader A saw x=1, Reader B should also see x=1.
```

**Linearizability vs. Serializability**:

```
Serializability: Isolation property for transactions (multiple objects)
                 Transactions appear to execute in some serial order.
                 But: Transactions may be reordered (T2 before T1 even if T1 started first).

Linearizability: Recency guarantee for reads and writes of single register.
                 Concurrent operations respect real-time ordering.

Strict serializability (strongest): Both together.
                                    PostgreSQL's "serializable" + real-time order.
```

#### Use Cases for Linearizability

```
1. Locking and Leader Election
   Only one node can be leader (or hold a lock).
   Requires: Reads from lock service reflect latest write.
   Provided by: ZooKeeper, etcd (use linearizable operations).

2. Uniqueness Constraints
   Username must be unique.
   Requires: All nodes see same "username already taken" state.
   Linearizable compare-and-set: "Set username if not already taken"

3. Cross-channel Timing Dependencies
   System writes file to storage, sends message to resize service.
   Resize service must see file that was just written.
   → Linearizable storage prevents seeing old version of file.
```

#### Implementing Linearizable Systems

```
Single-leader replication:
  Reads from leader OR sync followers: LINEARIZABLE
  Reads from async followers: NOT linearizable

Multi-leader replication: NOT linearizable
  (concurrent writes on different leaders)

Leaderless replication: NOT always linearizable
  Even with w+r>n, sloppy quorums can return stale data.

Consensus algorithms (Raft, Paxos): LINEARIZABLE
  Designed to prevent split brain; safe to serve reads from leader.
```

#### The Cost of Linearizability: CAP Theorem

```
CAP Theorem:
In the presence of a network partition, choose:
  - Consistency (linearizability): Refuse requests to avoid returning stale data
  - Availability: Continue serving requests (possibly stale)
  - But NOT both at the same time

More precisely: "CAP is about the trade-off between consistency and availability
                 during a network partition."

PACELC (more complete):
  During Partition: trade Availability vs. Consistency
  Else (no partition): trade Latency vs. Consistency
```

**Multi-datacenter operations**:

- Linearizability requires synchronous cross-datacenter round-trips → high latency
- Non-linearizable: Each datacenter continues operating independently

---

### 9.3 Ordering Guarantees

#### Ordering and Causality

**Causality**: One thing caused another. Causally related events have a **happens-before** relationship.

```
Causal consistency: A weaker model than linearizability.
  - Causally related events must be seen in order.
  - Concurrent events (no causal relationship) can be in any order.

This is the strongest consistency model compatible with availability.
```

#### Sequence Numbers and Lamport Timestamps

```
Lamport Timestamp: (counter, node_id)
  - Each node keeps a counter
  - counter = max(local_counter, received_counter) + 1 on each event
  - Consistent with causality: if A happened before B, timestamp(A) < timestamp(B)

Example:
Node 1: (1,1), (2,1), (5,1)...
Node 2: (1,2), (4,2), (6,2)...

Limitation: Can't determine from timestamps alone if two events are concurrent
            or if one causally depends on the other.
            Also: Can't determine if you have received all events with lower timestamps.
```

#### Total Order Broadcast

```
Total Order Broadcast (Atomic Broadcast):
  - All nodes must deliver the SAME messages in the SAME ORDER
  - No message is delivered more than once
  - If message is delivered to one node, it is delivered to all nodes

Properties:
  Reliable delivery: No message is lost
  Total ordered delivery: Messages delivered in same order to all nodes

Uses:
  - Distributed database replication (state machine replication)
  - Serializing transactions
  - Locks, leader election, configuration management (ZooKeeper, etcd)

Relationship to linearizability:
  - Total order broadcast <-> Linearizable storage (can implement each from the other)
  - Total order broadcast is like an append-only log
```

---

### 9.4 Distributed Transactions and Consensus

**Consensus** = get several nodes to agree on a value.

**Why consensus is hard**: FLP impossibility result — in an asynchronous system with even one faulty node, no deterministic consensus algorithm can terminate. (But in practice, partial synchrony + crash-recovery → consensus is achievable.)

#### Two-Phase Commit (2PC)

Used to atomically commit a transaction across multiple nodes.

```
2PC Protocol:
  Phase 1 (Prepare):
    Coordinator sends "prepare?" to all participants.
    Each participant checks if it CAN commit.
    Participant replies "yes" (and promises to commit if asked)
    or "no" (can abort).

  Phase 2 (Commit or Abort):
    If ALL participants voted "yes": Coordinator sends "commit" to all.
    If ANY voted "no": Coordinator sends "abort" to all.
    Participants act accordingly (commit or abort), send "done".

Key properties:
  - Participant votes "yes": must wait forever for coordinator's decision
    (cannot abort unilaterally after voting yes)
  - "Point of no return": Once coordinator decides, decision is final
```

```
2PC Flow:
Client -> Coordinator: BEGIN TRANSACTION
Client -> Coordinator: UPDATE table1...
Coordinator -> DB1: prepare?
Coordinator -> DB2: prepare?
DB1 -> Coordinator: yes (persists to WAL, releases locks after decision)
DB2 -> Coordinator: yes
Coordinator -> [writes commit decision to WAL]
Coordinator -> DB1: commit
Coordinator -> DB2: commit
DB1 -> Coordinator: done
DB2 -> Coordinator: done
Coordinator -> Client: transaction committed
```

**Problem with 2PC**: Coordinator failure

```
Coordinator failure after participants voted "yes":
  Participants are stuck waiting! They cannot abort (promised to commit if asked).
  They cannot commit (haven't received commit message).
  Must wait for coordinator to recover.
  → "In-doubt" transactions block database resources (locks, disk space)
  → Can last minutes to hours
  → This is why 2PC is described as "blocking" commit protocol
```

**Distributed transactions in practice:**

- XA transactions: Standard for 2PC across heterogeneous databases
- Too slow for high-performance systems
- Coordinator becomes single point of failure
- Many cloud/microservice architectures avoid them

#### Fault-Tolerant Consensus Algorithms

Consensus algorithms solve the coordinator single point of failure.

```
Requirements for consensus:
  Uniform agreement: No two nodes decide different values
  Integrity: Any decided value must have been proposed by some node
  Validity: A node decides only once
  Termination (liveness): Every non-crashed node eventually decides

Algorithms: Paxos, Raft, Zab (ZooKeeper), Viewstamped Replication

Raft (most understandable):
  - One leader elected from quorum
  - Leader handles all writes (total order)
  - Leader replicates to followers; waits for quorum ACK before committing
  - Leader failure -> new election; must win majority of votes

Safety: Works correctly with minority of nodes failed
Liveness: Requires majority of nodes to be available
```

```
Raft Leader Election:
All nodes start as followers.
Follower hasn't heard from leader in timeout -> becomes candidate.
Candidate sends RequestVote to all nodes.
Nodes grant vote if:
  - Haven't voted in this term
  - Candidate's log is at least as up-to-date as own log
If candidate gets majority -> becomes leader.
Leader sends heartbeats to maintain leadership.
```

#### Membership and Coordination Services (ZooKeeper, etcd)

```
ZooKeeper / etcd provides:
  - Linearizable atomic operations (compare-and-set)
  - Total ordering of operations
  - Failure detection (sessions with heartbeats/timeouts)
  - Change notifications (watches)

Use cases:
  - Leader election (who is the primary?)
  - Service discovery (which port is service X running on?)
  - Configuration management (all nodes get consistent config)
  - Distributed locks / leases
  - Partition assignment in distributed databases

NOT suitable for:
  - Storing application data (small amount of data, highly replicated)
  - High-throughput operations (designed for coordination, not data storage)
```

---

### Chapter 9 Summary

```
Consistency Models (strongest to weakest):
+----------------------------+-----------------------------------------------+
| Model                      | Guarantee                                     |
+----------------------------+-----------------------------------------------+
| Linearizability            | Appears as single copy; real-time ordering    |
+----------------------------+-----------------------------------------------+
| Sequential consistency     | All nodes see same order (not real-time)      |
+----------------------------+-----------------------------------------------+
| Causal consistency         | Causally related ops in order; concurrent ok  |
+----------------------------+-----------------------------------------------+
| Eventual consistency       | Eventually converge; no order guarantees      |
+----------------------------+-----------------------------------------------+

Consensus in Practice:
  - Total order broadcast underlies replicated state machines
  - 2PC provides atomicity across nodes but is blocking
  - Raft/Paxos provide fault-tolerant non-blocking consensus
  - ZooKeeper/etcd: coordination services built on consensus
  - CAP: Can't have both linearizability and availability during partition
```

---

# Part III: Derived Data

> Integrating multiple data systems is one of the most important and underappreciated aspects of software engineering.

**Systems of record vs. derived data:**

```
Systems of record (source of truth):
  - Authoritative version of data
  - Each fact represented exactly once
  - Usually normalized

Derived data:
  - Result of transforming existing data from another system
  - Caches, indexes, materialized views, denormalized views
  - Can be re-created if lost (from the system of record)
  - Often optimized for specific read patterns
```

---

## Chapter 10: Batch Processing

### Core Concept: Three Types of Systems

```
Online systems (services):
  Wait for client request -> handle quickly -> send response
  Primary metric: response time, availability

Batch processing systems (offline):
  Take large input -> run job -> produce output
  No user waiting; jobs run periodically (daily, hourly)
  Primary metric: throughput

Stream processing (near-real-time):
  Similar to batch but operates on events as they happen
  Lower latency than batch; higher latency than online
```

---

### 10.1 Batch Processing with Unix Tools

**The Unix philosophy** (Doug McIlroy, 1964):

1. Make each program do one thing well
2. Expect output of every program to become input to another
3. Design software to be tried early, iterate, rebuild if needed
4. Use tools in preference to unskilled help

**Uniform interface**: Files (file descriptor = just an ordered sequence of bytes).
All Unix tools read from stdin/stdout → any program's output can feed another's input.

```bash
# Log analysis with Unix pipes:
cat /var/log/nginx/access.log |
  awk '{print $7}' |           # Extract URL field
  sort           |              # Sort alphabetically
  uniq -c        |              # Count consecutive duplicates
  sort -r -n     |              # Sort by count (descending)
  head -n 5                     # Top 5 only

# The SAME data flows through each step
# Each step reads input, transforms, writes output
# Pipes connect them with no intermediate disk storage (streaming)
```

**Sorting vs. In-memory aggregation**:

- Small dataset: In-memory hash table (Ruby example) is fine
- Large dataset: Unix `sort` spills to disk automatically, parallelizes on multi-core
- Sort has sequential I/O patterns → efficient on disk

**Limitations of Unix tools**:

- Only works on one machine
- Need distributed equivalent for multi-machine

---

### 10.2 MapReduce and Distributed Filesystems

**HDFS (Hadoop Distributed File System)**:

```
Architecture:
  NameNode: Tracks which blocks are on which DataNodes (metadata)
  DataNodes: Store actual file blocks

Each file split into blocks (typically 128MB each)
Each block replicated to 3 DataNodes (by default)

Reading: NameNode says "block X is on DataNode 3,7,12"
         Client reads from any replica

Writing: Pipeline replication: Client -> DN1 -> DN2 -> DN3
```

**MapReduce Job Execution**:

```
Input: Large dataset (files in HDFS)

Map Phase:
  Input is split into InputSplits (typically one per HDFS block)
  For each split: a Mapper task runs on the node storing that block
  Mapper reads records, produces (key, value) pairs
  "Moving computation to data" (not data to computation!)

Shuffle Phase:
  All (key, value) pairs with same key sent to same Reducer node
  Data sorted by key on the way (to group same keys together)

Reduce Phase:
  Reducer receives sorted (key, [values...]) pairs
  Produces output (key, value) pairs
  Written to HDFS

Example (word count):
  Map:    ("hello world") -> [("hello",1), ("world",1)]
  Reduce: ("hello", [1,1,1]) -> ("hello", 3)
```

```
MapReduce Execution Flow:

HDFS Blocks: [Block1][Block2][Block3]
                 |       |       |
             Map 1    Map 2    Map 3   <- Run on same nodes as blocks
                 \       |       /
                  \      |      /      <- Shuffle: sort by key, partition
                   \     |     /       <- All same keys go to same reducer
              [Reduce1] [Reduce2] [Reduce3]
                    \      |      /
                     HDFS Output
```

---

### 10.3 MapReduce Join Algorithms

**Reduce-Side Joins (Sort-Merge Join)**:

```
Problem: Join user events with user data (stored in separate files)

Map Phase:
  User events mapper:  emit(user_id, {event: activity_record})
  User data mapper:    emit(user_id, {user: user_record})

Sort-Merge Join:
  All records with same user_id sorted together
  First come user records (secondary sort by type)
  Reducer receives: user_record, activity1, activity2, activity3...
  Reducer joins and emits combined records

Cost: All records for same key must be sent over network to same reducer
      Network and sorting overhead
```

**Map-Side Joins**:

When one dataset is small enough to fit in memory:

```
Broadcast Hash Join:
  Small dataset: Load entire user table into each Mapper's memory
  Map Phase:     For each activity record, look up user in in-memory map
  No Reduce needed!

  + Much faster than reduce-side join
  - Requires small dataset to fit in mapper memory (few GB)
```

```
Partitioned Hash Join (Bucketed Map Join):
  Both datasets partitioned in same way on same key.
  Mapper for partition K only needs to read partition K of user table.
  -> Each mapper uses only small fraction of user table.

  Requirement: Both datasets partitioned and sorted by same key.
```

**Handling Skew (Hot Keys)**:

```
Problem: One key (e.g., celebrity user_id) has millions of records
         -> One reducer gets all the work (hot spot)

Solution:
  1. Determine hot keys in advance
  2. Spread hot key's mapper output across multiple reducers
  3. Replicate other dataset's records for that key to all those reducers
  4. Final combine step merges partial results
```

---

### 10.4 Output of Batch Workflows

Batch processing outputs used for:

**1. Building search indexes (Lucene/Solr)**:

```
Batch job:
  Input: Large document corpus
  Map:   Extract (term, doc_id, position) tuples
  Reduce: Build term inverted index
  Output: Lucene index files on HDFS
  Deploy: Copy to search servers
```

**2. Key-value stores**:

```
Batch job builds a read-only key-value store (ElephantDB, etc.)
Stores results in HDFS as SSTable-like files
Deploy: Bulk load into database (much faster than individual writes)
```

**3. Analytics / machine learning models**:

- Aggregate statistics
- Training data for ML models
- Recommendation system outputs

**Philosophy of batch process outputs**:

- Input is immutable (never modify input data)
- Output to a different location (not in-place)
- If job fails: retry from scratch (idempotent)
- If code is buggy: re-run on same input to fix output
- Easy rollback: just swap old output back
- "Human fault tolerance"

---

### 10.5 Hadoop vs. Distributed Databases (MPP)

```
+--------------------+---------------------+---------------------+
| Aspect             | Hadoop/MapReduce     | MPP Database        |
+--------------------+---------------------+---------------------+
| Storage format     | Any (text, binary)  | Internal format only|
| Data model         | Arbitrary bytes     | Relational/specific |
| Query language     | Java code (flexible)| SQL                 |
| Fault tolerance    | Designed for faults | Assumes rare faults |
|                    | (commodity hardware)| (expensive hardware)|
+--------------------+---------------------+---------------------+
| Read model         | Sequential scan     | Index-based         |
| Indexing           | None built-in       | Rich indexing       |
| On failure         | Restart failed task | Abort whole query   |
| Data locality      | Yes (move compute   | Data must be loaded |
|                    | to data)            | into database       |
+--------------------+---------------------+---------------------+
| Best for           | Diverse processing  | Fast SQL analytics  |
|                    | models, ETL, ML     | on structured data  |
+--------------------+---------------------+---------------------+
```

**The "sushi principle"**: raw data is better. Store raw data in HDFS, transform on the way out. Don't pre-format when ingesting.

---

### 10.6 Beyond MapReduce

**Problems with MapReduce**:

- Must write output to disk after each step (even if next step immediately reads it)
- Lots of disk I/O overhead for multi-step workflows
- Slow for iterative algorithms (ML, graph processing)

**Dataflow engines** (Spark, Tez, Flink):

```
Key improvements over MapReduce:

1. Pipelining: Pass output of one step directly to next
   (no intermediate disk write/read)

2. Task granularity: Not always Map + Reduce + Shuffle
   Custom operators, flexible DAG of computations

3. Reuse JVM: Don't start new JVM for each task (reduces startup overhead)

4. Sort only when needed: MapReduce always sorts between Map and Reduce
   Dataflow engines only sort when sort is semantically required

Example (Spark):
  sc.textFile("hdfs://...")
    .flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
    .saveAsTextFile("hdfs://...")

  Spark builds a DAG, pipelines operations, only materializes to disk
  when needed (shuffle boundaries, checkpoints)
```

**Materialization of intermediate state**:

- MapReduce: Materializes (writes to disk) after every step
- Dataflow: Avoids materialization unless necessary
- Trade-off: If task fails, how far to rerun?
  - MapReduce: Restart just the failed task (intermediate state on disk)
  - Spark: Rerun from last checkpoint or from source (lineage)

**Fault tolerance via lineage**:

```
Spark's RDD (Resilient Distributed Dataset):
  Each RDD knows how it was derived (from parent RDD via transformation)
  On failure: Recompute only the failed partition using lineage
  No need to replicate intermediate state

Tradeoff:
  Short lineage: fast recovery
  Long lineage: slow recovery (recompute from scratch)
  Solution: Checkpoint periodically
```

---

### 10.7 Graph Processing and Iterative Algorithms

**Graph algorithms** (PageRank, shortest path, clustering) require many iterations — each iteration reads result of previous one.

**Bulk Synchronous Parallel (BSP) / Pregel model** (Google Pregel, Apache Giraph, GraphX):

```
Each vertex:
  Receives messages from previous iteration
  Computes its new value based on messages + own state
  Sends messages to neighboring vertices
  Votes to "halt" when done

Execution:
  Iteration 1: All active vertices compute, send messages
  Iteration 2: All vertices with incoming messages compute
  ...continues until all vertices vote to halt

PageRank in Pregel:
  Each vertex starts with rank 1/N
  Iteration: Each vertex sends its rank to neighbors
  Next iteration: Each vertex updates rank based on received messages
  Repeat until convergence
```

---

### Chapter 10 Summary

```
Batch Processing Evolution:
+-----------------+------------------------------------------+
| System          | Key Concepts                             |
+-----------------+------------------------------------------+
| Unix Tools      | Uniform interface (files), composability,|
|                 | pipes, text format, single-machine        |
+-----------------+------------------------------------------+
| MapReduce/HDFS  | Distributed, data locality, fault         |
|                 | tolerance, commodity hardware             |
+-----------------+------------------------------------------+
| Dataflow        | Pipelines, avoid intermediate             |
| (Spark/Flink)   | materialization, flexible DAG, lineage    |
+-----------------+------------------------------------------+
| Graph/Iterative | Pregel/BSP model, vertex-centric         |
| (Giraph/GraphX) | parallel computation                      |
+-----------------+------------------------------------------+

Key principles:
- Immutable inputs (idempotent, rerunnable)
- Move computation to data (data locality)
- Fault tolerance via retry/lineage
- Unix philosophy at scale: composable components
```

---

## Chapter 11: Stream Processing

### Core Concept: Events as They Happen

Batch processing: Fixed, bounded input dataset.
Stream processing: Unbounded, continuously arriving events.

**Event**: A small, immutable record containing something that happened.

- User action: clicked button, made purchase
- Sensor reading: temperature, GPS location
- Database write: a change to data

---

### 11.1 Transmitting Event Streams

#### Direct Messaging (Brokerless)

- UDP multicast (financial industry, low latency)
- ZeroMQ, nanomsg
- REST/RPC calls
- Problem: Assumes consumer is online; no buffering; producer must track consumers

#### Message Brokers (Message Queues)

```
Classic message brokers (RabbitMQ, ActiveMQ, JMS, MSMQ):
  Producer -> Broker -> Consumer

  + Consumer can be offline temporarily (broker buffers)
  + Acknowledgment: if consumer crashes, broker redelivers
  + Fan-out: multiple consumers can receive from same queue

  - Mostly treat messages as transient (delete after ACK)
  - Can't replay old messages (like re-reading a log)
  - Messages consumed in order within a queue, but multiple consumers
    may process concurrently (hard to maintain order)
```

#### Partitioned Logs (Log-Based Message Brokers)

```
Apache Kafka architecture:
  - Messages organized into topics
  - Each topic partitioned into multiple logs (partitions)
  - Each partition is an append-only sequence of messages
  - Each message has an offset (position in log)
  - Consumer group: Multiple consumers, each assigned to one or more partitions

  +-----------+    +-----------+    +-----------+
  |Partition 0|    |Partition 1|    |Partition 2|
  |offset:    |    |offset:    |    |offset:    |
  | 0: msg A  |    | 0: msg D  |    | 0: msg G  |
  | 1: msg B  |    | 1: msg E  |    | 1: msg H  |
  | 2: msg C  |    | 2: msg F  |    | 2: msg I  |
  +-----------+    +-----------+    +-----------+

Consumer group 1: Consumer A -> Partition 0,1
                  Consumer B -> Partition 2

Consumer group 2: Consumer C -> All partitions (independent offset)
```

**Key advantage of log-based brokers**:

- Consumers maintain their own offset pointer
- Reading a message doesn't delete it
- **Replaying** old messages is trivial (just reset offset)
- Multiple independent consumer groups, each at their own position
- Messages retained for configurable period (days/weeks)

**Comparison: Traditional broker vs. Log-based**:

```
+---------------------------+------------------+--------------------+
| Aspect                    | Traditional      | Log-based (Kafka)  |
+---------------------------+------------------+--------------------+
| Message ordering          | Queue (FIFO)     | Ordered per partition|
| Multiple consumers        | Fan-out          | Separate offsets   |
| Replay old messages       | Not possible     | Yes (reset offset) |
| Deletion after consume    | Yes              | After retention    |
| Fault tolerance           | ACK/redeliver    | Persistent log     |
| Throughput                | Moderate         | Very high          |
| Best for                  | Task queues,     | Event streams,     |
|                           | work distribution| CDC, analytics     |
+---------------------------+------------------+--------------------+
```

---

### 11.2 Databases and Streams

**Keeping systems in sync**:

```
Problem:
  Database: primary source of truth
  Search index: derived from database
  Cache: derived from database
  Data warehouse: derived from database

  How to keep them in sync?
  Option 1: Dual writes (write to both explicitly from application code)
            -> Race conditions! Inconsistency if one write fails.

  Option 2: Change Data Capture (CDC)
```

#### Change Data Capture (CDC)

```
CDC: Capture every change made to the database and stream it.

Implementation:
  - Parse replication log (MySQL binlog, PostgreSQL WAL, MongoDB oplog)
  - Trigger-based (slower, more overhead)

  +----------+   replication log   +--------+   stream   +---------+
  | Database | ---> (WAL/binlog) -> |  CDC   | ---------> | Kafka   |
  +----------+                     | Process|            +---------+
                                   +--------+              |     |
                                                     +-----+  +--+----+
                                                     | Search|  | Cache|
                                                     | Index |  +------+
                                                     +-------+

Benefits:
  - Single write to database (no dual-write race conditions)
  - All derived systems stay in sync via log
  - Can replay the entire log to rebuild any derived system

Initial Snapshot:
  For a new consumer starting from scratch:
  Take consistent snapshot of database (while recording log position)
  Consumer processes snapshot, then continues from recorded log position
```

**Log compaction**: Keep only the most recent value for each key.

```
Original log:
  set user_1.name="Alice"
  set user_1.email="old@email.com"
  delete user_2
  set user_1.email="new@email.com"

Compacted log (keep only latest per key):
  set user_1.name="Alice"
  set user_1.email="new@email.com"
  delete user_2   (tombstone)

Consumer starting from beginning of compacted log
gets current state of entire database.
```

#### Event Sourcing

```
Event Sourcing vs CDC:

  CDC: Database changes captured at low level (bytes/rows)
       Application unaware of CDC
       Log = low-level state mutations

  Event Sourcing: Application explicitly designed around events
                  Events represent business actions, not state changes
                  Log = high-level intent

  CDC example:  "row in enrollments table updated: status=cancelled"
  Event sourcing: "student cancelled course enrollment"

Benefits of event sourcing:
  - Richer historical information (intent, not just effect)
  - Easier to evolve application (replay events with new logic)
  - Easier debugging (understand exactly what happened)
  - Auditability

  Limitation: Need to process entire event log to get current state
              (unless you take periodic snapshots)
```

**State, Streams, and Immutability**:

```
The fundamental idea:
  Mutable state = integration of an event stream over time
  Event stream = differentiation of state over time

  Current account balance = sum of all transactions
  Transaction log = the changelog of account balances

  "The truth is the log. The database is a cache of the log." — Pat Helland

Command Query Responsibility Segregation (CQRS):
  Write side: Append events to log (commands)
  Read side:  Maintain materialized views (queries)
  → Multiple different read-optimized views from same event log
```

---

### 11.3 Processing Streams

Three options for processing a stream:

1. Write events to a database/cache/search index (keeping derived state in sync)
2. Push events to users (email alerts, real-time dashboards)
3. Process one or more input streams to produce output streams

**Stream processors** (option 3): Kafka Streams, Apache Storm, Spark Streaming, Apache Flink, Samza, Beam

#### Uses of Stream Processing

**Complex Event Processing (CEP)**:

- Search for patterns in event streams (like regex for events)
- Store queries, run events past them
- Used for fraud detection, trading, monitoring

**Stream Analytics**:

- Aggregations over time windows
- "How many requests per second in last 5 minutes?"
- Probabilistic algorithms: Bloom filters, HyperLogLog, percentile estimation

**Maintaining Materialized Views**:

- Keep derived data (search index, cache) up to date
- Read events, update view
- Different from CEP: needs ALL historical events (not just a window)

**Search on Streams (Percolator)**:

- Queries stored; documents run past them (reverse of normal search)
- Used by media monitoring services

---

### 11.4 Reasoning About Time

**Event time vs. Processing time**:

```
Event time:    When the event actually occurred (e.g., client timestamp)
Processing time: When the stream processor sees the event

Problem: Processing time >> Event time due to:
  - Network delays
  - Batching
  - Mobile devices offline for hours/days
  - Clock skew between producer and processor

Example: "How many events per minute?"
  Processing-time windows: easy but inaccurate
  Event-time windows: accurate but must handle out-of-order events
```

**Tumbling windows**:

```
Fixed-size, non-overlapping windows:
  [0:00-0:01][0:01-0:02][0:02-0:03]...
  Each event belongs to exactly one window.
```

**Hopping windows**:

```
Fixed size, fixed overlap:
  Size=5min, hop=1min:
  [0:00-0:05][0:01-0:06][0:02-0:07]...
  Each event belongs to multiple windows.
  "Rolling average over last 5 minutes, updated every minute"
```

**Sliding windows**:

```
All events within some time interval of each other:
  "Sessions within 30 minutes of each other"
```

**Session windows**:

```
Group events with no gap > timeout:
  User active at T=0, T=5, T=8 → one session
  User active at T=0, T=5, gap of 30+ min, T=40 → two sessions
```

**Handling stragglers (late events)**:

```
Option 1: Ignore late events (simplest, may lose data)
Option 2: Publish a correction when late event arrives
Option 3: Wait until all events for a window arrived
          (watermarks: declare "all events before time T have arrived")

Watermarks: Heuristic based on "how late can events be?"
            If event arrives after watermark, it's a straggler.
            Trade-off: Lower watermark = faster results but more stragglers
                       Higher watermark = more accurate but higher latency
```

---

### 11.5 Stream Joins

Three types of joins in stream processing:

#### Stream-Stream Join (Windowed Join)

```
Example: Search events joined with click events (within 1 hour)

  Search events: [user_id, query, timestamp]
  Click events:  [user_id, URL, timestamp]

  Goal: For each search, find if user clicked on result within 1 hour

  Stream processor: Maintain state of recent searches per user
                    When click arrives, look up recent search for same user
                    Emit (search, click) pair if found within window

  Both input streams are active and changing.
```

#### Stream-Table Join (Stream Enrichment)

```
Example: Enrich activity events with user profile data

  Activity stream: [user_id, activity]
  User profile table: [user_id, name, email, ...]

  Goal: Add user name to each activity event

  Load user database into processor memory
  For each activity event, look up user profile
  Emit enriched event

  Table side is relatively static; stream side is active.
  Table can be kept up-to-date via CDC stream.
```

#### Table-Table Join (Materialized View Maintenance)

```
Example: Twitter home timeline cache

  Tweets table: [tweet_id, author_id, text]
  Follows table: [follower_id, followee_id]

  Goal: Maintain home timeline for each user

  When user A tweets:
    Look up all followers of A
    Add tweet to each follower's timeline cache

  When user B follows C:
    Add all of C's recent tweets to B's timeline

  Both sides are changing. Output is a maintained materialized view.
```

**Time-dependence of joins**: The order of events matters!

```
"Enrich events with exchange rate at time of event"
  Exchange rates change over time.
  Must use the rate that was valid AT THE TIME of the event.
  → Keep history of exchange rates, not just current value.
```

---

### 11.6 Fault Tolerance in Stream Processing

**Unlike batch processing**: Can't just restart failed jobs from scratch (stream never ends).

**Microbatching** (Spark Streaming):

```
Break stream into small batches (e.g., 1 second)
Process each microbatch as a tiny batch job
Advantages: Inherits batch fault tolerance (restart failed microbatch)
Disadvantages: Higher latency than true streaming
```

**Checkpointing** (Flink, Apache Storm):

```
Periodically save operator state to durable storage (HDFS)
On failure: Replay from last checkpoint
Messages between checkpoint and failure are re-processed from upstream
```

**Idempotent operations**:

```
Idempotent: Operation can be applied multiple times with same result
  e.g., "set value to X" is idempotent
  vs. "increment counter by 1" is NOT idempotent (double-apply = double-count)

For at-least-once delivery + idempotent operations = effectively-once semantics
(External storage checks idempotency key before applying)
```

**Exactly-once semantics**:

```
True exactly-once requires distributed transactions or idempotent consumers.
Kafka Streams: Exactly-once using Kafka transactions.
Flink/Beam: Checkpoint + transactional sinks.

Approach: Assign unique operation ID to each event processing.
          Sink checks if this operation already applied before committing.
```

---

### Chapter 11 Summary

```
Stream Processing Architecture:

  Sources (Kafka, Kinesis, CDC)
       |
       v
  Stream Processor (Flink, Kafka Streams, Spark Streaming)
       |-- Windowing and aggregation
       |-- Joins (stream-stream, stream-table, table-table)
       |-- Filtering and enrichment
       |-- State management (local state + changelog to Kafka)
       v
  Sinks (Database, Search, Cache, Downstream streams)

Key concepts:
  - Event time vs. processing time
  - Windows (tumbling, hopping, sliding, session)
  - Watermarks for handling late data
  - Three join types (stream-stream, stream-table, table-table)
  - Fault tolerance: microbatching, checkpointing, idempotence
  - Exactly-once vs. at-least-once semantics
```

---

## Chapter 12: The Future of Data Systems

### Core Concept: Combining Specialized Tools

No single data system satisfies all requirements. Real applications integrate multiple specialized tools.

```
Modern Data Application Stack:
+----------------------------------------------------------+
|                     Application                           |
+----------------------------------------------------------+
      |          |          |          |          |
+--------+  +--------+  +--------+  +--------+  +--------+
|Primary  |  |Search  |  | Cache  |  | Data   |  |  ML    |
| DB      |  | Index  |  |(Redis) |  | Warehouse  | Models|
|(Postgres)|  |(Elastic)|         |  |(Redshift)  +--------+
+--------+  +--------+  +--------+  +--------+
     |           |          |          |
     +-----CDC---+--CDC/ETL-+----------+
                 (Kafka / Change Data Capture)
```

---

### 12.1 Data Integration

**The fundamental challenge**: Combining different data representations.

```
Derived data (caches, indexes) MUST stay in sync with primary data.
Otherwise: Stale search results, wrong cache values, inaccurate analytics.

Solutions:
1. Synchronous writes (dual-write): Write to both DB and cache in application code
   Problems: Race conditions, partial failures, no atomicity

2. Total order broadcast (CDC via Kafka):
   All writes go through a single ordered log.
   All derived systems consume the same log in order.
   -> Guaranteed consistency (same order for all consumers)
   -> Can replay to rebuild from scratch
   -> Can add new consumers at any time
```

**Lambda Architecture** (Nathan Marz):

```
+----------+    +-----------+    +------------+
| All data |    | Batch     |    | Accurate   |
|          |--> | layer     |--> | historical |
|          |    | (Hadoop)  |    | views      |
|          |    +-----------+    +------------+
|          |                           +
|          |    +-----------+    +-----v------+
|          |--> | Speed     |--> | Realtime   |---> Merged view
|          |    | layer     |    | (recent    |     to user
|          |    | (stream)  |    | data only) |
+----------+    +-----------+    +------------+

Problem with Lambda: Must maintain two separate code paths (batch + stream)
                     Complex to keep both producing same results
                     Hard to do incremental changes
```

**Kappa Architecture** (Jay Kreps):

```
"Use only a stream processing system"
  - All data stored as immutable log (Kafka with long retention)
  - Process stream to produce derived views
  - When code changes: Replay entire log with new processing code
  - Old and new consumer run in parallel; swap when new one is caught up

  Simpler than lambda; works well when input data is compact (log-compacted)
```

---

### 12.2 Unbundling Databases

Traditional databases combine many features:

- Durable storage
- Query language
- Indexes
- Transactions
- Replication

**Unbundling** = take these apart, use best-of-breed for each:

```
Database internals as building blocks:

  Feature              Implementation
  ──────────────────   ───────────────────────────────
  Durable log          Kafka (append-only, replicated)
  Ordered events       Kafka partitions
  Indexes              Elasticsearch, Lucene
  Caching              Redis, Memcached
  Full-text search     Elasticsearch
  Data warehousing     Redshift, BigQuery, Snowflake
  OLTP queries         PostgreSQL, MySQL
  Graph queries        Neo4j

  Glue: CDC / event streams connecting them all
```

**Designing applications around dataflow**:

```
Traditional: Application writes to DB; DB handles derived state
Dataflow: Application produces events; derived systems subscribe

Benefits:
  - Loose coupling: Each system independently evolvable
  - Easier to add new consumers (don't change producers)
  - Fault isolation
  - Historical replay
  - Clear data lineage (where did this data come from?)
```

**Observing Derived State**:

```
Read path:          User requests -> derived materialized view -> response
Write path:         Event -> stream -> updates materialized view

The derived view must eventually reflect all writes.
The speed at which this happens determines "how eventual" eventual consistency is.

Extending dataflow to user interfaces (reactive programming):
  When underlying data changes -> UI automatically updates
  (like a spreadsheet: change one cell, others update automatically)

  Frameworks: React/Redux, Elm, Meteor, Firebase
```

---

### 12.3 Aiming for Correctness

**The end-to-end argument**: A function implemented in the lower levels of a system may be incomplete; it must be checked end-to-end.

```
Example: TCP guarantees delivery, but:
  - TCP retries can deliver duplicate packets
  - Application must handle exactly-once semantics itself

  Database provides ACID, but:
  - If application retries on network error, it may duplicate operations
  - Application must use idempotency keys / two-phase commit

  The end-to-end check (idempotency key, uniqueness constraint)
  provides the true guarantee, not TCP or the database alone.
```

**Enforcing constraints with event logs**:

```
Problem: How to enforce "username must be unique" in a distributed system
         without expensive coordination?

Solution using total order broadcast:
  1. Append event to log: {claim_username: "alice"}
  2. Log has total order → all claims processed in same order on all nodes
  3. First claim for "alice" wins; subsequent claims rejected
  4. Single-threaded consumer, linearizable registration

  Trade-off: Must wait for event to propagate through log before confirming
             (if you need synchronous confirmation)
             OR: Tentative reservation → async confirmation (eventual)
```

**Timeliness and Integrity**:

```
Timeliness: User observes system in up-to-date state (consistency)
Integrity: Absence of corruption; no data loss; no contradictions

These are different!
  - System can be timely but have integrity violations (stale cache, fine; corrupt data, not)
  - System can have integrity but not timeliness (consistent but eventual)

For many applications:
  - Integrity is CRITICAL (losing data or corrupting it is catastrophic)
  - Timeliness can be relaxed (slightly stale data is acceptable)

→ Focus on integrity guarantees, allow loosening timeliness.
  Use event log + idempotent consumers + end-to-end checks.
```

**Coordination-Avoiding Data Systems**:

```
Traditional: Synchronous coordination for every write (2PC, serializable transactions)
             → Good consistency, but high latency and low availability

Alternative: Avoid coordination; use dataflow with integrity checks
  - Write to local log (fast, no cross-node coordination)
  - Async replication to all derived systems
  - End-to-end idempotency/uniqueness checks prevent double-processing
  - Periodic consistency checks detect and fix anomalies

  Result: Better performance AND fault tolerance
          With STRONG integrity guarantees
          But WEAK timeliness (eventual consistency)

  "Coordination and constraints reduce apologies due to inconsistencies,
   but may increase apologies due to outages. Find the right trade-off."
```

---

### 12.4 Doing the Right Thing (Ethics)

> "Technology is not good or bad in itself — what matters is how it is used and how it affects people."

**Predictive Analytics**:

- Data analysis for weather/disease prediction: fine
- Predicting if convict will reoffend, loan default, insurance claims: directly affects people's lives
- "Algorithmic prison": systematically excluded from jobs, services, housing by opaque algorithms
- No proof of guilt, no appeal, no explanation

**Bias and Discrimination**:

- Algorithms learn from biased historical data → amplify existing biases
- "Machine learning is like money laundering for bias"
- Anti-discrimination laws: can't use protected attributes (race, gender, religion...)
- But proxy features (postal code, IP address) may be highly correlated with protected attributes

**Feedback Loops**:

```
Self-reinforcing feedback example:
  Employer uses credit score to hire.
  Person has bad credit (not their fault).
  Can't get hired.
  Can't pay bills.
  Credit score worsens.
  Less likely to be hired.
  → Downward spiral.
```

**Privacy and Tracking**:

- Distinction: Service user chose to use (service for user) vs. tracking as side effect (surveillance)
- Advertiser-funded services: users are the product, not the customer
- "Replace 'data' with 'surveillance' and see if it sounds good"
- "The greatest mass surveillance infrastructure the world has ever seen"

**Data Ownership and Rights**:

- Informed consent: Do users understand what data is collected and how used?
- Freedom of choice: Can users opt out without losing essential services?
- Right to explanation: Can affected individuals understand why a decision was made?

**Responsibility**:

- Algorithmic decisions can't be appealed like human decisions
- When things go wrong, who is responsible?
- Engineers have ethical responsibility, not just product managers

---

### Chapter 12 Summary

```
Future of Data Systems (Key Themes):

1. Data Integration
   - No single system does everything; combine specialized tools
   - CDC + event streams as the connective tissue
   - Lambda vs. Kappa architecture

2. Unbundling Databases
   - Separate concerns: storage, indexes, queries, transactions
   - Compose best-of-breed components via dataflow
   - Total order broadcast as the coordination mechanism

3. Correctness Without Coordination
   - End-to-end argument: check integrity at application level
   - Idempotent operations + event logs = strong integrity
   - Timeliness (consistency) and integrity are separate concerns

4. Doing the Right Thing
   - Predictive analytics affects real people's lives
   - Algorithms can encode and amplify discrimination
   - Privacy and tracking: users must be respected, not exploited
   - Engineers have ethical responsibility
```

---

# Quick Reference Diagrams

## Storage Engine Decision Tree

```
What is your primary workload?

OLTP (many small reads/writes)          OLAP (few large scans)
         |                                       |
         v                                       v
    Row-oriented storage                Column-oriented storage
    (PostgreSQL, MySQL, MongoDB)        (Redshift, BigQuery, Parquet)
         |
    +-----------+
    v           v
B-Tree       LSM-Tree
(most OLTP)  (write-heavy,
              time-series,
              HBase, Cassandra)
```

## Replication Decision Tree

```
Do you need writes from multiple locations?
         |
    +----+----+
    v         v
   YES        NO
    |          |
Multi-Leader  Single-Leader
(multi-DC,   (simple, most common)
 offline)         |
                  |
         Do you need high availability
         with no leader failover?
                  |
            +-----+-----+
            v           v
           YES          NO
            |            |
         Leaderless   Single-Leader
         (Dynamo,     is sufficient
          Cassandra)
```

## Transaction Isolation Quick Reference

```
Anomaly/Level     | Read    | Read     | Snapshot  | Serial-
                  | Uncommit| Committed| Isolation | izable
------------------+---------+----------+-----------+----------
Dirty reads       |   YES   |    NO    |    NO     |   NO
Non-repeatable rd |   YES   |   YES    |    NO     |   NO
Phantom reads     |   YES   |   YES    | Partial   |   NO
Lost updates      |   YES   |   YES    |   NO*     |   NO
Write skew        |   YES   |   YES    |   YES     |   NO
(*) With automatic detection enabled
```

## Consensus vs. Coordination Services

```
ZooKeeper / etcd:
  Linearizable reads/writes
  Total order broadcast
  Leader election
  Service discovery
  Configuration management
  Distributed locks

  NOT for: High-throughput application data storage
  USE for: Small amounts of coordination data

Raft/Paxos:
  The underlying consensus algorithm
  Used internally by ZooKeeper (Zab), etcd (Raft), Kafka (KRaft)

  Requires: Majority of nodes available for progress
  Provides: Safety (no split brain) + Liveness (eventually decides)
```

## Batch vs. Stream Processing

```
+------------------+-------------------+--------------------+
| Aspect           | Batch (Spark)     | Stream (Flink)     |
+------------------+-------------------+--------------------+
| Input            | Bounded (finite)  | Unbounded (infinite|
| Latency          | Minutes to hours  | Milliseconds       |
| Windowing        | Not needed        | Essential (time,   |
|                  | (full dataset)    | count, session)    |
| State            | Ephemeral         | Managed (checkpoint|
| Fault recovery   | Restart task      | Checkpoint/replay  |
| Output           | Fixed dataset     | Continuous updates |
| Time semantics   | N/A               | Event time vs.     |
|                  |                   | processing time    |
+------------------+-------------------+--------------------+
```

---

# Key Concepts Glossary

| Term                     | Definition                                                                    |
| ------------------------ | ----------------------------------------------------------------------------- |
| **ACID**                 | Atomicity, Consistency, Isolation, Durability — transaction safety guarantees |
| **Backpressure**         | Signal from consumer to producer to slow down (avoid overload)                |
| **CAP Theorem**          | Consistency, Availability, Partition-tolerance — can't have all three         |
| **CDC**                  | Change Data Capture — capturing database changes as a stream                  |
| **CQRS**                 | Command Query Responsibility Segregation — separate write and read models     |
| **CRDTs**                | Conflict-free Replicated Data Types — merge concurrent edits automatically    |
| **Compaction**           | Merging log segments, keeping only latest value per key                       |
| **Consistent hashing**   | Partitioning where adding/removing nodes moves minimal data                   |
| **Dirty read**           | Reading uncommitted data from another transaction                             |
| **Durability**           | Data survives crashes (written to disk/replicated)                            |
| **ETL**                  | Extract, Transform, Load — moving data from OLTP to data warehouse            |
| **Exactly-once**         | Processing each event once despite failures                                   |
| **Fencing token**        | Monotonically increasing token preventing stale leaders from acting           |
| **Hot spot**             | A partition or node receiving disproportionately more load                    |
| **Idempotent**           | Operation that can be applied multiple times with the same effect             |
| **Impedance mismatch**   | Disconnect between object model and relational model                          |
| **LWW**                  | Last Write Wins — resolving conflicts by timestamp (data loss risk)           |
| **LSM-Tree**             | Log-Structured Merge-Tree — write-optimized storage engine                    |
| **Linearizability**      | Appears as single copy of data, all ops atomic in real time                   |
| **MVCC**                 | Multi-Version Concurrency Control — readers don't block writers               |
| **Phantom read**         | Write in one transaction changes results of search in another                 |
| **Polyglot persistence** | Using different database types for different parts of an app                  |
| **Quorum**               | Minimum votes required for a distributed operation to succeed                 |
| **Read skew**            | Reading inconsistent data due to concurrent writes (non-repeatable read)      |
| **Serializability**      | Transactions appear to execute one at a time (strongest isolation)            |
| **Sharding**             | See Partitioning                                                              |
| **Split brain**          | Two nodes both think they are the leader (dangerous)                          |
| **SSTable**              | Sorted String Table — sorted key-value file used in LSM-trees                 |
| **Tombstone**            | Marker indicating a key has been deleted (for log-structured stores)          |
| **WAL**                  | Write-Ahead Log — durability mechanism, log before applying changes           |
| **Write amplification**  | One logical write causes multiple physical writes                             |
| **Write skew**           | Two transactions read same data, update different objects based on it         |

---

_Notes compiled from "Designing Data-Intensive Applications" by Martin Kleppmann (O'Reilly, 2017)_

_These notes are for personal study and understanding. All concepts and ideas belong to the original author._
