# Load Balancing and Consistent Hashing

## The Basics of Load Balancing

When a single server cannot handle an increasing number of user requests, systems need to scale by adding more servers. **Load balancing** is the process of distributing these incoming requests evenly across multiple servers (e.g., $N$ servers) so no single machine gets overwhelmed.

## The Standard Hashing Approach (and its Flaw)

A basic way to route a request is by using a hash function combined with a modulo operation:

- **The Process:** The system takes a Request ID (or User ID), passes it through a hash function to get a random number, and then finds the remainder when divided by the number of active servers (`Hash(ID) % N`).
- **The Benefit:** This reliably sends the same user to the same server every time. This is highly efficient because that specific server can store the user's profile in its local cache, preventing slow database lookups on every request.

## The Scaling Problem

The standard modulo approach completely breaks down when you need to add or remove servers:

- If you have 4 servers and add a 5th, your formula changes from `% 4` to `% 5`.
- **Catastrophic Reshuffling:** Because the math changes for every single request, nearly all requests are suddenly routed to entirely different servers.
- **Cache Misses:** All the useful user data stored in the local caches of the original servers is suddenly rendered useless, which can severely bottleneck the system.

## The Solution: Consistent Hashing

To fix this, systems use **Consistent Hashing**. Instead of completely reshaping the entire routing table when a 5th server is added, consistent hashing ensures that the new server only takes a small, proportional slice of the load from each of the existing 4 servers. This minimizes disruption, preserving the vast majority of the cached data and keeping the system stable as it scales up or down.

# What is Consistent Hashing and Where is it Used?

> reference : https://bytebytego.com/courses/system-design-interview/design-consistent-hashing

## The Core Problem

In a distributed system, regular load balancing (using simple modulo arithmetic like `Hash(Request_ID) % N`) causes a massive problem when a server is added or removed. The entire math changes, which shuffles almost all requests to different servers, destroying local caches.

## The Solution: The "Hash Ring"

**Consistent Hashing** solves this by mapping everything onto a circular structure or a "ring" (e.g., positions $0$ to $M-1$):

1. **Mapping Servers:** Server IDs are passed through a hash function and placed at specific points on this ring.
2. **Mapping Requests:** Incoming requests are also hashed and placed on the same ring.
3. **Routing Strategy:** When a request lands on the ring, it simply moves **clockwise** to find the very first server point it encounters. That nearest server handles the request.

## Handling Changes Gracefully

- **Adding a server:** When a new server is added to the ring, it only takes over the requests that fall directly behind it. The other servers are mostly unaffected.
- **Removing a server:** If a server crashes or is removed, its requests simply fall clockwise to the next available server on the ring. The rest of the system stays intact.

## The Flaw: Skewed Distributions

Theoretically, hashes are uniform, but practically—especially with a small number of servers—the server nodes might end up clumped together on the ring. This causes one server to handle a massive chunk of the ring (and thus, a massive load), while others do nothing.

## The Fix: Virtual Servers (Virtual Nodes)

To solve the skewed distribution problem without buying expensive physical servers, engineers use **Virtual Servers**:

- Instead of hashing a physical server just once, you pass it through multiple ($K$) different hash functions.
- If $K=3$, a single physical server will appear at 3 different randomly distributed points on the ring.
- This greatly increases the number of points on the ring.
- **Result:** The ring is carved up into many smaller, interwoven slices, ensuring a highly uniform distribution of requests. If a server goes down, its load is evenly distributed across multiple remaining servers instead of crushing just one.

## Where is it Used?

Consistent hashing is heavily used in distributed systems where scaling and flexibility are critical, specifically in:

- **Web Caches**
- **Distributed Databases**
