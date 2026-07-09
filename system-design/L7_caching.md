# Caching in Distributed Systems

### 1. What is Caching?
Caching is a foundational concept in computer science aimed at **reducing repeatable work through storage**. Instead of executing expensive computations or querying a database repeatedly for the same data, the results are saved in local, high-speed memory.
* **Backend Caching Focus:** While client-to-server optimization is valuable, backend engineers focus primarily on optimizing the server-to-database communication layer.
* **The User Cohort Example:** On applications like Instagram, users belonging to similar cohorts (e.g., young software engineers in India who like football) often request similar news feeds. Instead of querying the database and recomputing the feed for every single user request, the server computes it once, stores it in memory, and instantly serves it to the rest of the cohort.

### 2. The Core Problems of Cache Management
Because cache memory is faster but significantly more limited and expensive than database storage, you cannot store an entire petabyte-scale database in memory. This introduces two critical engineering questions:
1. **Cache Eviction (What to kick out?):** When the cache is full and a new popular item (like a viral video) needs to be added, existing data must be evicted. This is governed by a **Cache Eviction Policy**. The most common industry policies include:
   * **LRU (Least Recently Used):** Evicts the data that hasn't been accessed for the longest time.
   * **LFU (Least Frequently Used):** Evicts the data accessed the lowest number of times.
2. **Write Policies (When to update?):** Since a cache is a copy of the database, any data update requires a strategy to synchronize the cache and the database (the original source of truth) together or asynchronously.

### 3. Key Drawbacks & Limitations
Despite its massive performance benefits, caching introduces new architectural challenges:
* **Cache Thrashing:** If a client requests data sequentially (e.g., IDs 1, 2, 3, 4, 1, 2...) in a cache that can only hold three items, an unoptimized cache policy will constantly evict and reload entries. This creates useless work, drives up cache misses, and increases system latency instead of reducing it.
* **Eventual Consistency & Stale Data:** Keeping copies of data means they can fall out of sync. For example, a YouTube video's exact like count might be cached and updated only once per minute to save database resources. While acceptable for social media metrics, this stale data behavior creates severe issues if applied blindly to strict transactional or financial systems.

### 4. Cache Placement Strategies
Caches can be integrated at multiple layers of an enterprise architecture:
* **In-Memory Cache (App Layer):** Built locally inside the application server (e.g., using an in-memory hash map). It is fast but constrained by the application's process memory.
* **Database Cache:** Built natively inside the database engine as a black box to optimize frequent queries automatically.
* **Distributed / Global Cache:** An independent, external caching server system (e.g., Redis or Memcached) accessed via API calls (`GET` and `PUT`) by various services.

### 5. Architectural Recommendation
In complex, large-scale distributed systems, engineers lean heavily on a **Distributed Cache** because it provides three primary advantages:
1. **Independent Scaling:** The caching layer can scale its memory and throughput independently of the application logic.
2. **Decoupled Deployments:** Modifications to the caching algorithms do not require re-deploying or restarting the core application servers.
3. **Shared Access:** Multiple isolated microservices can access the exact same cached data pool without rewriting custom caching logic.
