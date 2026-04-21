# Heap Data Structure

> **Source:** Handwritten DSA Notes – Pages 168–175

---

## 1. What is a Heap?

A Heap is a **complete binary tree** that satisfies the **heap property**:

- **Max-Heap:** Every parent node is **≥** its children (root = maximum).
- **Min-Heap:** Every parent node is **≤** its children (root = minimum).

```
Max-Heap:              Min-Heap:
      50                     10
     /  \                   /  \
   30    40               20    30
  / \   /                / \   /
 10 20 35              40  50 35
```

### Properties

| Property | Value |
|----------|-------|
| Structure | Complete Binary Tree (CBT) |
| Root | Max element (Max-Heap) or Min element (Min-Heap) |
| Height | O(log n) |
| Array-based | Yes — no pointers needed |

---

## 2. Array Representation

A heap is stored in an **array** with level-order mapping. For a node at index `i` (0-indexed):

| Relationship | Formula (0-indexed) | Formula (1-indexed) |
|-------------|---------------------|---------------------|
| **Parent** | `(i - 1) / 2` | `i / 2` |
| **Left child** | `2 * i + 1` | `2 * i` |
| **Right child** | `2 * i + 2` | `2 * i + 1` |

### Example (Max-Heap)

```
Tree:           Array:
      50        [50, 30, 40, 10, 20, 35]
     /  \        0   1   2   3   4   5
   30    40
  / \   /
 10 20 35

Index 0: 50 (root)
Index 1: 30 → parent = (1-1)/2 = 0 (50) ✓
Index 2: 40 → parent = (2-1)/2 = 0 (50) ✓
Index 3: 10 → parent = (3-1)/2 = 1 (30) ✓
Index 4: 20 → parent = (4-1)/2 = 1 (30) ✓
Index 5: 35 → parent = (5-1)/2 = 2 (40) ✓
```

---

## 3. Heap Operations

### 3.1 Insertion (Up-Heap / Swim)

1. Add the new element at the **end** of the array (next available position in CBT).
2. **Swim up:** Compare with parent, swap if violating heap property.
3. Repeat until heap property is restored or root is reached.

```java
public void insert(int val) {
    arr[size] = val;
    int i = size;
    size++;
    
    // Swim up
    while (i > 0 && arr[i] > arr[(i - 1) / 2]) {  // Max-Heap
        swap(arr, i, (i - 1) / 2);
        i = (i - 1) / 2;
    }
}
```

### Visual Walkthrough (Insert 55 into Max-Heap)

```
Before:     50              After insert:  50            After swim:    55
           /  \                           /  \                         /  \
         30    40                       30    40                     50    40
        / \   /                        / \   / \                    / \   /
      10 20 35                       10 20 35  55                 10 20 35 30
                                               ↑ swim up
```

**Time Complexity:** O(log n) — at most height of tree swaps.

### 3.2 Removal / Extract Max (Down-Heap / Sink)

1. Remove the **root** element (max in max-heap).
2. Replace root with the **last element** in the array.
3. **Sink down:** Compare with children, swap with the larger child (max-heap).
4. Repeat until heap property is restored or leaf is reached.

```java
public int extractMax() {
    int max = arr[0];
    arr[0] = arr[size - 1];
    size--;
    
    // Sink down
    heapifyDown(0);
    return max;
}

private void heapifyDown(int i) {
    while (true) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        
        if (left < size && arr[left] > arr[largest])
            largest = left;
        if (right < size && arr[right] > arr[largest])
            largest = right;
        
        if (largest == i) break;
        
        swap(arr, i, largest);
        i = largest;
    }
}
```

### Visual Walkthrough (Extract Max from Max-Heap)

```
Step 1: Remove root (50), move last (20) to root
      20                  
     /  \                 
   30    40               
  / \                     
 10  35                   

Step 2: Sink down — 20 < 40 (right child is larger)
      40
     /  \
   30    20
  / \
 10  35

Step 3: Sink down — 20 < 35 (right child is larger)
      40
     /  \
   30    35
  / \
 10  20
Done! ✓
```

**Time Complexity:** O(log n)

---

## 4. Heapify (Build Heap from Array)

### Concept

Convert an unordered array into a heap **in-place** in O(n) time. Start from the **last non-leaf node** and sink down each node.

### Algorithm

```java
public static void buildHeap(int[] arr, int n) {
    // Last non-leaf node index = (n/2) - 1
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapifyDown(arr, n, i);
    }
}

private static void heapifyDown(int[] arr, int n, int i) {
    int largest = i;
    int left = 2 * i + 1;
    int right = 2 * i + 2;
    
    if (left < n && arr[left] > arr[largest])
        largest = left;
    if (right < n && arr[right] > arr[largest])
        largest = right;
    
    if (largest != i) {
        int temp = arr[i];
        arr[i] = arr[largest];
        arr[largest] = temp;
        heapifyDown(arr, n, largest);
    }
}
```

