# ArrayList in Java

> **Source:** Handwritten DSA Notes – Pages 1–2

---

## 1. What is an ArrayList?

An **ArrayList** is a **dynamic, resizable** array implementation provided by the `java.util` package. Unlike traditional arrays whose size is fixed at creation, an ArrayList automatically grows and shrinks as elements are added or removed.

| Feature | Array | ArrayList |
|---------|-------|-----------|
| Size | Fixed at declaration | Dynamic (grows automatically) |
| Type | Primitives + Objects | Objects only (Wrapper classes) |
| Memory | Contiguous block | Contiguous block (internally uses array) |
| Performance | Faster (no overhead) | Slight overhead for resizing |
| Syntax | `int[] arr = new int[5];` | `ArrayList<Integer> list = new ArrayList<>();` |

---

## 2. Declaration & Initialization

```java
// Basic declaration
ArrayList<Integer> numbers = new ArrayList<>();

// With initial capacity
ArrayList<String> names = new ArrayList<>(20);

// Initialize with values
ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
```

### Generics & Wrapper Classes

ArrayList uses **Generics** (`<T>`) and cannot hold primitive types directly. You must use **Wrapper Classes**:

| Primitive | Wrapper Class |
|-----------|--------------|
| `int` | `Integer` |
| `char` | `Character` |
| `float` | `Float` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `long` | `Long` |

> **Note:** Java performs **autoboxing** (primitive → wrapper) and **unboxing** (wrapper → primitive) automatically.

---

## 3. Internal Working

- **Default initial capacity:** `10`
- When the internal array is full, Java creates a **new array** with **1.5× the current capacity** and copies all elements.
- This resize operation is **O(n)**, but since it happens infrequently, the **amortized time complexity** of `.add()` remains **O(1)**.

```
Initial:  [_, _, _, _, _, _, _, _, _, _]   capacity = 10
After 10 adds: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]   capacity = 10 (FULL)
After 11th add: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, _, _, _, _]  capacity = 15
```

---

## 4. Core Methods

### Adding Elements

```java
list.add(10);           // Appends to end → O(1) amortized
list.add(0, 5);         // Inserts at index 0 → O(n) (shifts elements)
```

### Accessing Elements

```java
int val = list.get(2);  // Returns element at index 2 → O(1)
```

### Checking Existence

```java
boolean exists = list.contains(10);  // Returns true/false → O(n)
```

### Size

```java
int size = list.size(); // Returns number of elements → O(1)
```

### Removing Elements

```java
list.remove(0);              // Removes element at index 0 → O(n)
list.remove(Integer.valueOf(10)); // Removes first occurrence of value 10 → O(n)
```

### Updating Elements

```java
list.set(1, 99);  // Replaces element at index 1 with 99 → O(1)
```

### Complete Method Reference

| Method | Description | Time Complexity |
|--------|-------------|-----------------|
| `add(E e)` | Append element | O(1) amortized |
| `add(int i, E e)` | Insert at index | O(n) |
| `get(int i)` | Get element at index | O(1) |
| `set(int i, E e)` | Replace element | O(1) |
| `remove(int i)` | Remove by index | O(n) |
| `contains(Object o)` | Check if element exists | O(n) |
| `size()` | Get number of elements | O(1) |
| `isEmpty()` | Check if empty | O(1) |
| `clear()` | Remove all elements | O(n) |
| `indexOf(Object o)` | First index of element | O(n) |

---

## 5. Iteration Techniques

```java
// 1. For loop
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// 2. Enhanced for-each loop
for (int num : list) {
    System.out.println(num);
}

// 3. Iterator
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}
```

---

## 6. Multidimensional ArrayList

A **2D ArrayList** is an `ArrayList` of `ArrayList`s — useful when you need a dynamic 2D structure (e.g., adjacency list for graphs).

```java
// Declaration
ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();

// Initialization — MUST create inner lists manually
for (int i = 0; i < rows; i++) {
    matrix.add(new ArrayList<>());
}

// Adding elements
matrix.get(0).add(10);  // Add 10 to row 0
matrix.get(1).add(20);  // Add 20 to row 1

// Accessing elements
int val = matrix.get(0).get(0); // Row 0, Column 0
```

### Visualization

```
matrix = [
    [10, 20, 30],    // Row 0
    [40, 50],        // Row 1
    [60, 70, 80, 90] // Row 2
]
```

> **Key Point:** Each inner ArrayList can have a **different size** — unlike a traditional 2D array where all rows must have the same number of columns.

---

## 7. ArrayList vs LinkedList

| Feature | ArrayList | LinkedList |
|---------|-----------|------------|
| Access by index | O(1) | O(n) |
| Insert at end | O(1) amortized | O(1) |
| Insert at beginning | O(n) | O(1) |
| Memory | Less (contiguous) | More (node + pointers) |
| Best for | Random access | Frequent insertions/deletions |

---

## 8. Common Pitfalls

1. **ConcurrentModificationException:** Don't modify an ArrayList while iterating with for-each. Use `Iterator.remove()` instead.
2. **IndexOutOfBoundsException:** Always check `size()` before accessing by index.
3. **Autoboxing with `remove()`:** `list.remove(1)` removes at **index** 1, not the **value** 1. Use `list.remove(Integer.valueOf(1))` to remove by value.

---

## 9. Summary

- ArrayList is a **dynamic array** that automatically resizes.
- Uses **Generics** and **Wrapper Classes** (no primitives).
- Default capacity is **10**; grows by **1.5×**.
- **Random access** is O(1), but **insertion/deletion** in the middle is O(n).
- Supports **multidimensional** structures via nesting.
- Prefer ArrayList for **read-heavy** operations and LinkedList for **write-heavy** operations.
