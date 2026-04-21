# Bit Manipulation

> **Source:** Handwritten DSA Notes – Pages 66–74

---

## 1. Introduction

Bit manipulation involves performing operations directly on the **binary representation** of numbers. It is extremely fast (operates at CPU instruction level) and is heavily used in competitive programming, cryptography, networking, and systems programming.

### Number Systems Review

| Base | Name | Digits | Example |
|------|------|--------|---------|
| 2 | Binary | 0, 1 | `1010₂ = 10₁₀` |
| 8 | Octal | 0–7 | `12₈ = 10₁₀` |
| 10 | Decimal | 0–9 | `10₁₀` |
| 16 | Hexadecimal | 0–9, A–F | `A₁₆ = 10₁₀` |

### Binary Conversion

```
Decimal → Binary (repeated division by 2):
13 → 13/2=6 R1, 6/2=3 R0, 3/2=1 R1, 1/2=0 R1
→ Read remainders bottom-up: 1101₂

Binary → Decimal (positional value):
1101₂ = 1×2³ + 1×2² + 0×2¹ + 1×2⁰ = 8 + 4 + 0 + 1 = 13
```

---

## 2. Bitwise Operators

### 2.1 AND (`&`)

Returns `1` only if **both** bits are `1`.

```
  1 0 1 1  (11)
& 1 1 0 1  (13)
---------
  1 0 0 1  (9)
```

**Use Cases:**
- Check if a number is even/odd: `n & 1`
- Masking specific bits
- Clear bits

### 2.2 OR (`|`)

Returns `1` if **either** bit is `1`.

```
  1 0 1 1  (11)
| 1 1 0 1  (13)
---------
  1 1 1 1  (15)
```

**Use Cases:**
- Set specific bits
- Combine flags

### 2.3 XOR (`^`)

Returns `1` if bits are **different**.

```
  1 0 1 1  (11)
^ 1 1 0 1  (13)
---------
  0 1 1 0  (6)
```

**Key Properties:**
- `a ^ a = 0` (any number XOR itself = 0)
- `a ^ 0 = a` (any number XOR 0 = itself)
- `a ^ b = b ^ a` (commutative)
- `(a ^ b) ^ c = a ^ (b ^ c)` (associative)

**Use Cases:**
- Find the unique element in an array where all others appear twice
- Swap two numbers without a temp variable
- Toggle bits

### 2.4 NOT (`~`) — Complement

Flips all bits (including the sign bit in signed numbers).

```
~ 0000 1011  (11)
= 1111 0100  (-12 in 2's complement)
```

> Rule: `~n = -(n + 1)`

### 2.5 Left Shift (`<<`)

Shifts bits to the left by `n` positions, filling with `0`s from the right.

```
5 << 1 = 10
5 << 2 = 20
```

> `a << n` = `a × 2^n`

### 2.6 Right Shift (`>>`)

Shifts bits to the right by `n` positions. For signed numbers, the sign bit is preserved (arithmetic shift).

```
20 >> 1 = 10
20 >> 2 = 5
```

> `a >> n` = `a / 2^n` (integer division)

### Operator Summary

| Operator | Symbol | Rule | Example (5, 3) |
|----------|--------|------|-----------------|
| AND | `&` | Both 1 | `5 & 3 = 1` |
| OR | `\|` | Either 1 | `5 \| 3 = 7` |
| XOR | `^` | Different | `5 ^ 3 = 6` |
| NOT | `~` | Flip all | `~5 = -6` |
| Left Shift | `<<` | × 2^n | `5 << 1 = 10` |
| Right Shift | `>>` | ÷ 2^n | `5 >> 1 = 2` |

---

## 3. Common Bit Hacks

### 3.1 Check Even or Odd

```java
if ((n & 1) == 0) {
    // EVEN — last bit is 0
} else {
    // ODD — last bit is 1
}
```

Why it works: The least significant bit (LSB) is `1` for all odd numbers and `0` for even.

### 3.2 Get the i-th Bit

```java
int bit = (n >> i) & 1;
// OR
int bit = (n & (1 << i)) != 0 ? 1 : 0;
```

