# Mathematics for DSA

> **Source:** Handwritten DSA Notes – Pages 75–100

---

## 1. Number Representation

### Signed vs Unsigned Integers

| Type | Bits | Signed Range | Unsigned Range |
|------|------|-------------|----------------|
| byte | 8 | -128 to 127 | 0 to 255 |
| short | 16 | -32,768 to 32,767 | 0 to 65,535 |
| int | 32 | -2³¹ to 2³¹-1 | 0 to 2³²-1 |
| long | 64 | -2⁶³ to 2⁶³-1 | 0 to 2⁶⁴-1 |

**Formula for n-bit integers:**
- **Signed range:** $-2^{n-1}$ to $2^{n-1} - 1$
- **Unsigned range:** $0$ to $2^n - 1$

### Why the Asymmetry?

The sign bit uses one bit, and there's one more negative number than positive because zero is non-negative:
- 8-bit: `10000000` = -128 (no positive counterpart, since `+128` needs 9 bits)

---

## 2. GCD and LCM

### Greatest Common Divisor (GCD)

The GCD of two numbers is the **largest number that divides both** of them.

### Euclidean Algorithm

Based on the property: $\gcd(a, b) = \gcd(b, a \% b)$

```java
// Iterative
public static int gcd(int a, int b) {
    while (b != 0) {
        int temp = b;
        b = a % b;
        a = temp;
    }
    return a;
}

// Recursive
public static int gcd(int a, int b) {
    if (b == 0) return a;
    return gcd(b, a % b);
}
```

### Walkthrough

```
gcd(48, 18):
  48 % 18 = 12 → gcd(18, 12)
  18 % 12 = 6  → gcd(12, 6)
  12 % 6  = 0  → gcd(6, 0) = 6
→ Answer: 6
```

**Time Complexity:** O(log(min(a, b)))

### Least Common Multiple (LCM)

$$\text{lcm}(a, b) = \frac{|a \times b|}{\gcd(a, b)}$$

```java
public static int lcm(int a, int b) {
    return (a / gcd(a, b)) * b;  // Divide first to prevent overflow
}
```

### Key Properties

| Property | GCD | LCM |
|----------|-----|-----|
| gcd(a, 0) = a | lcm(a, 0) = 0 |
| gcd(a, a) = a | lcm(a, a) = a |
| gcd(a, b) × lcm(a, b) = a × b | |
| Commutative | ✅ | ✅ |
| Associative | ✅ | ✅ |

---

## 3. Prime Numbers

### Definition

A prime number is a natural number **greater than 1** that has no positive divisors other than **1 and itself**.

### Basic Primality Test

```java
public static boolean isPrime(int n) {
    if (n <= 1) return false;
    if (n <= 3) return true;
    if (n % 2 == 0 || n % 3 == 0) return false;
    
    for (int i = 5; i * i <= n; i += 6) {
        if (n % i == 0 || n % (i + 2) == 0) {
            return false;
        }
    }
    return true;
}
```

> **Why check up to √n?** If `n = a × b` and both `a, b > √n`, then `a × b > n` — contradiction. So at least one factor must be ≤ √n.

> **Why `i += 6`?** All primes > 3 are of the form `6k ± 1`. Numbers of the form `6k, 6k+2, 6k+3, 6k+4` are divisible by 2 or 3.

**Time Complexity:** O(√n)

### Sieve of Eratosthenes

An efficient algorithm to find **all primes up to n**.

**Algorithm:**
1. Create a boolean array `isPrime[0..n]`, initialize all to `true`.
2. Mark 0 and 1 as not prime.
3. For each number `i` from 2 to √n:
   - If `isPrime[i]` is true, mark all multiples of `i` (starting from `i²`) as not prime.

```java
public static boolean[] sieveOfEratosthenes(int n) {
    boolean[] isPrime = new boolean[n + 1];
    Arrays.fill(isPrime, true);
    isPrime[0] = isPrime[1] = false;
    
    for (int i = 2; i * i <= n; i++) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }
    return isPrime;
}
```

### Visual Walkthrough (n = 30)

```
Start: 2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30

i=2:   2  3  ×  5  ×  7  ×  9  ×  11 ×  13 ×  15 ×  17 ×  19 ×  21 ×  23 ×  25 ×  27 ×  29 ×
i=3:   2  3     5     7     ×     11    13    ×     17    19    ×     23    25    ×     29
i=5:   2  3     5     7           11    13          17    19          23    ×           29

Primes: 2, 3, 5, 7, 11, 13, 17, 19, 23, 29
```

**Time Complexity:** O(n log log n) — nearly linear  
**Space Complexity:** O(n)

---

## 4. Square Root

### Binary Search Approach

Find the integer square root of `n` (i.e., the largest integer `x` such that `x² ≤ n`).

```java
public static int sqrt(int n) {
    if (n < 2) return n;
    
    long start = 1, end = n / 2;
    long result = 0;
    
    while (start <= end) {
        long mid = start + (end - start) / 2;
        if (mid * mid == n) {
            return (int) mid;
        } else if (mid * mid < n) {
            result = mid;       // Candidate answer
            start = mid + 1;
        } else {
            end = mid - 1;
        }
    }
    return (int) result;
}
```

