# What's an Event-Driven System?

### 1. Core Paradigm: Request-Response vs. Event-Driven

- **Request-Response:** Clients send an explicit command or "pull" interaction through an API gateway directly to internal services, stringing requests across backend nodes.
- **Event-Driven:** Internal services never communicate directly. Instead, a service broadcasts that a state transition has occurred by dropping an "event notification" onto a centralized **Event Bus**. Interested nodes independently observe and handle that event.

---

### 2. Event-Driven Architecture Components Diagram

```
[ Client Request ]
│
▼
┌───────────────┐
│  Gateway API  │
└───────────────┘
│
▼
┌───────────────┐
│ Core Service  │
└───────────────┘
│
│  Publish State Change Event
▼
================================== [ EVENT BUS / TOPIC STREAM ] ==================================
│                                      │                                      │
▼ (Consume Event)                      ▼ (Consume Event)                      ▼ (Consume Event)
┌───────────────┐                      ┌───────────────┐                      ┌───────────────┐
│   Service A   │                      │   Service B   │                      │   Service C   │
└───────────────┘                      └───────────────┘                      └───────────────┘
│                                      │                                      │
▼                                      ▼                                      ▼
┌───────────────┐                      ┌───────────────┐                      ┌───────────────┐
│ Local DB Log  │                      │ Local DB Log  │                      │ Outbound Alert│
└───────────────┘                      └───────────────┘                      └───────────────┘
(Isolate Event)                        (Isolate Event)                        (Trigger Email)
```

---

### 3. Key Behavioral Advantages

- **High System Availability:** Rather than pulling shared data dynamically from a centralized microservice, downstream instances consume notifications from the event bus and persist them in custom, isolated **Local Databases**. If one microservice fails, other systems keep running because they read independent local stores.
- **Easy Service Replacements:** If an engineering team wants to substitute a legacy module with a brand-new component, it doesn't need complex data migration routines. They simply spin up the new target node, direct it to stream all recorded historic entries from time zero up to the present timestamp ($T$), and replay the logs until it achieves full state parity with the platform.
- **Transaction Semantics & Intent Mapping:** Offers flexible transactional safety:
  - _At-Most-Once Delivery:_ Good for non-critical alerts (like onboarding emails).
  - _At-Least-Once Delivery:_ Guaranteed arrival through automated retry sequences (essential for invoice workflows).
  - _Intent Logging:_ Logs store _why_ a state shift was made, preserving domain logic for auditing and analytics.

---

### 4. Deterministic Bug Isolation & Time-Travel Debugging

Because changes are saved to an append-only event log inside the database instead of instantly destroying and rewriting flat properties, systems acquire an intrinsic "Time-Travel" capability.

```
[ REPLAY HISTORIC EVENT STREAM LOOP ]

Time Zero [0] ────► [ Event 1 ] ────► [ Event 2 ] ────► [ Event 3 ] ────► Timestamp [T] (Bug Found)
│
▼
[ Step-by-Step Isolation ]
```
