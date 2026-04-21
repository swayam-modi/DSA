# Recursion & Complexity Analysis

> **Source:** Handwritten DSA Notes – Pages 31–47, 57–65

---

## 1. What is Recursion?

Recursion is a technique where a function **calls itself** to solve a problem by breaking it down into smaller subproblems. Every recursive function has two essential parts:

1. **Base Case** — The condition that stops recursion (prevents infinite calls).
2. **Recursive Case** — The function calls itself with a **smaller/simpler** input.

```java
// General structure
public static returnType solve(parameters) {
    if (base condition) {
        return base value;        // BASE CASE
    }
    // Do some work
    return solve(smaller input);  // RECURSIVE CASE
}
```

---

## 2. Visualizing Recursion

### Stack Frames

Each recursive call creates a new **stack frame** in memory:

```
factorial(4)
├── factorial(3)
│   ├── factorial(2)
│   │   ├── factorial(1)
│   │   │   └── returns 1        ← Base case
│   │   └── returns 2 × 1 = 2
│   └── returns 3 × 2 = 6
└── returns 4 × 6 = 24
```

### The Call Stack

```
┌──────────────┐
│ factorial(1)  │ ← Top (current)
├──────────────┤
│ factorial(2)  │
├──────────────┤
│ factorial(3)  │
├──────────────┤
│ factorial(4)  │ ← Bottom (first call)
└──────────────┘
```

Each frame stores:
- Local variables
- Parameters
- Return address (where to resume after returning)

> ⚠️ **Stack Overflow** occurs when the recursion depth is too large and the call stack exceeds its memory limit.

---

## 3. Recursion Tree

A recursion tree is a **visual representation** of all recursive calls. It's critical for **complexity analysis**.

### Example: Fibonacci

```java
int fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}
```

```
                    fib(5)
                  /        \
            fib(4)          fib(3)
           /      \        /      \
       fib(3)   fib(2)  fib(2)  fib(1)
       /    \   /    \   /    \
   fib(2) fib(1) fib(1) fib(0) fib(1) fib(0)
   /    \
fib(1) fib(0)
```

- **Total nodes** ≈ 2^n → Time: **O(2^n)**
- Many **overlapping subproblems** (e.g., `fib(2)` computed 3 times)

---

## 4. Classic Recursion Examples

### 4.1 Factorial

```java
public static int factorial(int n) {
    if (n == 0 || n == 1) return 1;       // Base case
    return n * factorial(n - 1);           // Recursive case
}
```

- **Time:** O(n) — n calls  
- **Space:** O(n) — n stack frames

### 4.2 Sum of Digits

```java
public static int sumOfDigits(int n) {
    if (n == 0) return 0;
    return (n % 10) + sumOfDigits(n / 10);
}
```

### 4.3 Power Function

```java
// Naive: O(n)
public static int power(int base, int exp) {
    if (exp == 0) return 1;
    return base * power(base, exp - 1);
}

// Optimized: O(log n) — Fast Exponentiation
public static int fastPower(int base, int exp) {
    if (exp == 0) return 1;
    int half = fastPower(base, exp / 2);
    if (exp % 2 == 0) return half * half;
    else return base * half * half;
}
```

### 4.4 Binary Search (Recursive)

```java
public static int binarySearch(int[] arr, int target, int start, int end) {
    if (start > end) return -1;
    int mid = start + (end - start) / 2;
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) return binarySearch(arr, target, mid + 1, end);
    else return binarySearch(arr, target, start, mid - 1);
}
```

---

## 5. Complexity Analysis — Big O Notation

### 5.1 What is Big O?

Big O describes the **upper bound** of an algorithm's growth rate — how the running time (or space) scales with input size `n`.

### 5.2 Complexity Hierarchy

$$O(1) < O(\log N) < O(\sqrt{N}) < O(N) < O(N \log N) < O(N^2) < O(N^3) < O(2^N) < O(N!)$$

### 5.3 Comparison Table

| Complexity | Name | Example | n=1000 |
|------------|------|---------|--------|
| O(1) | Constant | Array access | 1 |
| O(log n) | Logarithmic | Binary Search | ~10 |
| O(√n) | Square root | Prime check | ~31 |
| O(n) | Linear | Linear Search | 1,000 |
| O(n log n) | Linearithmic | Merge Sort | ~10,000 |
| O(n²) | Quadratic | Bubble Sort | 1,000,000 |
| O(2^n) | Exponential | Subsets | ~10^301 |
| O(n!) | Factorial | Permutations | Beyond computation |

### 5.4 Asymptotic Notations

| Notation | Meaning | Analogy |
|----------|---------|---------|
| **O (Big O)** | Upper bound (worst case) | ≤ |
| **Ω (Omega)** | Lower bound (best case) | ≥ |
| **Θ (Theta)** | Tight bound (exact) | = |
| **o (Little o)** | Strict upper bound | < |
| **ω (Little omega)** | Strict lower bound | > |

---

## 6. Space Complexity

Space complexity counts the **total memory used** by an algorithm:

- **Input space** — memory to store the input
- **Auxiliary space** — extra memory used during execution (stack frames, temporary arrays)

| Example | Auxiliary Space |
|---------|----------------|
| Iterative loop | O(1) |
| Recursive function (depth d) | O(d) |
| Merge Sort | O(n) |
| Creating a copy of array | O(n) |

---

## 7. Recurrence Relations

A recurrence relation expresses the running time of a recursive algorithm in terms of smaller inputs.

### 7.1 Common Recurrences

