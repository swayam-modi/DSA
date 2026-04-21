# Sorting Algorithms

> **Source:** Handwritten DSA Notes – Pages 10–15, 92, 94–96

---

## 1. Overview

Sorting arranges elements in a specific order (ascending/descending). Understanding sorting is fundamental to DSA — many algorithms and data structures rely on sorted data.

| Algorithm | Best | Average | Worst | Space | Stable? |
|-----------|------|---------|-------|-------|---------|
| Bubble Sort | O(n) | O(n²) | O(n²) | O(1) | ✅ Yes |
| Selection Sort | O(n²) | O(n²) | O(n²) | O(1) | ❌ No |
| Insertion Sort | O(n) | O(n²) | O(n²) | O(1) | ✅ Yes |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ Yes |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ No |
| Cyclic Sort | O(n) | O(n) | O(n) | O(1) | ❌ No |

> **Stable Sort:** A sort is stable if it preserves the relative order of elements with equal keys.

---

## 2. Bubble Sort

### Concept

Bubble Sort repeatedly steps through the array, compares adjacent elements, and **swaps** them if they are in the wrong order. The largest unsorted element "bubbles up" to its correct position after each pass.

### Algorithm

```
BubbleSort(arr):
    for i = 0 to n-1:
        swapped = false
        for j = 0 to n-i-2:
            if arr[j] > arr[j+1]:
                swap(arr[j], arr[j+1])
                swapped = true
        if not swapped:
            break   // Array is already sorted — optimization
```

### Java Implementation

```java
public static void bubbleSort(int[] arr) {
    int n = arr.length;
    for (int i = 0; i < n - 1; i++) {
        boolean swapped = false;
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                // Swap
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
                swapped = true;
            }
        }
        if (!swapped) break; // Early termination
    }
}
```

### Visual Walkthrough

```
Array: [5, 3, 8, 4, 2]

Pass 1: [3, 5, 4, 2, |8|]     ← 8 bubbles to end
Pass 2: [3, 4, 2, |5|, 8]     ← 5 in place
Pass 3: [3, 2, |4|, 5, 8]     ← 4 in place
Pass 4: [2, |3|, 4, 5, 8]     ← 3 in place
Result: [2, 3, 4, 5, 8] ✓
```

### Complexity Analysis

| Case | Condition | Comparisons | Complexity |
|------|-----------|-------------|------------|
| **Best** | Already sorted (with optimization) | n - 1 | **O(n)** |
| **Worst** | Reverse sorted | n(n-1)/2 | **O(n²)** |
| **Average** | Random order | n(n-1)/4 | **O(n²)** |

### Key Observations

- After `i` passes, the last `i` elements are guaranteed sorted.
- The `swapped` flag provides **early termination** if no swaps occur in a pass (already sorted).
- **In-place** algorithm — only O(1) extra space needed.

---

## 3. Selection Sort

### Concept

Selection Sort divides the array into a **sorted** and **unsorted** region. In each iteration, it finds the **minimum** (or maximum) element from the unsorted region and places it at the beginning of the unsorted region.

### Algorithm

```
SelectionSort(arr):
    for i = 0 to n-1:
        minIndex = i
        for j = i+1 to n-1:
            if arr[j] < arr[minIndex]:
                minIndex = j
        swap(arr[i], arr[minIndex])
```

### Java Implementation

```java
public static void selectionSort(int[] arr) {
    int n = arr.length;
    for (int i = 0; i < n - 1; i++) {
        int minIndex = i;
        for (int j = i + 1; j < n; j++) {
            if (arr[j] < arr[minIndex]) {
                minIndex = j;
            }
        }
        // Swap the found minimum with the first unsorted element
        int temp = arr[i];
        arr[i] = arr[minIndex];
        arr[minIndex] = temp;
    }
}
```

### Visual Walkthrough

```
Array: [29, 10, 14, 37, 13]

Pass 1: Find min in [29, 10, 14, 37, 13] → 10 at index 1
        Swap 29 ↔ 10 → [|10|, 29, 14, 37, 13]

Pass 2: Find min in [29, 14, 37, 13] → 13 at index 4
        Swap 29 ↔ 13 → [10, |13|, 14, 37, 29]

Pass 3: Find min in [14, 37, 29] → 14 at index 2
        No swap needed → [10, 13, |14|, 37, 29]

Pass 4: Find min in [37, 29] → 29 at index 4
        Swap 37 ↔ 29 → [10, 13, 14, |29|, |37|]

Result: [10, 13, 14, 29, 37] ✓
```

### Complexity Analysis

| Case | Complexity |
|------|------------|
| **Best** | O(n²) |
| **Worst** | O(n²) |
| **Average** | O(n²) |

> **Note:** Selection Sort **always** performs O(n²) comparisons regardless of input. However, it performs at most O(n) swaps, making it useful when **write operations are expensive**.