**Explanation:** Create a mask with a `1` at position `i`, then AND with `n`.

```
n = 13 = 1101₂
Get bit 2:
  1 << 2 = 0100
  1101 & 0100 = 0100 (non-zero → bit is 1)
```

### 3.3 Set the i-th Bit (to 1)

```java
n = n | (1 << i);
```

```
n = 9 = 1001₂
Set bit 1:
  1 << 1 = 0010
  1001 | 0010 = 1011 = 11
```

### 3.4 Clear the i-th Bit (to 0)

```java
n = n & ~(1 << i);
```

```
n = 13 = 1101₂
Clear bit 2:
  1 << 2 = 0100
  ~0100  = 1011
  1101 & 1011 = 1001 = 9
```

### 3.5 Toggle the i-th Bit

```java
n = n ^ (1 << i);
```

XOR with a mask: if the bit is `1`, it becomes `0`; if `0`, it becomes `1`.

### 3.6 Check if Power of 2

```java
boolean isPowerOf2 = (n > 0) && ((n & (n - 1)) == 0);
```

**Why it works:** Powers of 2 have exactly one `1` bit. `n - 1` flips all bits after that `1`, so their AND is `0`.

```
8 = 1000,  7 = 0111  → 1000 & 0111 = 0000 ✓
6 = 0110,  5 = 0101  → 0110 & 0101 = 0100 ✗
```

### 3.7 Count Set Bits (Brian Kernighan's Algorithm)

```java
int count = 0;
while (n > 0) {
    n = n & (n - 1);  // Removes the lowest set bit
    count++;
}
```

**Time:** O(number of set bits) — faster than checking all 32 bits.

### 3.8 Swap Two Numbers

```java
a = a ^ b;
b = a ^ b;  // b = (a^b)^b = a
a = a ^ b;  // a = (a^b)^a = b
```

### 3.9 Find Unique Element

Given an array where every element appears **twice** except one, find the unique element:

```java
int unique = 0;
for (int num : arr) {
    unique ^= num;  // Pairs cancel out (a ^ a = 0)
}
// unique now holds the answer
```

---

## 4. Two's Complement (Negative Numbers)

Computers store negative numbers using **2's complement**:

1. Write the binary of the positive number
2. Flip all bits (1's complement)
3. Add 1

```
+5 = 00000101
 ~5 = 11111010  (1's complement)
 +1 = 11111011  (-5 in 2's complement)
```

### Range for n-bit signed integer:

$$-2^{n-1} \text{ to } 2^{n-1} - 1$$

| Type | Bits | Range |
|------|------|-------|
| byte | 8 | -128 to 127 |
| short | 16 | -32,768 to 32,767 |
| int | 32 | -2,147,483,648 to 2,147,483,647 |
| long | 64 | -9.2 × 10¹⁸ to 9.2 × 10¹⁸ |

---

## 5. Bit Manipulation Practice Problems

| Problem | Key Idea | Complexity |
|---------|----------|------------|
| Check if i-th bit is set | `n & (1 << i)` | O(1) |
| Set i-th bit | `n \| (1 << i)` | O(1) |
| Clear i-th bit | `n & ~(1 << i)` | O(1) |
| Toggle i-th bit | `n ^ (1 << i)` | O(1) |
| Is power of 2? | `n & (n-1) == 0` | O(1) |
| Count set bits | Brian Kernighan's | O(k), k = set bits |
| Find unique in pairs | XOR all elements | O(n) |
| Multiply by 2^k | `n << k` | O(1) |
| Divide by 2^k | `n >> k` | O(1) |

---

## 6. Key Takeaways

1. **Bitwise operations are O(1)** and execute at hardware speed — fastest possible operations.
2. `n & 1` checks parity (even/odd) — faster than `n % 2`.
3. `n & (n-1)` removes the lowest set bit — foundation for many tricks.
4. **XOR** is the most versatile bit operator — used for finding unique elements, swapping, toggling, and encryption.
5. **Left shift** multiplies by powers of 2; **right shift** divides.
6. Understanding **2's complement** is essential for working with signed integers.
7. Many interview problems can be solved elegantly with bit manipulation.
