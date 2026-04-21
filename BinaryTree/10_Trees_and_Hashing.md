# Trees & Hashing

> **Source:** Handwritten DSA Notes – Pages 152–181

---

## 1. Binary Tree

### Terminology

| Term | Definition |
|------|-----------|
| **Root** | Topmost node (no parent) |
| **Leaf** | Node with no children |
| **Height** | Longest path from node to a leaf |
| **Depth** | Distance from root to the node |
| **Level** | Depth + 1 |
| **Degree** | Number of children of a node |
| **Subtree** | Tree formed by a node and its descendants |

### Node Structure

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) {
        this.val = val;
    }
}
```

### Visual

```
          1          ← Root (depth 0, height 2)
        /   \
       2     3       ← depth 1
      / \     \
     4   5     6     ← Leaves (depth 2)
```

---

## 2. Types of Binary Trees

| Type | Definition |
|------|-----------|
| **Full BT** | Every node has 0 or 2 children |
| **Complete BT** | All levels filled except possibly last (filled left to right) |
| **Perfect BT** | All internal nodes have 2 children, all leaves at same depth |
| **Balanced BT** | Height of left and right subtrees differ by at most 1 |
| **Degenerate** | Every node has only 1 child (essentially a linked list) |

### Properties

| Property | Formula |
|----------|---------|
| Max nodes at level `l` | $2^l$ |
| Max nodes in tree of height `h` | $2^{h+1} - 1$ |
| Min height for `n` nodes | $\lfloor \log_2 n \rfloor$ |
| Leaf nodes in Full BT | Internal nodes + 1 |

---

## 3. Tree Traversals

### Depth-First Traversals

```java
// Inorder: Left → Root → Right (gives sorted order for BST)
void inorder(TreeNode node) {
    if (node == null) return;
    inorder(node.left);
    System.out.print(node.val + " ");
    inorder(node.right);
}

// Preorder: Root → Left → Right
void preorder(TreeNode node) {
    if (node == null) return;
    System.out.print(node.val + " ");
    preorder(node.left);
    preorder(node.right);
}

// Postorder: Left → Right → Root
void postorder(TreeNode node) {
    if (node == null) return;
    postorder(node.left);
    postorder(node.right);
    System.out.print(node.val + " ");
}
```

### Example

```
      1
    /   \
   2     3
  / \
 4   5