### Key Observations

- **Not stable** — relative order of equal elements may change.
- Performs **minimum number of swaps** (at most n-1).
- Always O(n²) — no best-case optimization like Bubble Sort.

---

## 4. Insertion Sort

### Concept

Insertion Sort builds the sorted array **one element at a time** by picking each element and inserting it into its correct position within the already sorted portion. Think of it like sorting playing cards in your hand.

### Algorithm

```
InsertionSort(arr):
    for i = 1 to n-1:
        key = arr[i]
        j = i - 1
        while j >= 0 AND arr[j] > key:
            arr[j + 1] = arr[j]    // Shift right
            j--
        arr[j + 1] = key            // Insert in correct position
```

### Java Implementation

```java
public static void insertionSort(int[] arr) {
    int n = arr.length;
    for (int i = 1; i < n; i++) {
        int key = arr[i];
        int j = i - 1;
        
        // Shift elements greater than key to the right
        while (j >= 0 && arr[j] > key) {
            arr[j + 1] = arr[j];
            j--;
        }
        arr[j + 1] = key;
    }
}
```

### Visual Walkthrough

```
Array: [12, 11, 13, 5, 6]

Step 1: key=11, sorted=[12] → shift 12 → [|11, 12|, 13, 5, 6]
Step 2: key=13, sorted=[11,12] → no shift → [11, 12, |13|, 5, 6]
Step 3: key=5,  sorted=[11,12,13] → shift all → [|5, 11, 12, 13|, 6]
Step 4: key=6,  sorted=[5,11,12,13] → shift 11,12,13 → [5, |6, 11, 12, 13|]

Result: [5, 6, 11, 12, 13] ✓
```

### Complexity Analysis

| Case | Condition | Complexity |
|------|-----------|------------|
| **Best** | Already sorted | **O(n)** — only comparisons, no shifts |
| **Worst** | Reverse sorted | **O(n²)** — maximum shifts |
| **Average** | Random order | **O(n²)** |

### Key Observations

- **Stable** sort — preserves relative order of equal elements.
- **Adaptive** — runs in O(n) time on nearly sorted data.
- **Online** — can sort as elements are received (streaming data).
- Best for **small datasets** or **nearly sorted arrays**.

---

## 5. Merge Sort

### Concept

Merge Sort is a **Divide and Conquer** algorithm:

1. **Divide:** Split the array into two halves.
2. **Conquer:** Recursively sort each half.
3. **Merge:** Combine the two sorted halves into a single sorted array.

### Algorithm

```
MergeSort(arr, start, end):
    if start >= end:
        return
    
    mid = start + (end - start) / 2
    MergeSort(arr, start, mid)        // Sort left half
    MergeSort(arr, mid + 1, end)      // Sort right half
    Merge(arr, start, mid, end)       // Merge sorted halves

Merge(arr, start, mid, end):
    Create temp arrays L[] and R[]
    Copy arr[start..mid] to L[]
    Copy arr[mid+1..end] to R[]
    
    i = 0, j = 0, k = start
    while i < L.length AND j < R.length:
        if L[i] <= R[j]:
            arr[k++] = L[i++]
        else:
            arr[k++] = R[j++]
    
    Copy remaining elements of L[] (if any)
    Copy remaining elements of R[] (if any)
```

### Java Implementation

```java
public static void mergeSort(int[] arr, int start, int end) {
    if (start >= end) return;
    
    int mid = start + (end - start) / 2;
    mergeSort(arr, start, mid);
    mergeSort(arr, mid + 1, end);
    merge(arr, start, mid, end);
}

private static void merge(int[] arr, int start, int mid, int end) {
    int[] temp = new int[end - start + 1];
    int i = start, j = mid + 1, k = 0;
    
    while (i <= mid && j <= end) {
        if (arr[i] <= arr[j]) {
            temp[k++] = arr[i++];
        } else {
            temp[k++] = arr[j++];
        }
    }
    
    while (i <= mid) temp[k++] = arr[i++];
    while (j <= end) temp[k++] = arr[j++];
    
    System.arraycopy(temp, 0, arr, start, temp.length);
}
```

### Visual Walkthrough

```
[38, 27, 43, 3, 9, 82, 10]
          ↙            ↘
  [38, 27, 43, 3]    [9, 82, 10]
    ↙        ↘         ↙      ↘
[38, 27]  [43, 3]   [9, 82]  [10]
 ↙  ↘     ↙  ↘      ↙  ↘
[38][27] [43][3]   [9][82]  [10]
  ↘ ↙     ↘ ↙      ↘ ↙
[27, 38] [3, 43]  [9, 82]  [10]
    ↘    ↙          ↘    ↙
[3, 27, 38, 43]  [9, 10, 82]
        ↘          ↙
 [3, 9, 10, 27, 38, 43, 82]  ✓
```