### Visual Walkthrough

```
Array: [4, 10, 3, 5, 1]

Tree (unordered):
      4
     / \
   10    3
  / \
 5   1

Start from last non-leaf (index 1 → value 10):
  10 > 5, 10 > 1 → already valid

Index 0 (value 4):
  4 < 10 → swap → 10, 4, 3, 5, 1
  4 < 5  → swap → 10, 5, 3, 4, 1

Result (Max-Heap):
      10
     /  \
    5     3
   / \
  4   1  ✓
```

### Why O(n) instead of O(n log n)?

Intuitively, leaf nodes (half the tree) need 0 swaps, nodes one level above need at most 1 swap, etc. The sum converges to O(n).

---

## 5. Heap Sort

### Algorithm

1. **Build a Max-Heap** from the array — O(n).
2. Repeatedly **extract the max** (swap root with last unsorted element) and reduce heap size — O(n log n).

```java
public static void heapSort(int[] arr) {
    int n = arr.length;
    
    // Step 1: Build Max-Heap
    for (int i = n / 2 - 1; i >= 0; i--)
        heapifyDown(arr, n, i);
    
    // Step 2: Extract elements one by one
    for (int i = n - 1; i > 0; i--) {
        // Move current root (max) to end
        int temp = arr[0];
        arr[0] = arr[i];
        arr[i] = temp;
        
        // Heapify the reduced heap
        heapifyDown(arr, i, 0);
    }
}
```

### Visual Walkthrough

```
Array: [4, 10, 3, 5, 1]

Step 1: Build Max-Heap → [10, 5, 3, 4, 1]

Step 2: Swap and heapify:
  Swap 10↔1: [1, 5, 3, 4, |10|]  → heapify → [5, 4, 3, 1, |10|]
  Swap 5↔1:  [1, 4, 3, |5, 10|]  → heapify → [4, 1, 3, |5, 10|]
  Swap 4↔3:  [3, 1, |4, 5, 10|]  → heapify → [3, 1, |4, 5, 10|]
  Swap 3↔1:  [1, |3, 4, 5, 10|]  → done

Result: [1, 3, 4, 5, 10] ✓
```

### Complexity

| Aspect | Value |
|--------|-------|
| **Time (all cases)** | O(n log n) |
| **Space** | O(1) — in-place |
| **Stable** | ❌ No |

---

## 6. Priority Queue (Heap-based)

Java's `PriorityQueue` uses a **Min-Heap** internally.

```java
// Min-Heap (default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(30);
minHeap.offer(10);
minHeap.offer(20);
minHeap.poll();  // Returns 10 (smallest)

// Max-Heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
maxHeap.offer(30);
maxHeap.offer(10);
maxHeap.offer(20);
maxHeap.poll();  // Returns 30 (largest)
```

| Operation | Time |
|-----------|------|
| `offer()` (insert) | O(log n) |
| `poll()` (remove min/max) | O(log n) |
| `peek()` (view min/max) | O(1) |

### Common Applications

| Application | Heap Type |
|-------------|-----------|
| **Dijkstra's shortest path** | Min-Heap |
| **Top K elements** | Min-Heap (size K) |
| **Kth largest element** | Min-Heap (size K) |
| **Merge K sorted lists** | Min-Heap |
| **Task scheduling** | Max/Min-Heap |
| **Median of stream** | Both (Max + Min Heap) |

---

## 7. Heap Operations Summary

| Operation | Time Complexity |
|-----------|----------------|
| Insert | O(log n) |
| Extract Min/Max | O(log n) |
| Peek Min/Max | O(1) |
| Build Heap (Heapify) | O(n) |
| Heap Sort | O(n log n) |
| Delete arbitrary | O(n) to find + O(log n) to fix |

---

## 8. Key Takeaways

1. A Heap is a **complete binary tree** stored in an **array** — no pointers needed.
2. **Parent at `(i-1)/2`**, left child at `2i+1`, right child at `2i+2` (0-indexed).
3. **Insertion** swims up; **Extraction** sinks down — both O(log n).
4. **Heapify** builds a heap from an unordered array in **O(n)** — not O(n log n).
5. **Heap Sort** is O(n log n) in ALL cases and is in-place — but not stable.
6. Java's `PriorityQueue` = Min-Heap; use `Collections.reverseOrder()` for Max-Heap.
7. Heaps are the backbone of **Priority Queues**, which are essential for Dijkstra's, top-K, and scheduling problems.