Inorder:   4 2 5 1 3
Preorder:  1 2 4 5 3
Postorder: 4 5 2 3 1
```

### Breadth-First Traversal (Level Order)

```java
void levelOrder(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        System.out.print(node.val + " ");
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
// Output: 1 2 3 4 5
```

---

## 4. Binary Search Tree (BST)

### Property

For every node: **left child < node < right child**

```
        8
       / \
      3   10
     / \    \
    1   6    14
       / \   /
      4   7 13
```

### Search — O(log n) average, O(n) worst

```java
TreeNode search(TreeNode root, int target) {
    if (root == null || root.val == target) return root;
    if (target < root.val) return search(root.left, target);
    return search(root.right, target);
}
```

### Insert

```java
TreeNode insert(TreeNode root, int val) {
    if (root == null) return new TreeNode(val);
    if (val < root.val) root.left = insert(root.left, val);
    else if (val > root.val) root.right = insert(root.right, val);
    return root;
}
```

### BST Complexity

| Operation | Average | Worst (Skewed) |
|-----------|---------|----------------|
| Search | O(log n) | O(n) |
| Insert | O(log n) | O(n) |
| Delete | O(log n) | O(n) |

> **Inorder traversal of a BST always gives sorted output.**

---

## 5. HashMap

### Concept

A HashMap stores **key-value pairs** with **O(1)** average time for `put`, `get`, and `remove` operations.

### How Hashing Works

```
Key → hashFunction(key) → index → Store value at arr[index]
```

1. Compute the **hash code** of the key.
2. Map hash code to an **array index**: `index = hashCode % arraySize`
3. Store the value at that index.

### Java Usage

```java
HashMap<String, Integer> map = new HashMap<>();
map.put("Alice", 90);
map.put("Bob", 85);
int score = map.get("Alice");       // 90
boolean has = map.containsKey("Bob"); // true
map.remove("Bob");
```

### Collision Handling

When two keys hash to the same index:

**Chaining (Linked List):** Each bucket stores a linked list of entries.

```
Index 0: → [Alice, 90] → [Charlie, 78] → null
Index 1: → [Bob, 85] → null
Index 2: → empty
```

### Time Complexity

| Operation | Average | Worst (all collisions) |
|-----------|---------|------------------------|
| put | O(1) | O(n) |
| get | O(1) | O(n) |
| remove | O(1) | O(n) |
| containsKey | O(1) | O(n) |

### Common HashMap Methods

| Method | Description |
|--------|-------------|
| `put(K, V)` | Insert/update key-value pair |
| `get(K)` | Get value by key |
| `containsKey(K)` | Check if key exists |
| `remove(K)` | Remove by key |
| `keySet()` | Get all keys |
| `values()` | Get all values |
| `entrySet()` | Get all key-value pairs |
| `getOrDefault(K, V)` | Get value or default |

### Counting Frequency Pattern

```java
HashMap<Character, Integer> freq = new HashMap<>();
for (char c : str.toCharArray()) {
    freq.put(c, freq.getOrDefault(c, 0) + 1);
}
```

---

## 6. Trie (Prefix Tree)

### Concept

A Trie is a tree-like data structure for **efficient string operations** — searching, auto-complete, and spell checking. Each node represents a **character**.

### Structure

```java
class TrieNode {
    TrieNode[] children = new TrieNode[256]; // For all Unicode chars (or 26 for lowercase)
    boolean isEndOfWord;
}
```

### Visual

```
Inserting: "cat", "car", "dog"

         root
        /    \
       c      d
       |      |
       a      o
      / \     |
     t   r    g
     *   *    *    (* = end of word)
```

### Operations

```java
class Trie {
    TrieNode root = new TrieNode();

    // Insert — O(word length)
    void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c] == null)
                node.children[c] = new TrieNode();
            node = node.children[c];
        }
        node.isEndOfWord = true;
    }

    // Search — O(word length)
    boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c] == null) return false;
            node = node.children[c];
        }
        return node.isEndOfWord;
    }

    // Starts With (prefix) — O(prefix length)
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

### Trie Complexity

| Operation | Time | Space |
|-----------|------|-------|
| Insert | O(L) | O(L) per word |
| Search | O(L) | — |
| Prefix search | O(L) | — |

Where L = length of the word.

### Trie vs HashMap

| Feature | Trie | HashMap |
|---------|------|---------|
| Prefix search | ✅ Efficient | ❌ Not supported |
| Exact search | O(L) | O(1) average |
| Sorted order | ✅ Natural | ❌ No |
| Memory | Higher (pointers) | Lower |
| Use case | Autocomplete, spell check | Key-value lookup |

---

## 7. AVL Trees (Self-Balancing BST)

> **Source:** Pages 160–165

### Problem with BSTs

A regular BST can become **skewed** (degenerate to a linked list) with sorted insertions, degrading operations to O(n). **AVL Trees** solve this by maintaining balance.

### Balance Factor

$$\text{Balance Factor (BF)} = \text{Height(left subtree)} - \text{Height(right subtree)}$$

An AVL tree requires: $-1 \leq BF \leq 1$ for **every** node.

### Rotations

When an insertion/deletion violates the balance, **rotations** restore it:

#### 1. Right Rotation (LL Imbalance)

Left child is too heavy on the left side.

```
        z                    y
       / \                 /   \
      y   T4    →→→       x     z
     / \                 / \   / \
    x   T3              T1 T2 T3 T4
   / \
  T1  T2
```

#### 2. Left Rotation (RR Imbalance)

Right child is too heavy on the right side.