### Complexity Analysis

| Case | Complexity |
|------|------------|
| **Best** | O(n log n) |
| **Worst** | O(n log n) |
| **Average** | O(n log n) |
| **Space** | **O(n)** — auxiliary space for merging |

### Recurrence Relation

$$T(n) = 2T\left(\frac{n}{2}\right) + O(n)$$

By Master Theorem: $a = 2$, $b = 2$, $f(n) = O(n)$, $\log_b a = 1$

Since $f(n) = \Theta(n^{\log_b a})$, we get $T(n) = \Theta(n \log n)$.

### Key Observations

- **Guaranteed O(n log n)** — no worst-case degradation.
- **Stable** sort.
- Uses **O(n) extra space** — not in-place.
- Preferred for **linked lists** (no random access needed, space overhead minimal).
- Foundation for **external sorting** (sorting data that doesn't fit in memory).

---

## 6. Quick Sort

### Concept

Quick Sort is a **Divide and Conquer** algorithm that works by selecting a **pivot** element and **partitioning** the array around it — elements smaller than pivot go left, larger go right. Then recursively sort the subarrays.

### Algorithm

```
QuickSort(arr, low, high):
    if low < high:
        pivotIndex = partition(arr, low, high)
        QuickSort(arr, low, pivotIndex - 1)    // Sort left of pivot
        QuickSort(arr, pivotIndex + 1, high)   // Sort right of pivot

Partition(arr, low, high):
    pivot = arr[high]    // Choose last element as pivot
    i = low - 1          // Index of smaller element
    
    for j = low to high - 1:
        if arr[j] <= pivot:
            i++
            swap(arr[i], arr[j])
    
    swap(arr[i + 1], arr[high])   // Place pivot in correct position
    return i + 1                   // Return pivot's final index
```

### Java Implementation

```java
public static void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        int pivotIndex = partition(arr, low, high);
        quickSort(arr, low, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, high);
    }
}

private static int partition(int[] arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1;
    
    for (int j = low; j < high; j++) {
        if (arr[j] <= pivot) {
            i++;
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
    
    // Place pivot in correct position
    int temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;
    
    return i + 1;
}
```

### Visual Walkthrough

```
Array: [10, 80, 30, 90, 40, 50, 70]
Pivot = 70 (last element)

Partition:
  j=0: 10 ≤ 70 → i=0, swap → [10, 80, 30, 90, 40, 50, 70]
  j=1: 80 > 70 → skip
  j=2: 30 ≤ 70 → i=1, swap → [10, 30, 80, 90, 40, 50, 70]
  j=3: 90 > 70 → skip
  j=4: 40 ≤ 70 → i=2, swap → [10, 30, 40, 90, 80, 50, 70]
  j=5: 50 ≤ 70 → i=3, swap → [10, 30, 40, 50, 80, 90, 70]

Place pivot at i+1=4: [10, 30, 40, 50, |70|, 90, 80]

Now recursively sort [10, 30, 40, 50] and [90, 80]
```

### Pivot Selection Strategies

| Strategy | Description | Risk |
|----------|-------------|------|
| Last element | Simple, use `arr[high]` | O(n²) on sorted arrays |
| First element | Use `arr[low]` | O(n²) on sorted arrays |
| Random | Pick random index | Best average performance |
| Median of three | Median of first, mid, last | Good balance |

### Complexity Analysis

| Case | Condition | Complexity |
|------|-----------|------------|
| **Best** | Pivot always splits evenly | **O(n log n)** |
| **Average** | Random splits | **O(n log n)** |
| **Worst** | Pivot always min or max (sorted data) | **O(n²)** |
| **Space** | Recursion stack | **O(log n)** average, O(n) worst |

### Recurrence

- **Best/Average:** $T(n) = 2T(n/2) + O(n) = O(n \log n)$
- **Worst:** $T(n) = T(n-1) + O(n) = O(n^2)$

### Quick Sort vs Merge Sort

| Feature | Quick Sort | Merge Sort |
|---------|-----------|------------|
| Average Time | O(n log n) | O(n log n) |
| Worst Time | O(n²) | O(n log n) |
| Space | O(log n) | O(n) |
| In-place | ✅ Yes | ❌ No |
| Stable | ❌ No | ✅ Yes |
| Cache | Excellent | Good |
| Practical speed | Usually faster | Consistent |

> **In practice, Quick Sort is often faster** than Merge Sort due to better cache locality and smaller constant factors, despite the worse worst case.

---

## 7. Cyclic Sort

> **Source:** Pages 19–22

Cyclic Sort is a specialized sorting algorithm for arrays containing numbers in a **continuous range** (e.g., `1 to N` or `0 to N-1`). It achieves **O(n)** time complexity by placing each element directly at its correct index.

### Core Idea

> If the array contains numbers from `1 to N`, then element `i` should be at index `i - 1`.

### Java Implementation

```java
public static void cyclicSort(int[] arr) {
    int i = 0;
    while (i < arr.length) {
        int correctIndex = arr[i] - 1;
        if (arr[i] != arr[correctIndex]) {
            int temp = arr[i];
            arr[i] = arr[correctIndex];
            arr[correctIndex] = temp;
        } else {
            i++;
        }
    }
}
```

### Visual Walkthrough

```
Array: [3, 5, 2, 1, 4]    (numbers 1-5)

i=0: arr[0]=3, correctIndex=2, swap → [2, 5, 3, 1, 4]
i=0: arr[0]=2, correctIndex=1, swap → [5, 2, 3, 1, 4]
i=0: arr[0]=5, correctIndex=4, swap → [4, 2, 3, 1, 5]
i=0: arr[0]=4, correctIndex=3, swap → [1, 2, 3, 4, 5]
i=0: arr[0]=1, correct position → i++
i=1 to 4: all correct → done

Result: [1, 2, 3, 4, 5] ✓
```

### Complexity

| Aspect | Value |
|--------|-------|
| **Time** | **O(n)** — each element swapped at most once |
| **Space** | **O(1)** — in-place |
| **Stable** | ❌ No |

### Applications (LeetCode Problems)

#### Find All Missing Numbers (LC 448)

```java
public static List<Integer> findDisappearedNumbers(int[] arr) {
    int i = 0;
    while (i < arr.length) {
        int correctIndex = arr[i] - 1;
        if (arr[i] != arr[correctIndex]) {
            int temp = arr[i];
            arr[i] = arr[correctIndex];
            arr[correctIndex] = temp;
        } else { i++; }
    }
    List<Integer> missing = new ArrayList<>();
    for (int j = 0; j < arr.length; j++) {
        if (arr[j] != j + 1) missing.add(j + 1);
    }
    return missing;
}
```

#### Find the Duplicate Number (LC 287)

```java
public static int findDuplicate(int[] arr) {
    int i = 0;
    while (i < arr.length) {
        int correctIndex = arr[i] - 1;
        if (arr[i] != arr[correctIndex]) {
            int temp = arr[i];
            arr[i] = arr[correctIndex];
            arr[correctIndex] = temp;
        } else {
            if (i != correctIndex) return arr[i];  // Duplicate!
            i++;
        }
    }
    return -1;
}
```

#### First Missing Positive (LC 41)

```java
public static int firstMissingPositive(int[] arr) {
    int i = 0;
    while (i < arr.length) {
        int correctIndex = arr[i] - 1;
        if (arr[i] > 0 && arr[i] <= arr.length && arr[i] != arr[correctIndex]) {
            int temp = arr[i];
            arr[i] = arr[correctIndex];
            arr[correctIndex] = temp;
        } else { i++; }
    }
    for (int j = 0; j < arr.length; j++) {
        if (arr[j] != j + 1) return j + 1;
    }
    return arr.length + 1;
}
```

### When to Use Cyclic Sort

| Use When... | Don't Use When... |
|------------|-------------------|
| Numbers in range `[1, N]` or `[0, N-1]` | Numbers not in a bounded range |
| Finding missing/duplicate numbers | General purpose sorting |
| Need O(n) time, O(1) space | Data has no index-value mapping |

---

## 8. Algorithm Selection Guide

```
Is the array nearly sorted?
  ├── YES → Insertion Sort (O(n) best case)
  └── NO
      ├── Numbers in range [1, N]?
      │   └── YES → Cyclic Sort (O(n))
      └── NO
          ├── Is the array small (n ≤ 50)?
          │   └── YES → Insertion Sort
          └── NO
              ├── Need guaranteed O(n log n)?
              │   └── YES → Merge Sort
              └── NO
                  ├── Is memory limited?
                  │   ├── YES → Quick Sort (in-place)
                  │   └── NO → Merge Sort
                  └── Need fastest average case?
                      └── YES → Quick Sort
```

---

## 9. Key Takeaways

1. **Bubble Sort** is simple but inefficient — useful for learning, not production.
2. **Selection Sort** minimizes swaps — use when writes are expensive.
3. **Insertion Sort** is optimal for small/nearly-sorted data — O(n) best case.
4. **Merge Sort** guarantees O(n log n) and is stable — best for predictable performance.
5. **Quick Sort** is fastest in practice due to cache efficiency — the default in many libraries.
6. Quick Sort's worst case O(n²) is avoidable with **randomized pivot** selection.
7. **Cyclic Sort** achieves O(n) for range-based problems — solves a whole family of LeetCode problems (41, 268, 287, 442, 448).
8. The key insight of Cyclic Sort: **element `i` belongs at index `i-1`** — don't increment after a swap.

