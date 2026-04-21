# Trie (Prefix Tree)

> **Source:** Handwritten DSA Notes – Pages 178–181

---

## 1. What is a Trie?

A Trie is a tree-like data structure for storing strings where **each node represents a character**. It enables **O(L) prefix search** where L is the word length — much faster than HashMap for prefix-based queries.

### Structure

```
Insert: "cat", "car", "cap", "do", "dog"

        (root)
       /      \
      c         d
      |         |
      a         o
    / | \       |
   t   r  p     g
```

Each path from root to a leaf (or marked node) represents a complete word.

---

## 2. TrieNode Structure

```java
class TrieNode {
    TrieNode[] children = new TrieNode[128];  // ASCII characters
    boolean isEndOfWord = false;
}
```

- `children[128]` → one slot per ASCII character
- `isEndOfWord` → marks if this node completes a valid word

---

## 3. Trie Operations

```java
class Trie {
    TrieNode root = new TrieNode();

    // Insert — O(L)
    void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c] == null)
                node.children[c] = new TrieNode();
            node = node.children[c];
        }
        node.isEndOfWord = true;
    }

    // Search — O(L)
    boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c] == null) return false;
            node = node.children[c];
        }
        return node.isEndOfWord;
    }

    // Prefix Search — O(L)
    boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (node.children[c] == null) return false;
            node = node.children[c];
        }
        return true;
    }
}
```

### Walkthrough

```
Insert "cat":
  root → c (create) → a (create) → t (create, mark end)

Insert "car":
  root → c (exists) → a (exists) → r (create, mark end)

Search "cat":
  root → c ✓ → a ✓ → t ✓ → isEndOfWord = true → FOUND

Search "ca":
  root → c ✓ → a ✓ → isEndOfWord = false → NOT FOUND

startsWith "ca":
  root → c ✓ → a ✓ → node exists → TRUE
```

---

## 4. Complexity

| Operation | Time | Space |
|-----------|------|-------|
| Insert | O(L) | O(L) per word |
| Search | O(L) | — |
| Prefix search | O(L) | — |
| Delete | O(L) | — |

Where **L** = length of the word.

---

## 5. Trie vs HashMap

| Feature | Trie | HashMap |
|---------|------|---------|
| Prefix search | ✅ Efficient O(L) | ❌ Not supported |
| Exact search | O(L) | O(1) average |
| Sorted order | ✅ Natural | ❌ No |
| Memory | Higher (pointers) | Lower |
| Use case | Autocomplete, spell check | Key-value lookup |

---

## 6. Applications

| Application | How Trie is Used |
|-------------|-----------------|
| **Autocomplete** | Find all words starting with a prefix |
| **Spell checker** | Check if word exists in dictionary |
| **IP routing** | Longest prefix matching |
| **Phone directory** | Contact search by prefix |
| **Word games** | Validate words efficiently |

---

## 7. Key Takeaways

1. Trie stores strings character-by-character in a **tree structure**.
2. All operations are **O(L)** where L = word length — independent of number of stored words.
3. `isEndOfWord` flag distinguishes complete words from prefixes (e.g., "car" vs "ca").
4. Trie excels at **prefix-based queries** — autocomplete, spell-check, IP routing.
5. Higher memory usage than HashMap due to pointer arrays, but provides natural sorting.
