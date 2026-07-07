# Horizontal vs. Vertical Scaling

## Executive Summary

System design centers on **scalability**—the ability to handle increasing requests. This is achieved by either **Vertical Scaling** (upgrading a single machine) or **Horizontal Scaling** (adding more machines). Real-world systems typically use a hybrid approach, leveraging the strengths of both.

## Key Comparison

| Metric               | Vertical Scaling (Scale Up)   | Horizontal Scaling (Scale Out)  |
| :------------------- | :---------------------------- | :------------------------------ |
| **Load Balancing**   | Not required                  | Essential (distributes traffic) |
| **Resilience**       | Low (Single Point of Failure) | High (Fault tolerant)           |
| **Communication**    | Fast (Inter-process)          | Slow (Network/RPC calls)        |
| **Data Consistency** | Easy (Single data source)     | Challenging (Distributed data)  |
| **Hardware Limits**  | Hard limit (Maximum size)     | Linear (Scales with more boxes) |

## Detailed Notes

### 1. Scaling Definitions

- **Vertical Scaling:** Increasing the power of a single machine (CPU, RAM).
- **Horizontal Scaling:** Increasing the number of machines in the resource pool.

### 2. Implementation Strategy

- **Initial Growth:** Vertical scaling is usually preferred at the start because it keeps architecture simple, fast, and consistent.
- **Late-Stage Growth:** Once hardware limitations are reached or high availability is required, the system must shift to horizontal scaling.
- **The Hybrid Approach:** Most production systems use a hybrid model:
  - They utilize **vertical scaling** to keep individual nodes powerful and fast.
  - They utilize **horizontal scaling** to add resilience, manage massive load, and eliminate single points of failure.

### 3. Core Trade-offs

- **Consistency vs. Availability:** Scaling out horizontally introduces the CAP theorem trade-off—it becomes harder to maintain strict data consistency across multiple distributed machines compared to a single machine.
- **Performance:** Vertical scaling is inherently faster for processes involving heavy data interaction because it avoids the latency of network calls between different servers.
