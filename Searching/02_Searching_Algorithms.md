# Searching Algorithms

> **Source:** Handwritten DSA Notes – Pages 3–9

---

## 1. Overview

Searching is the process of finding a target element within a data structure. The choice of algorithm depends on whether the data is **sorted** and the required **time complexity**.

| Algorithm | Data Requirement | Time Complexity | Space Complexity |
|-----------|-----------------|-----------------|------------------|
| Linear Search | None | O(n) | O(1) |
| Binary Search | Sorted | O(log n) | O(1) iterative / O(log n) recursive |
| 2D Binary Search | Row-Col sorted matrix | O(m + n) | O(1) |

---

## 2. Linear Search

### Concept

Linear Search checks **every element** in the array sequentially until the target is found or the end is reached. It is the **simplest** searching algorithm.

### Algorithm

```
LinearSearch(arr, target):
    for i = 0 to arr.length - 1:
        if arr[i] == target:
            return i
    return -1  // not found
```

### Java Implementation

```java
public static int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;   // Return index of target
        }
    }
    return -1;  // Target not found
}
```

### Complexity Analysis

| Case | Scenario | Comparisons | Complexity |
|------|----------|-------------|------------|
| **Best** | Target at index 0 | 1 | **O(1)** |
| **Worst** | Target at last index or absent | n | **O(n)** |
| **Average** | Target at random position | n/2 | **O(n)** |

### When to Use

- Array is **unsorted** or **small** (n < 100).
- Searching in a **linked list** (no random access).
- One-time search (not worth sorting first).

---

## 3. Binary Search

### Concept

Binary Search works on **sorted arrays** by repeatedly dividing the search interval in half. At each step, it compares the target with the **middle element** and eliminates half the remaining elements.

### Prerequisites

> ⚠️ The array **MUST be sorted** (ascending or descending) for binary search to work.

### Algorithm

```
BinarySearch(arr, target):
    start = 0
    end = arr.length - 1
    
    while start <= end:
        mid = start + (end - start) / 2    // Avoids integer overflow
        
        if arr[mid] == target:
            return mid
        else if arr[mid] < target:
            start = mid + 1    // Target is in right half
        else:
            end = mid - 1      // Target is in left half
    
    return -1  // Not found
```

### Why `start + (end - start) / 2` instead of `(start + end) / 2`?

To prevent **integer overflow**. If `start` and `end` are both large values close to `Integer.MAX_VALUE`, their sum could exceed the int range.

### Java Implementation (Iterative)

```java
public static int binarySearch(int[] arr, int target) {
    int start = 0;
    int end = arr.length - 1;
    
    while (start <= end) {
        int mid = start + (end - start) / 2;
        
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            start = mid + 1;
        } else {
            end = mid - 1;
        }
    }
    return -1;
}
```

### Java Implementation (Recursive)

```java
public static int binarySearch(int[] arr, int target, int start, int end) {
    if (start > end) return -1;
    
    int mid = start + (end - start) / 2;
    
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) return binarySearch(arr, target, mid + 1, end);
    else return binarySearch(arr, target, start, mid - 1);
}
```

### Complexity Derivation

At each step, the array size is halved:

```
Step 1: N elements
Step 2: N/2 elements
Step 3: N/4 elements
...
Step k: N/2^k elements
```

The search ends when only 1 element remains:

$$\frac{N}{2^k} = 1 \implies 2^k = N \implies k = \log_2 N$$

> **Time Complexity: O(log N)**

### Visual Walkthrough

```
Array: [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
Target: 23

Step 1: start=0, end=9, mid=4 → arr[4]=16 < 23 → start=5
Step 2: start=5, end=9, mid=7 → arr[7]=56 > 23 → end=6
Step 3: start=5, end=6, mid=5 → arr[5]=23 ✓ → FOUND at index 5
```

### Variations

| Variation | Description |
|-----------|-------------|
| **Find first occurrence** | When target found, continue searching left (`end = mid - 1`) |
| **Find last occurrence** | When target found, continue searching right (`start = mid + 1`) |
| **Floor value** | Largest element ≤ target |
| **Ceiling value** | Smallest element ≥ target |
| **Descending order** | Flip the comparison logic |
| **Rotated sorted array** | Modified binary search checking which half is sorted |
| **Peak element** | Compare mid with neighbors |
| **Infinite sorted array** | Expand bounds exponentially, then binary search |

---

## 4. Binary Search on 2D Sorted Matrix

### Problem

Given a matrix where:
- Each row is sorted left to right
- Each column is sorted top to bottom

Find the position of a target value.

### Strategy: Staircase Search

Start from the **top-right corner** (or bottom-left corner) and use a pruning approach:

- If `current > target` → move **left** (eliminate column)
- If `current < target` → move **down** (eliminate row)
- If `current == target` → **found**

### Algorithm

```
Search2DMatrix(matrix, target):
    row = 0
    col = matrix[0].length - 1    // Start at top-right
    
    while row < matrix.length AND col >= 0:
        if matrix[row][col] == target:
            return (row, col)
        else if matrix[row][col] > target:
            col--       // Move left
        else:
            row++       // Move down
    
    return NOT_FOUND
```

### Java Implementation

```java
public static int[] search2DMatrix(int[][] matrix, int target) {
    int row = 0;
    int col = matrix[0].length - 1;
    
    while (row < matrix.length && col >= 0) {
        if (matrix[row][col] == target) {
            return new int[]{row, col};
        } else if (matrix[row][col] > target) {
            col--;
        } else {
            row++;
        }
    }
    return new int[]{-1, -1}; // Not found
}
```

### Visual Walkthrough

```
Matrix:         Target: 14
[1,   4,  7,  11]
[2,   5,  8,  12]
[3,   6,  9,  16]
[10, 13, 14,  17]

Start at (0, 3) → 11 < 14 → row++
      at (1, 3) → 12 < 14 → row++
      at (2, 3) → 16 > 14 → col--
      at (2, 2) →  9 < 14 → row++
      at (3, 2) → 14 ✓ FOUND at (3, 2)
```

### Complexity

| Aspect | Value |
|--------|-------|
| **Time** | O(m + n) where m = rows, n = columns |
| **Space** | O(1) |

> At most `m + n - 1` steps because each step either eliminates a row or a column.

---

## 5. Comparison Summary

```
┌──────────────────────────────────────────────────┐
│           Searching Algorithm Comparison          │
├──────────────┬──────────┬────────────┬───────────┤
│ Algorithm    │ Best     │ Average    │ Worst     │
├──────────────┼──────────┼────────────┼───────────┤
│ Linear       │ O(1)     │ O(n)       │ O(n)      │
│ Binary       │ O(1)     │ O(log n)   │ O(log n)  │
│ 2D Staircase │ O(1)     │ O(m+n)     │ O(m+n)    │
└──────────────┴──────────┴────────────┴───────────┘
```

---

## 6. Key Takeaways

1. **Linear Search** is the go-to for unsorted/small data — simple but slow for large datasets.
2. **Binary Search** is extremely efficient (O(log N)) but **requires sorted data**.
3. Always use `start + (end - start) / 2` to compute mid — prevents overflow.
4. The **staircase approach** for 2D matrices is elegant and runs in O(m + n).
5. Binary Search has many variations — mastering the template enables solving dozens of problems.
