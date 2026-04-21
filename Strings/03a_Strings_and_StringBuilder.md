# Strings & StringBuilder

> **Source:** Handwritten DSA Notes – Pages 23–30

---

## 1. Strings in Java

### Immutability

Strings in Java are **immutable** — once created, their content **cannot be changed**. Any "modification" creates a **new String object** in memory.

```java
String s = "Hello";
s = s + " World";  // Creates a NEW string "Hello World"
                    // The original "Hello" is now eligible for garbage collection
```

### Memory: String Pool vs Heap

Java maintains a **String Pool** (inside the Heap) for string literals to save memory through **interning**.

```java
String a = "Hello";       // Stored in String Pool
String b = "Hello";       // Points to the SAME object in pool
String c = new String("Hello"); // Creates a NEW object on Heap

System.out.println(a == b);      // true  (same reference)
System.out.println(a == c);      // false (different objects)
System.out.println(a.equals(c)); // true  (same content)
```

### Memory Visualization

```
┌──────────────────────────────────────┐
│              HEAP                     │
│  ┌─────────────────────┐            │
│  │   STRING POOL        │            │
│  │  ┌──────────┐       │            │
│  │  │ "Hello"  │←─ a, b│            │
│  │  └──────────┘       │            │
│  └─────────────────────┘            │
│                                      │
│  ┌──────────┐                        │
│  │ "Hello"  │←─ c  (separate object) │
│  └──────────┘                        │
└──────────────────────────────────────┘

STACK:
  a → ref to pool "Hello"
  b → ref to pool "Hello"
  c → ref to heap "Hello"
```

---

## 2. String Comparison

| Method | Compares | Example |
|--------|----------|---------|
| `==` | Reference (memory address) | `a == b` |
| `.equals()` | Content (character by character) | `a.equals(c)` |
| `.equalsIgnoreCase()` | Content (case-insensitive) | `"Hi".equalsIgnoreCase("hi")` |
| `.compareTo()` | Lexicographic order (returns int) | `"abc".compareTo("abd")` → -1 |

> ⚠️ **Always use `.equals()` for content comparison**, never `==`.

---

## 3. Common String Methods

| Method | Description | Example | Result |
|--------|-------------|---------|--------|
| `.length()` | Length of string | `"Hello".length()` | 5 |
| `.charAt(i)` | Character at index | `"Hello".charAt(1)` | 'e' |
| `.substring(s, e)` | Substring [s, e) | `"Hello".substring(1, 3)` | "el" |
| `.indexOf(str)` | First occurrence | `"Hello".indexOf("ll")` | 2 |
| `.contains(str)` | Check substring exists | `"Hello".contains("ell")` | true |
| `.toUpperCase()` | Convert to uppercase | `"hello".toUpperCase()` | "HELLO" |
| `.toLowerCase()` | Convert to lowercase | `"HELLO".toLowerCase()` | "hello" |
| `.trim()` | Remove leading/trailing spaces | `"  hi  ".trim()` | "hi" |
| `.replace(a, b)` | Replace characters | `"Hello".replace('l', 'r')` | "Herro" |
| `.split(regex)` | Split into array | `"a,b,c".split(",")` | ["a","b","c"] |
| `.toCharArray()` | Convert to char[] | `"Hi".toCharArray()` | ['H','i'] |

---

## 4. String Concatenation Complexity

### The Problem with `+` in Loops

```java
String result = "";
for (int i = 0; i < n; i++) {
    result = result + i;  // Creates a NEW string each iteration!
}
```

**Each concatenation:**
1. Creates a new `char[]` of size `old.length + new.length`
2. Copies all old characters + appends new characters
3. Creates a new String object

**Total work:** $1 + 2 + 3 + \ldots + n = \frac{n(n+1)}{2} = O(n^2)$

> **This is extremely inefficient for large n!**

---

## 5. StringBuilder

### Why StringBuilder?

`StringBuilder` is a **mutable** sequence of characters. It modifies the string **in-place** without creating new objects, making string modifications **O(n)** instead of O(n²).

### Declaration

```java
StringBuilder sb = new StringBuilder();        // Empty, default capacity 16
StringBuilder sb2 = new StringBuilder("Hello"); // From string
StringBuilder sb3 = new StringBuilder(50);      // Custom initial capacity
```

### Key Methods

| Method | Description | Example |
|--------|-------------|---------|
| `.append(x)` | Add to end | `sb.append("World")` |
| `.insert(i, x)` | Insert at index | `sb.insert(0, "Hi ")` |
| `.delete(s, e)` | Delete range [s, e) | `sb.delete(0, 3)` |
| `.deleteCharAt(i)` | Delete char at index | `sb.deleteCharAt(0)` |
| `.replace(s, e, str)` | Replace range | `sb.replace(0, 2, "He")` |
| `.reverse()` | Reverse in-place | `sb.reverse()` |
| `.charAt(i)` | Get char at index | `sb.charAt(0)` |
| `.setCharAt(i, c)` | Set char at index | `sb.setCharAt(0, 'h')` |
| `.length()` | Current length | `sb.length()` |
| `.toString()` | Convert to String | `sb.toString()` |

### Performance Comparison

```java
// BAD — O(n²)
String result = "";
for (int i = 0; i < 100000; i++) {
    result += i;
}

// GOOD — O(n)
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

| Operation | String | StringBuilder |
|-----------|--------|---------------|
| Concatenation in loop (n times) | O(n²) | **O(n)** |
| Single concatenation | O(m+n) | **O(n) amortized** |
| Thread safety | Yes (immutable) | No |
| Memory | New object each time | In-place |

---

## 6. Operator Overloading with Strings

Java **overloads the `+` operator** for Strings:

```java
System.out.println("Score: " + 95);        // "Score: 95" (int → String)
System.out.println("List: " + new ArrayList<>()); // "List: []"
System.out.println(1 + 2 + "a");           // "3a" (1+2=3, then 3+"a")
System.out.println("a" + 1 + 2);           // "a12" (left to right: "a"+"1"="a1", "a1"+"2"="a12")
```

> **Key Rule:** When `+` encounters a String operand, it converts the other operand to a String via `.toString()`.

---

## 7. Palindrome Check

A palindrome reads the same forwards and backwards.

### Using Two Pointers

```java
public static boolean isPalindrome(String s) {
    int left = 0, right = s.length() - 1;
    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) {
            return false;
        }
        left++;
        right--;
    }
    return true;
}
```

### Using StringBuilder

```java
public static boolean isPalindrome(String s) {
    return s.equals(new StringBuilder(s).reverse().toString());
}
```

**Time:** O(n), **Space:** O(1) for two-pointer, O(n) for StringBuilder approach.

---

## 8. Key Takeaways

1. **Strings are immutable** — every modification creates a new object.
2. **String Pool** optimizes memory for string literals via interning.
3. Use `.equals()` for content comparison, never `==`.
4. String concatenation in loops is **O(n²)** — always use **StringBuilder** instead.
5. StringBuilder is **mutable** and modifies in-place — O(n) for building strings.
6. Java's `+` operator is overloaded for Strings — handles auto-conversion.
7. The two-pointer technique is ideal for palindrome checking — O(n) time, O(1) space.
