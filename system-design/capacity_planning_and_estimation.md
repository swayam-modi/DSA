# Capacity Planning and Estimation: YouTube Storage Case Study

## Executive Summary

Estimation and capacity planning questions evaluate how an engineer breaks down vast, ambiguous system constraints into actionable metrics. Using YouTube as a case study, this breakdown models how to estimate daily video storage, cache infrastructure, and processor counts. The core philosophy is that being off by a factor of 10 is fine, but the underlying logic must be structured, consistent, and grounded in realistic hardware constraints.

## 1. Daily Video Storage Breakdown

### Core Assumptions

- **User Base:** 1 Billion total users.
- **Daily Uploaders:** 1 in 1,000 users upload content daily (**1 Million uploaders**).
- **Video Duration:** Average video length of 10 minutes.
  - _Total minutes uploaded per day:_ 1M uploaders $\times$ 10 minutes = **$10^7$ minutes of video/day**.

### File Size Optimization

- **Baseline:** A standard 2-hour high-quality movie is roughly 4 GB.
- **YouTube Optimization:** Due to heavy codec compression, space is reduced by 90%, making a 2-hour optimized video **400 MB**.
- **Per Unit Size:** This breaks down to 200 MB per hour, or roughly **3 MB per minute**.

### Total Daily Storage Calculation

1.  **Raw Data:** $10^7 \text{ minutes} \times 3 \text{ MB/minute} = \mathbf{30 \text{ TB / day}}$
2.  **Redundancy Multiplier:** Storing 3 full copies for fault tolerance and low regional latency: $30 \text{ TB} \times 3 = \mathbf{90 \text{ TB}}$
3.  **Transcoding Multiplier:** Storing videos in multiple resolutions (144p, 360p, 720p, etc.) roughly doubles the storage overhead: $90 \text{ TB} \times 2 = \mathbf{180 \text{ TB}}$

> **Final Daily Storage Estimate:** **~180 TB** (or roughly 0.2 Petabytes) per day.

---

## 2. Cache Requirements (RAM)

To optimize recommendations and home feeds, video metadata (specifically **thumbnails** and titles) must be cached.

- **Thumbnail Size:** Highly compressed scaled-down UI images are roughly **10 KB**.
- **Caching Strategy:** Cache all videos uploaded within the **last 90 days** (plus active evergreen content).
- **Total Cached Assets:** 90 days $\times$ 1 Million videos/day = 90 Million videos.
- **Total RAM Needed:** $90\text{M videos} \times 10 \text{ KB} \approx \mathbf{1 \text{ TB of RAM}}$.

### Sharding & Cluster Architecture

Because a single computer cannot efficiently support 1 TB of RAM, the system must be distributed horizontally using standard **16 GB RAM nodes**.

- **Raw Cluster Size:** $1000 \text{ GB} / 16 \text{ GB} \approx 64 \text{ nodes}$
- **Global Replication:** Multiplied by 3 for distinct geographic data centers = 192 nodes.
- **Operational Headroom:** Doubled to maintain a safe **50% peak capacity** to handle sudden node failures.

> **Final Cache Nodes Required:** **~500 nodes** total.

---

## 3. Processing Node Requirements

To ingest, transcode, and save all uploaded content within a standard 24-hour cycle, the system must run continuously.

- **Data Throughput:** Converting $10^7$ minutes of video daily means handling **400 MB of data per second** globally.
- **Time Overhead Per 1 MB of Video:**
  - _Read from Disk:_ ~10 ms
  - _Processing/Transcoding (CPU Bound):_ ~20 ms
  - _Write to Disk (Includes locking/indexing):_ ~20 ms
  - _Total Time:_ **50 ms per MB**

### Processor Count Calculation

$$\text{Work required per second} = 400 \text{ MB/sec} \times 50 \text{ ms/MB} = 20,000 \text{ ms of work per physical second}$$

Since one processor core provides 1,000 ms of work per physical second:
$$\text{Processors required} = \frac{20,000 \text{ ms}}{1,000 \text{ ms/core}} = 20 \text{ parallel cores}$$

> **Final Processing Estimate:** **~20 parallel processing nodes** running concurrently.

---

## Cheat Sheet: "Numbers Everyone Should Know"

When completing system design estimations, rely on these industry-standard latency approximations to ground your answers:

- **L1 Cache Reference:** 0.5 ns
- **Branch Mispredict:** 5 ns
- **L2 Cache Reference:** 7 ns
- **Mutex Lock/Unlock:** 100 ns
- **Main Memory Reference (RAM):** 100 ns
- **Read 1 MB sequentially from memory:** 250,000 ns (0.25 ms)
- **Round trip within same datacenter:** 500,000 ns (0.5 ms)
- **Disk Seek (HDD):** 10,000,000 ns (10 ms)
- **Read 1 MB sequentially from disk:** 30,000,000 ns (30 ms)
- **Send packet CA -> Netherlands -> CA:** 150,000,000 ns (150 ms)
