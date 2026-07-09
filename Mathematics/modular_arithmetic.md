# Modular Arithmetic — Reference Notes

## 1. What "mod" actually means

- `a mod m` = the remainder when `a` is divided by `m`, always in range `[0, m-1]`.
- Every integer `a` can be written as `a = q*m + r`, where `r = a mod m`.
- Two numbers `a` and `b` are **congruent mod m** (written `a ≡ b (mod m)`) if they leave the same remainder when divided by `m` — equivalently, `m` divides `(a - b)`.
- Intuition: think of a clock with `m` positions. Numbers wrap around every `m` steps; `a mod m` tells you which position you land on.

## 2. Why competitive programming uses it

- Problems ask for answers "mod 1e9+7" (or similar) because the true answer can be astronomically large (factorials, big products, huge concatenated numbers like in your `sumAndMultiply` problem).
- `1_000_000_007` is chosen because it's a prime just under `2^31`, which keeps intermediate products (up to `(m-1)^2 ≈ 10^18`) safely inside a 64-bit `long` without overflow, and being prime makes modular inverses well-defined (see §6).

## 3. The core distributive properties

These are the rules that make it _safe_ to reduce numbers early instead of computing the true huge value first:

```
(a + b) mod m = ((a mod m) + (b mod m)) mod m
(a - b) mod m = ((a mod m) - (b mod m) + m) mod m     <-- note the +m, see §4
(a × b) mod m = ((a mod m) × (b mod m)) mod m
```

- **Why it works:** write `a = q1*m + r1`, `b = q2*m + r2`. Any product/sum of the `q*m` parts is itself a multiple of `m`, so it vanishes once you take `% m` again. Only the remainders `r1, r2` survive the final mod — so it doesn't matter whether you reduce before or after combining.
- This is exactly what you verified earlier: reducing `x` down to `57565553` before multiplying by `66` gave the same final answer as multiplying the full 17-digit number first.
- **No such simple rule for division** — division needs modular inverse (§6).

## 4. The negative-remainder trap (language-specific gotcha)

- Mathematically, `mod` results are always non-negative. But **Java, C++, and C's `%` operator can return negative values** when the dividend is negative (`-5 % 3` gives `-2` in Java, not `1`). Python's `%` does _not_ have this problem — it always returns a non-negative result matching the sign of the divisor.
- This bites you specifically during **subtraction**, since `a mod m` and `b mod m` are both in `[0, m-1]`, but `a - b` can still be negative.
- **Standard fix, applies in Java/C++:**
  ```java
  long result = ((a - b) % MOD + MOD) % MOD;
  ```
  Adding `MOD` before the final `% MOD` guarantees a positive result regardless of how negative the raw subtraction went (as long as `|a - b| < MOD`, one `+MOD` is always enough).

## 5. Modular exponentiation (fast power)

- Computing `a^b mod m` by multiplying `a` by itself `b` times is too slow for large `b` (e.g. `b ~ 10^9`).
- **Fast exponentiation ("binary exponentiation")** computes it in `O(log b)` by repeated squaring:
  ```java
  long power(long a, long b, long m) {
      long result = 1;
      a %= m;
      while (b > 0) {
          if ((b & 1) == 1) result = (result * a) % m;
          a = (a * a) % m;
          b >>= 1;
      }
      return result;
  }
  ```
- This is the building block for modular inverse via Fermat's Little Theorem (§6) and for precomputing `pow10[]` arrays like in your problem (though there, simple iterative multiplication is enough since you need _every_ prefix power, not just one).

## 6. Modular inverse (this is where division comes in)

- You **cannot** just do `(a / b) mod m` — division doesn't distribute over mod the way +, -, × do.
- Instead, define the **modular inverse** of `b`, written `b⁻¹`, as the number such that `(b × b⁻¹) mod m = 1`. Then:
  ```
  (a / b) mod m  =  (a × b⁻¹) mod m
  ```
- **Fermat's Little Theorem** (only works when `m` is prime, which `1e9+7` is):
  ```
  b⁻¹ ≡ b^(m-2) mod m
  ```
  So you compute the inverse using fast exponentiation from §5: `power(b, m-2, m)`.
- **Extended Euclidean Algorithm** is the general-purpose method that works even when `m` isn't prime, as long as `gcd(b, m) == 1`.
- **Where this shows up:** computing `nCr mod p` (combinatorics), dividing sums, or any formula involving division under a modulus.

## 7. Modular arithmetic with factorials / combinatorics (nCr mod p)

- Precompute factorials mod `p` and their inverses:
  ```java
  fact[0] = 1;
  for (i = 1; i <= n; i++) fact[i] = (fact[i-1] * i) % MOD;
  invFact[n] = power(fact[n], MOD - 2, MOD);
  for (i = n; i >= 1; i--) invFact[i-1] = (invFact[i] * i) % MOD;
  ```
- Then `nCr mod p = fact[n] * invFact[r] % p * invFact[n-r] % p`.
- This pattern shows up constantly in DP-with-counting problems.

## 8. The "prefix peeling" trick (the technique from your problem)

- This is the pattern you used in `sumAndMultiply`: you want the value of a **substring** of digits (or subarray of a rolling construction), but you've only stored **prefix** values mod `m`.
- General template: if `P[i]` = some rolling combination of the first `i` elements (mod `m`), and you know how many "positions" separate `P[l]` from `P[r]`, you can peel off the prefix:
  ```
  value(l, r) = (P[r] - P[l] * base^shift) mod m
  ```
  where `base` is whatever multiplier your rolling construction uses (10 for digit concatenation, a chosen prime for string hashing, etc.), and `shift` is the exact count of "steps" between `l` and `r`.
- This exact template is used in:
  - Digit concatenation (your problem — `base = 10`, `shift = count of nonzero digits`)
  - **Rolling hash / string hashing** (`base` = some prime, used for substring comparison in `O(1)`)
  - Prefix-XOR / prefix-sum with a multiplicative twist

## 9. Key pitfalls checklist (things that bite people in practice)

- **Forgetting `+ MOD` after subtraction** → silent negative results (Java/C++ only).
- **Uninitialized `pow10[0]`** → defaults to `0` in Java, silently zeroing every dependent computation (the bug you hit earlier!). Always explicitly set `pow10[0] = 1`.
- **Overflow before the mod is applied** — always mod _after_ multiplication, not before, when combining two already-small values: `(a * b) % MOD` is fine in a `long` as long as `a, b < MOD` (since `MOD ~ 1e9`, product is `~1e18`, fits in a 64-bit `long`, but would overflow `int`).
- **Casting to `int` too early** — truncates/overflows before the mod reduction happens. Always do arithmetic in `long`, only cast to `int` at the very end after the final `% MOD`.
- **Using division directly** instead of modular inverse — a very common silent-wrong-answer bug in combinatorics problems.
- **Assuming order is preserved under mod** — `a > b` does _not_ imply `(a mod m) > (b mod m)`. This is exactly why the log10-based digit-count guess in your very first version of the code was broken — magnitude comparisons don't survive modular reduction.

## 10. Chinese Remainder Theorem (CRT) — good to know exists

- If you need a number mod `m1` and mod `m2` (with `gcd(m1, m2) = 1`) and want to reconstruct the answer mod `m1 * m2`, CRT lets you combine them.
- Rare in typical LeetCode-style problems, but shows up in harder number-theory problems and cryptography contexts.
