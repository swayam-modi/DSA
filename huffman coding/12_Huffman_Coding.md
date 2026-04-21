# Huffman Coding

> A lossless data compression algorithm using variable-length prefix codes.

---

## 1. What is Huffman Coding?

Huffman Coding is a **greedy algorithm** for lossless data compression. It assigns **shorter bit codes** to frequently occurring characters and **longer codes** to rare characters, minimizing total bits.

### Key Idea

```
Standard ASCII: Each char = 8 bits (fixed)
"aaaabc" = 6 × 8 = 48 bits

Huffman: Variable-length codes
'a' → 0        (1 bit, most frequent)
'b' → 10       (2 bits)
'c' → 11       (2 bits)
"aaaabc" = 4×1 + 1×2 + 1×2 = 8 bits → 83% compression!
```

---

## 2. Algorithm

### Step 1: Build Frequency Table

Count how often each character appears.

### Step 2: Build Huffman Tree

1. Create a **leaf node** for each character with its frequency.
2. Insert all nodes into a **Min-Heap** (Priority Queue).
3. While heap has more than 1 node:
   - Extract the **two smallest** nodes.
   - Create a new internal node with their sum as frequency.
   - Set the two extracted nodes as its left and right children.
   - Insert the new node back into the heap.
4. The remaining node is the **root** of the Huffman tree.

### Step 3: Generate Codes

Traverse the tree:
- Go **left** → append `0`
- Go **right** → append `1`
- At a **leaf** → the accumulated bits are that character's code.

---

## 3. Implementation


### TreeNode Structure

```java
private class TreeNode implements Comparable<TreeNode> {
    TreeNode left, right;
    char data;
    int val;       // frequency

    public TreeNode(char data, int val) {
        this.data = data;
        this.val = val;
    }

    @Override
    public int compareTo(TreeNode node) {
        return this.val - node.val;  // Min-Heap by frequency
    }
}
```

### Building the Huffman Tree

```java
public Huffman(String str) {
    // Step 1: Frequency table
    HashMap<Character, Integer> freq = new HashMap<>();
    for (int i = 0; i < str.length(); i++) {
        freq.put(str.charAt(i), freq.getOrDefault(str.charAt(i), 0) + 1);
    }

    // Step 2: Build tree using Min-Heap
    PriorityQueue<TreeNode> minHeap = new PriorityQueue<>();
    for (char key : freq.keySet()) {
        minHeap.offer(new TreeNode(key, freq.get(key)));
    }

    while (minHeap.size() > 1) {
        TreeNode leftNode = minHeap.poll();   // Smallest
        TreeNode rightNode = minHeap.poll();  // Second smallest

        TreeNode node = new TreeNode('#', leftNode.val + rightNode.val);
        node.setChildren(leftNode, rightNode);
        minHeap.offer(node);
    }

    this.root = minHeap.poll();  // Root of Huffman tree

    // Step 3: Generate encoding table
    createEncodeTable(root, new ArrayList<>(), freq);
}
```

### Encoding

```java
private int createEncodeTable(TreeNode node, ArrayList<Boolean> list,
                               HashMap<Character,Integer> freq) {
    int size = 0;
    if (node == null) return size;

    list.add(false);  // Go left → 0
    size += createEncodeTable(node.left, list, freq);
    list.removeLast();

    list.add(true);   // Go right → 1
    size += createEncodeTable(node.right, list, freq);
    list.removeLast();

    if (node.left == null && node.right == null) {
        // Leaf node — store the code
        size += freq.get(node.data) * list.size();
        encodedTable.put(node.data, new ArrayList<>(list));
    }
    return size;
}
```

### Decoding

```java
private String getDecode(TreeNode root, int idx) {
    StringBuilder sb = new StringBuilder();
    while (idx < encodedData.length) {
        TreeNode node = root;
        // Walk the tree based on bits
        while (node.left != null && node.right != null) {
            node = encodedData[idx++] ? node.right : node.left;
        }
        sb.append(node.data);  // Leaf = character found
    }
    return sb.toString();
}
```

---

## 4. Visual Walkthrough

```
Input: "swayam"
Frequencies: s=1, w=1, y=1, m=1, a=2

Step 1: Min-Heap → [s:1, w:1, y:1, m:1, a:2]

Step 2: Build tree:
  Extract s(1), w(1) → #(2) with children s, w
  Extract y(1), m(1) → #(2) with children y, m
  Extract a(2), #(2) → #(4) with children a, #(y,m)
  Extract #(2), #(4) → #(6) with children #(s,w), #(a,y,m)

Step 3: Huffman Tree:
           #[6]
          /    \
       #[2]    #[4]
       / \     / \
     s[1] w[1] a[2] #[2]
                    / \
                  y[1] m[1]

Codes:
  s → 00
  w → 01
  a → 10
  y → 110
  m → 111

Encoded "swayam":
  00 01 10 110 10 111 = 14 bits (vs 48 bits ASCII = 71% savings)
```

---

## 5. Properties

| Property | Value |
|----------|-------|
| **Type** | Greedy algorithm |
| **Compression** | Lossless (no data loss) |
| **Prefix-free** | ✅ No code is prefix of another |
| **Optimal** | ✅ Among all prefix codes |
| **Build time** | O(n log n) where n = unique chars |
| **Encode time** | O(L) where L = string length |
| **Decode time** | O(L × h) where h = tree height |

> **Prefix-free property** ensures unambiguous decoding — you can always tell where one code ends and the next begins.

---

## 6. Applications

| Application | How Huffman is Used |
|-------------|-------------------|
| **ZIP/GZIP** | Core compression algorithm |
| **DEFLATE** | Combined with LZ77 |
| **JPEG** | Entropy coding step |
| **MP3** | Encoding frequency data |
| **HTTP/2** | HPACK header compression |

---

## 7. Key Takeaways

1. Huffman Coding is **greedy** — always merge the two least frequent nodes.
2. It produces an **optimal prefix-free** code — no code is a prefix of another.
3. Uses a **Min-Heap** (PriorityQueue) to efficiently find minimum frequency nodes.
4. The Huffman tree is a **full binary tree** — every internal node has exactly 2 children.
5. More frequent characters → **shorter codes** → better compression.
6. Decoding walks the tree from root to leaf using the bit stream.
7. Real-world usage: ZIP, JPEG, MP3, and network protocols.
