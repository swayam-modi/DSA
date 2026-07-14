# System Design: Fundamentals of API Design

### 1. What is an API?

An **Application Programming Interface (API)** is a formally documented contract that allows external consumers to understand how to interact with your code.

- **The Core Rule:** It dictates _what_ your system can do and how to consume it—never _how_ the internal code functions behind the scenes.
- **Functional Analogy:** An API operates exactly like a standard programming function; it establishes a strict name, accepts specialized parameters, checks for explicit errors, and provides a distinct return type.

---

### 2. Basic API Architecture Design (HTTP Mapping)

[ Client Request ]
│
▼
[ DNS / Domain Gateway ]
(e.g., api.whatsapp.com)
│
▼
┌───────────────────────────┐
│ Routing Path │ ──► Maps to: /v1/chat-messaging/groups/admins
└───────────────────────────┘
│
├─► [ HTTP Method ] ──► GET (Fetch data without payload)
│
├─► [ Query Param ] ──► ?group_id=123
│
▼
┌───────────────────────────┐
│ Group Domain Service │
└───────────────────────────┘
│
▼ [ Formats Output ]
┌───────────────────────────┐
│ JSON Response Object │ ──► Returns: { "admins": [...] }
└───────────────────────────┘

---

### 3. Industry Standards & Best Practices

- **Clarity in Naming:** The name of your API must strictly align with the exact action it performs. For example, if an endpoint is called `getAdmins`, it should only return an array of administrators—never unrelated structural group metadata.
- **Lean Parameters:** Do not accept extra parameters unless they are strictly required to fulfill the business goal. The only exception is advanced edge-case performance optimizations to prevent expensive cross-microservice network I/O queries.
- **Extensibility Traps:** Avoid packing speculative, unused properties into your response object in the hope of making it future-proof. This results in unnecessary network overhead and introduces architectural confusion.
- **Balanced Error Handlers:** Avoid both extremes—do not write a generic error code for everything, but don't write custom handlers for implicit client structural bugs (e.g., verifying if a string parameter should be an integer). Focus on expected business errors like a deleted resource (`404 Not Found`).

---

### 4. Advanced API Design Challenges

#### Dealing with Side Effects & Atomicity

Endpoints must be predictable and completely free of hidden side effects. For example, designing a `setAdmins` endpoint that implicitly creates a missing group or adds missing users as members creates a cluttered interface that is highly fragile to test.

[ Problematic Multi-Action Design ]Client Call: setAdmins(GroupID, UserList) ──► (Performs: Create Group? + Add Members? + Make Admins?)

                                    VS.

[ Decoupled Atomic Flow ]Client Call 1: createGroup(MemberList) ──► Returns New GroupIDClient Call 2: setAdmins(GroupID, AdminList)

If an action requires strict transactional consistency (atomicity), explicitly break the processes into decoupled, atomic API operations. If a group doesn't exist, throw an explicit error (`404`) and force the client application to run a distinct `createGroup` sequence first.

#### Managing Large Payloads (Pagination vs. Fragmentation)

When an endpoint evaluates queries that yield immense datasets (e.g., returning thousands of active group members), returning everything in a single payload will block your system.

- **Pagination:** Passes data ownership to the client by utilizing an `offset` or a `limit` parameter, chunking rows into manageable sequences (e.g., fetching 10 members at a time).
- **Fragmentation:** Utilizes underlying network-level protocol architectures (like TCP sequencing) to split a massive payload exceeding memory parameters into multiple numbered data packet frames, concluded by a clear terminating packet string indicator.

#### Consistency vs. Performance (Service Degradation)

- **Eventual Consistency:** In high-throughput infrastructures, pulling text logs or comments straight from an active cache provides massive performance boosts at the cost of slight data staleness (e.g., video likes updating once a minute).
- **Service Degradation:** When underlying data storage nodes face high load, APIs degrade gracefully by stripping secondary visual metadata (like profile photos or status labels) and returning only essential identifiers (like a string name) to ensure the system doesn't crash.
