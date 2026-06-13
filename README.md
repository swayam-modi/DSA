# 📚 Data Structures & Algorithms in Java

A comprehensive collection of **DSA implementations** in Java with detailed documentation for each topic.

---

## 📁 Repository Structure

| Folder | Topics | Docs | Code |
|--------|--------|------|------|
| [**ArrayList**](./ArrayList/) | Dynamic arrays, 2D lists | ✅ | — |
| [**Searching**](./Searching/) | Linear Search, Binary Search, 2D Matrix Search | ✅ | — |
| [**Sorting**](./Sorting/) | Bubble, Selection, Insertion, Merge, Quick, Cyclic Sort | ✅ | — |
| [**Strings**](./Strings/) | String Pool, Immutability, StringBuilder | ✅ | — |
| [**Recursion**](./Recursion/) | Recursion, Big O, Master Theorem, Akra-Bazzi | ✅ | — |
| [**BitManipulation**](./BitManipulation/) | Bitwise operators, bit hacks, two's complement | ✅ | — |
| [**Mathematics**](./Mathematics/) | GCD/LCM, Sieve, Extended Euclidean, Bezout's Identity | ✅ | — |
| [**Backtracking**](./Backtracking/) | Subsets, Permutations, N-Queens, Sudoku Solver | ✅ | — |
| [**LinkedList**](./LinkedList/) | Singly, Doubly, Circular LL, Reverse k-Group | ✅ | ✅ |
| [**StacksAndQueue**](./StacksAndQueue/) | Stack, Queue, Circular Queue, Dynamic variants | ✅ | ✅ |
| [**BinaryTree**](./BinaryTree/) | Binary Tree, BST, AVL Tree, Segment Tree | ✅ | ✅ |
| [**Heap**](./Heap/) | Heap, Heap Sort, Priority Queue | ✅ | ✅ |
| [**HashMap**](./HashMap/) | Custom HashMap, Hashing, Rehashing | ✅ | ✅ |
| [**Trie**](./Trie/) | Prefix Tree, Insert, Search, StartsWith | ✅ | ✅ |
| [**GraphTheory**](./GraphTheory/) | BFS, DFS, Dijkstra, Bellman-Ford, Prim's MST, Topological Sort, Tarjan's, SCC | ✅ | ✅ |
| [**huffman coding**](./huffman%20coding/) | Huffman Tree, Encoding, Decoding | ✅ | ✅ |
| [**OOP**](./OOP/) | Classes, Inheritance, Polymorphism, Interfaces, Enums | ✅ | — |
| [**SQL**](./SQL/) | Relational DB, DDL/DML/DQL, Joins, Subqueries, Indexes, Transactions, Normalization, Design | ✅ | — |

---

## 🧠 Topics Covered

### Fundamentals
- Arrays & ArrayLists
- Searching (Linear, Binary)
- Sorting (Bubble, Selection, Insertion, Merge, Quick, Cyclic)
- Strings & StringBuilder
- Bit Manipulation

### Core Concepts
- Recursion & Complexity Analysis
- Mathematics (GCD, Primes, Sieve, Modular Arithmetic)
- Backtracking (Subsets, Permutations, N-Queens, Sudoku)

### Data Structures
- Linked Lists (Singly, Doubly, Circular)
- Stacks & Queues
- Binary Trees, BST, AVL Trees
- Heaps & Priority Queues
- HashMaps (Custom Implementation)
- Tries (Prefix Trees)
- Segment Trees

### Advanced Algorithms
- **Graph Theory:** BFS, DFS, Dijkstra, Bellman-Ford, Prim's MST, Topological Sort
- **Advanced Graph:** Tarjan's Bridges, Articulation Points, Kosaraju's SCC
- **Huffman Coding:** Lossless compression with prefix codes

### Object-Oriented Programming
- Classes & Objects, Constructors, `this` & `super`
- Inheritance, Polymorphism, Abstraction
- Interfaces, Access Modifiers, Enumerations
- `static`, `final`, Packages

### SQL & Relational Databases
- Database fundamentals, RDBMS vs NoSQL, relational concepts
- DDL / DML / DQL — CREATE, INSERT, SELECT, UPDATE, DELETE
- Joins (INNER, LEFT, RIGHT, FULL OUTER, CROSS, SELF)
- Subqueries, CTEs, Window Functions, Set Operators
- Indexing (B-Tree, Composite, Partial, Covering) & Query Optimization
- Views, Materialized Views, Stored Procedures, Triggers, UDFs
- Transactions, ACID, Isolation Levels, Deadlocks
- Normalization (1NF → BCNF), Database Design, ER Diagrams
- JSON Processing, Full-Text Search, Partitioning, Replication
- Security, Backup & Recovery, Interview Preparation

---

## 🚀 How to Use

Each folder contains:
- **`.md` file** — Detailed notes with explanations, code examples, visual walkthroughs, and complexity analysis
- **`Implementation/`** — Java source code (where available)

```bash
# Navigate to a topic
cd GraphTheory/

# Read the documentation
cat 11_Graph_Theory.md

# Run the code
javac Implementation/*.java
java GraphTheory.Main
```

---

## 📊 Complexity Cheat Sheet

| Data Structure | Access | Search | Insert | Delete |
|---------------|--------|--------|--------|--------|
| Array | O(1) | O(n) | O(n) | O(n) |
| Linked List | O(n) | O(n) | O(1) | O(1) |
| Stack/Queue | O(n) | O(n) | O(1) | O(1) |
| HashMap | — | O(1) | O(1) | O(1) |
| BST (balanced) | O(log n) | O(log n) | O(log n) | O(log n) |
| Heap | — | O(n) | O(log n) | O(log n) |
| Trie | — | O(L) | O(L) | O(L) |

| Algorithm | Best | Average | Worst |
|-----------|------|---------|-------|
| Bubble Sort | O(n) | O(n²) | O(n²) |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) |
| Quick Sort | O(n log n) | O(n log n) | O(n²) |
| Cyclic Sort | O(n) | O(n) | O(n) |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) |
| Dijkstra | — | O((V+E) log V) | — |
| Bellman-Ford | — | O(V×E) | — |
| BFS/DFS | — | O(V+E) | — |

---

## 📝 Sources

- Handwritten DSA Notes (181 pages)
- Handwritten OOP in Java Notes (35 pages)
- Custom Java implementations

---

*Made with ❤️ by Swayam*