**Time Complexity:** O(log n)

### Newton-Raphson Method

An iterative method for finding roots of equations. For square root:

$$x_{n+1} = \frac{1}{2}\left(x_n + \frac{n}{x_n}\right)$$

```java
public static int sqrtNewton(int n) {
    if (n < 2) return n;
    long x = n;
    while (x * x > n) {
        x = (x + n / x) / 2;
    }
    return (int) x;
}
```

**Convergence:** Quadratic — doubles correct digits each iteration.

---

## 5. Modular Arithmetic

### Key Rules

$$
(a + b) \% m = ((a \% m) + (b \% m)) \% m
$$
$$
(a \times b) \% m = ((a \% m) \times (b \% m)) \% m
$$
$$
(a - b) \% m = ((a \% m) - (b \% m) + m) \% m
$$

> ⚠️ Division does NOT follow the same rule. Use **modular inverse** instead.

### Modular Exponentiation (Fast Power)

Compute $a^b \% m$ efficiently:

```java
public static long modPow(long base, long exp, long mod) {
    long result = 1;
    base %= mod;
    
    while (exp > 0) {
        if ((exp & 1) == 1) {     // If exp is odd
            result = (result * base) % mod;
        }
        exp >>= 1;                // exp = exp / 2
        base = (base * base) % mod;
    }
    return result;
}
```

**Time Complexity:** O(log exp)

---

## 6. Factorial and Combinatorics

### Factorial

$$n! = n \times (n-1) \times (n-2) \times \ldots \times 2 \times 1$$

```java
public static long factorial(int n) {
    if (n <= 1) return 1;
    long result = 1;
    for (int i = 2; i <= n; i++) {
        result *= i;
    }
    return result;
}
```

### Combinations (nCr)

$$\binom{n}{r} = \frac{n!}{r!(n-r)!}$$

Efficient computation using Pascal's Triangle:

$$\binom{n}{r} = \binom{n-1}{r-1} + \binom{n-1}{r}$$

```java
public static long nCr(int n, int r) {
    if (r > n - r) r = n - r;  // Optimization: C(n,r) = C(n,n-r)
    
    long result = 1;
    for (int i = 0; i < r; i++) {
        result *= (n - i);
        result /= (i + 1);
    }
    return result;
}
```

---

## 7. Important Formulas

| Formula | Expression |
|---------|-----------|
| Sum of first n naturals | $\frac{n(n+1)}{2}$ |
| Sum of squares | $\frac{n(n+1)(2n+1)}{6}$ |
| Sum of cubes | $\left(\frac{n(n+1)}{2}\right)^2$ |
| Geometric series | $\frac{a(r^n - 1)}{r - 1}$ |
| Number of digits | $\lfloor \log_{10} n \rfloor + 1$ |
| Power of 2 check | `n & (n-1) == 0` |

---

## 8. Extended Euclidean Algorithm & Bezout's Identity

> **Source:** Pages 85–88

### Bezout's Identity

For any two integers $a$ and $b$, there exist integers $x$ and $y$ such that:

$$ax + by = \gcd(a, b)$$

This is **Bezout's Identity**. The Extended Euclidean Algorithm finds $x$ and $y$.

### Extended Euclidean Algorithm

```java
// Returns [gcd, x, y] such that a*x + b*y = gcd(a,b)
public static int[] extendedGcd(int a, int b) {
    if (b == 0) return new int[]{a, 1, 0};
    
    int[] result = extendedGcd(b, a % b);
    int gcd = result[0];
    int x = result[2];
    int y = result[1] - (a / b) * result[2];
    
    return new int[]{gcd, x, y};
}
```

### Walkthrough

```
extendedGcd(30, 20):
  extendedGcd(20, 10):
    extendedGcd(10, 0): returns [10, 1, 0]
    x = 0, y = 1 - 2*0 = 1 → returns [10, 0, 1]
  x = 1, y = 0 - 1*1 = -1 → returns [10, 1, -1]

Verify: 30×1 + 20×(-1) = 30 - 20 = 10 = gcd(30,20) ✓
```

### Linear Diophantine Equations

An equation $ax + by = c$ has integer solutions **if and only if** $\gcd(a, b)$ divides $c$.

**Applications:**
- **Die Hard / Water Jug Problem:** Can you measure exactly `c` liters using jugs of size `a` and `b`?
  - Answer: Only if $\gcd(a, b)$ divides $c$.

```
Example: Jugs of 3L and 5L, measure 4L?
gcd(3, 5) = 1, and 1 divides 4 → YES, possible!
```

---

## 9. Key Takeaways

1. **Euclidean Algorithm** for GCD runs in O(log(min(a,b))) — very efficient.
2. **Extended Euclidean** finds $x, y$ in $ax + by = \gcd(a,b)$ — basis for modular inverse.
3. **Bezout's Identity** determines if a Diophantine equation has solutions.
4. **Sieve of Eratosthenes** is the gold standard for generating primes up to N.
5. Only check divisibility up to **√n** for primality.
6. All primes > 3 follow the pattern **6k ± 1**.
7. **Modular arithmetic** is essential for handling large numbers in competitive programming.
8. **Newton-Raphson** converges quadratically — much faster than binary search for precision.