| Algorithm | Recurrence | Solution |
|-----------|-----------|----------|
| Linear search | T(n) = T(n-1) + O(1) | O(n) |
| Binary search | T(n) = T(n/2) + O(1) | O(log n) |
| Merge sort | T(n) = 2T(n/2) + O(n) | O(n log n) |
| Fibonacci (naive) | T(n) = T(n-1) + T(n-2) + O(1) | O(2^n) |
| Fast exponentiation | T(n) = T(n/2) + O(1) | O(log n) |

### 7.2 Solving by Substitution

Example: T(n) = T(n-1) + 1, T(1) = 1

```
T(n) = T(n-1) + 1
     = T(n-2) + 1 + 1
     = T(n-3) + 1 + 1 + 1
     ...
     = T(1) + (n-1)
     = 1 + n - 1
     = n
→ T(n) = O(n)
```

---

## 8. Master Theorem

The Master Theorem provides a **direct solution** for recurrences of the form:

$$T(n) = aT\left(\frac{n}{b}\right) + O(n^d)$$

Where:
- `a` = number of subproblems
- `b` = factor by which input is reduced
- `d` = exponent of the work done outside recursion

### Three Cases

Compute $\log_b a$ and compare with $d$:

| Case | Condition | Result |
|------|-----------|--------|
| **Case 1** | $d < \log_b a$ | $T(n) = O(n^{\log_b a})$ |
| **Case 2** | $d = \log_b a$ | $T(n) = O(n^d \cdot \log n)$ |
| **Case 3** | $d > \log_b a$ | $T(n) = O(n^d)$ |

### Examples

**Binary Search:** $T(n) = 1 \cdot T(n/2) + O(1)$
- $a=1, b=2, d=0$
- $\log_2 1 = 0 = d$ → **Case 2:** $T(n) = O(\log n)$

**Merge Sort:** $T(n) = 2T(n/2) + O(n)$
- $a=2, b=2, d=1$
- $\log_2 2 = 1 = d$ → **Case 2:** $T(n) = O(n \log n)$

**Karatsuba Multiplication:** $T(n) = 3T(n/2) + O(n)$
- $a=3, b=2, d=1$
- $\log_2 3 ≈ 1.58 > d$ → **Case 1:** $T(n) = O(n^{1.58})$

---

## 9. Linear Recurrence Relations

### Homogeneous Form

$$a_n = c_1 a_{n-1} + c_2 a_{n-2} + \ldots + c_k a_{n-k}$$

**Solution Method:**
1. Write the **characteristic equation**: $r^k - c_1 r^{k-1} - c_2 r^{k-2} - \ldots - c_k = 0$
2. Find roots $r_1, r_2, \ldots$
3. General solution: $a_n = A \cdot r_1^n + B \cdot r_2^n + \ldots$
4. Use initial conditions to solve for A, B, ...

### Example: Fibonacci

$F(n) = F(n-1) + F(n-2)$

Characteristic equation: $r^2 - r - 1 = 0$

Roots: $r = \frac{1 \pm \sqrt{5}}{2}$

→ $\phi = \frac{1+\sqrt{5}}{2} \approx 1.618$ (Golden Ratio)

→ $F(n) = \frac{\phi^n - \psi^n}{\sqrt{5}}$ where $\psi = \frac{1-\sqrt{5}}{2}$

---

## 10. Akra-Bazzi Method

> **Source:** Pages 55–56 — A more **general** method than the Master Theorem for solving recurrences.

### When to Use

The Master Theorem requires **equal-sized** subproblems: $T(n) = aT(n/b) + f(n)$. The **Akra-Bazzi method** handles **unequal splits**:

$$T(n) = \sum_{i=1}^{k} a_i T\left(\frac{n}{b_i}\right) + g(n)$$

Where subproblems can have **different sizes** (different $b_i$ values).

### Steps

1. **Find p** such that: $\sum_{i=1}^{k} \frac{a_i}{b_i^p} = 1$
2. **Compute the solution:**

$$T(n) = \Theta\left(n^p \left(1 + \int_1^n \frac{g(u)}{u^{p+1}} du\right)\right)$$

### Example

$T(n) = T(n/2) + T(n/3) + n$

Here $a_1 = 1, b_1 = 2, a_2 = 1, b_2 = 3, g(n) = n$.

**Step 1:** Find $p$ such that $\frac{1}{2^p} + \frac{1}{3^p} = 1$
- By numerical methods: $p \approx 0.788$

**Step 2:** Compute the integral:
$$T(n) = \Theta\left(n^{0.788}\left(1 + \int_1^n \frac{u}{u^{1.788}}du\right)\right) = \Theta(n)$$

### Akra-Bazzi vs Master Theorem

| Feature | Master Theorem | Akra-Bazzi |
|---------|----------------|------------|
| Subproblem sizes | Must be equal ($n/b$) | Can be different ($n/b_i$) |
| Number of terms | One term | Multiple terms |
| Ease of use | Simple (3 cases) | Requires integral |
| Generality | Limited | Very general |

---

## 11. Key Takeaways

1. Every recursion needs a **base case** to avoid infinite loops.
2. **Recursion tree** visualization is essential for understanding time complexity.
3. Big O gives us a **language to compare algorithms** independent of hardware.
4. The **Master Theorem** is a powerful shortcut for divide-and-conquer recurrences with equal splits.
5. The **Akra-Bazzi method** generalizes the Master Theorem for unequal subproblem sizes.
6. Recursive space complexity = **O(maximum recursion depth)**.
7. Naive recursive solutions often have **exponential** complexity — optimize with **memoization** or **iteration**.