```
    z                       y
   / \                    /   \
  T1   y      →→→       z     x
      / \               / \   / \
     T2  x             T1 T2 T3 T4
        / \
       T3  T4
```

#### 3. Left-Right Rotation (LR Imbalance)

Left child is heavy on its right side — do left rotation on child, then right rotation on node.

```
      z               z               x
     / \             / \            /   \
    y   T4   →→→    x   T4  →→→   y     z
   / \             / \            / \   / \
  T1  x           y   T3        T1 T2 T3 T4
     / \         / \
    T2  T3      T1  T2
```

#### 4. Right-Left Rotation (RL Imbalance)

Right child is heavy on its left side — do right rotation on child, then left rotation on node.

### Java Implementation (Insert with Balancing)

```java
int height(TreeNode node) {
    return node == null ? -1 : node.height;
}

int getBalance(TreeNode node) {
    return node == null ? 0 : height(node.left) - height(node.right);
}

TreeNode rightRotate(TreeNode z) {
    TreeNode y = z.left;
    TreeNode T3 = y.right;
    y.right = z;
    z.left = T3;
    z.height = Math.max(height(z.left), height(z.right)) + 1;
    y.height = Math.max(height(y.left), height(y.right)) + 1;
    return y;
}

TreeNode leftRotate(TreeNode z) {
    TreeNode y = z.right;
    TreeNode T2 = y.left;
    y.left = z;
    z.right = T2;
    z.height = Math.max(height(z.left), height(z.right)) + 1;
    y.height = Math.max(height(y.left), height(y.right)) + 1;
    return y;
}

TreeNode insert(TreeNode node, int val) {
    if (node == null) return new TreeNode(val);
    if (val < node.val) node.left = insert(node.left, val);
    else if (val > node.val) node.right = insert(node.right, val);
    else return node;

    node.height = Math.max(height(node.left), height(node.right)) + 1;
    int balance = getBalance(node);

    // LL
    if (balance > 1 && val < node.left.val) return rightRotate(node);
    // RR
    if (balance < -1 && val > node.right.val) return leftRotate(node);
    // LR
    if (balance > 1 && val > node.left.val) {
        node.left = leftRotate(node.left);
        return rightRotate(node);
    }
    // RL
    if (balance < -1 && val < node.right.val) {
        node.right = rightRotate(node.right);
        return leftRotate(node);
    }
    return node;
}
```

### AVL Complexity

| Operation | Time Complexity |
|-----------|----------------|
| Search | O(log n) — guaranteed |
| Insert | O(log n) |
| Delete | O(log n) |
| Space | O(n) |

> AVL trees guarantee **O(log n)** height, eliminating BST's O(n) worst case.

---

## 8. Segment Trees

> **Source:** Pages 166–168

### Concept

A Segment Tree is a tree data structure for **range queries** (sum, min, max) and **point updates** on an array, both in **O(log n)** time.

### When to Use

| Problem | Brute Force | Segment Tree |
|---------|------------|-------------|
| Range sum query | O(n) | **O(log n)** |
| Point update | O(1) | **O(log n)** |
| Combined (Q queries + U updates) | O(Q × n) | **O((Q+U) × log n)** |

### Structure

Each node stores information about a **range** of the array. The root covers the entire array, and children split the range in half.

```
Array: [1, 3, 5, 7, 9, 11]

                   [36]              (sum of 0-5)
                /        \
          [9]               [27]      (sum 0-2, sum 3-5)
         /    \           /     \
      [4]    [5]       [16]    [11]   (sum 0-1, sum 2-2, sum 3-4, sum 5-5)
      / \              / \
    [1] [3]          [7] [9]
```

### Java Implementation

```java
class SegmentTree {
    int[] tree;
    int n;

    SegmentTree(int[] arr) {
        n = arr.length;
        tree = new int[4 * n];
        build(arr, 1, 0, n - 1);
    }

    // Build: O(n)
    void build(int[] arr, int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
            return;
        }
        int mid = (start + end) / 2;
        build(arr, 2 * node, start, mid);
        build(arr, 2 * node + 1, mid + 1, end);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    // Query (range sum): O(log n)
    int query(int node, int start, int end, int l, int r) {
        if (r < start || end < l) return 0;         // No overlap
        if (l <= start && end <= r) return tree[node]; // Complete overlap
        int mid = (start + end) / 2;
        return query(2 * node, start, mid, l, r)
             + query(2 * node + 1, mid + 1, end, l, r);
    }

    // Update (point): O(log n)
    void update(int node, int start, int end, int idx, int val) {
        if (start == end) {
            tree[node] = val;
            return;
        }
        int mid = (start + end) / 2;
        if (idx <= mid) update(2 * node, start, mid, idx, val);
        else update(2 * node + 1, mid + 1, end, idx, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }
}
```

### Complexity

| Operation | Time | Space |
|-----------|------|-------|
| Build | O(n) | O(4n) ≈ O(n) |
| Range Query | O(log n) | — |
| Point Update | O(log n) | — |

---

## 9. Karp-Rabin Algorithm (String Matching)

> **Source:** Page 172

### Concept

Karp-Rabin uses **hashing** to find a pattern in a text. Instead of comparing characters one by one, it compares **hash values** of the pattern and text windows.

### Algorithm

1. Compute hash of the pattern.
2. Compute hash of the first window of text (same length as pattern).
3. Slide the window one character at a time, updating the hash in O(1) using a **rolling hash**.
4. When hashes match, verify with character comparison (to handle collisions).

### Rolling Hash Formula

$$hash = (c_0 \cdot d^{m-1} + c_1 \cdot d^{m-2} + \ldots + c_{m-1} \cdot d^0) \mod q$$

Where:
- $d$ = number of characters in alphabet (e.g., 256)
- $m$ = pattern length
- $q$ = a prime number (for modular arithmetic)

### Sliding the Window

$$hash_{new} = (d \cdot (hash_{old} - c_{old} \cdot d^{m-1}) + c_{new}) \mod q$$

### Java Implementation

```java
public static void rabinKarp(String text, String pattern) {
    int d = 256;     // Number of characters
    int q = 101;     // A prime number
    int m = pattern.length();
    int n = text.length();
    int h = 1;       // d^(m-1) % q

    for (int i = 0; i < m - 1; i++)
        h = (h * d) % q;

    int pHash = 0, tHash = 0;
    for (int i = 0; i < m; i++) {
        pHash = (d * pHash + pattern.charAt(i)) % q;
        tHash = (d * tHash + text.charAt(i)) % q;
    }

    for (int i = 0; i <= n - m; i++) {
        if (pHash == tHash) {
            // Verify character by character
            if (text.substring(i, i + m).equals(pattern))
                System.out.println("Pattern found at index " + i);
        }
        if (i < n - m) {
            tHash = (d * (tHash - text.charAt(i) * h) + text.charAt(i + m)) % q;
            if (tHash < 0) tHash += q;
        }
    }
}
```

### Complexity

| Case | Time |
|------|------|
| **Average** | O(n + m) |
| **Worst** (many hash collisions) | O(n × m) |
| Space | O(1) |

> Karp-Rabin is efficient for **multiple pattern searching** and is the basis for plagiarism detection tools.

---

## 10. Key Takeaways

1. **Binary Trees** are hierarchical — fundamentally different from linear structures.
2. **BST** enables O(log n) search — but degrades to O(n) if unbalanced.
3. **AVL Trees** guarantee O(log n) operations via self-balancing rotations (LL, RR, LR, RL).
4. **Inorder traversal** of BST = sorted output.
5. **Level-order traversal** uses a Queue (BFS pattern).
6. **HashMap** provides O(1) average operations — the most-used data structure in interviews.
7. **Trie** is the go-to for prefix-based operations (autocomplete, dictionary).
8. **Segment Trees** enable O(log n) range queries and point updates — essential for competitive programming.
9. **Karp-Rabin** uses rolling hash for efficient string matching — O(n+m) average.

