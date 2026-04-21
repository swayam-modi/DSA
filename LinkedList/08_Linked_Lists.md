# Linked Lists

> **Source:** Handwritten DSA Notes – Pages 129–140

---

## 1. What is a Linked List?

A Linked List is a **linear data structure** where elements (called **nodes**) are stored in **non-contiguous memory** locations and connected via **pointers**.

### Node Structure

Each node contains:
1. **Data** (the value)
2. **Next pointer** (reference to the next node)

```java
class Node {
    int val;
    Node next;
    
    Node(int val) {
        this.val = val;
        this.next = null;
    }
}
```

### Array vs Linked List

| Feature | Array | Linked List |
|---------|-------|-------------|
| Memory | Contiguous | Non-contiguous |
| Size | Fixed | Dynamic |
| Access by index | O(1) | O(n) |
| Insert at beginning | O(n) | **O(1)** |
| Insert at end | O(1) amortized | O(n) (O(1) with tail) |
| Delete by index | O(n) | O(n) |
| Memory overhead | None | Extra pointer per node |
| Cache performance | Excellent | Poor |

### Visual Representation

```
head
 ↓
[10 | •] → [20 | •] → [30 | •] → [40 | null]
```

---

## 2. Types of Linked Lists

### 2.1 Singly Linked List

Each node points to the **next** node. Traversal is **one-directional** (forward only).

```
head → [A] → [B] → [C] → [D] → null
```

### 2.2 Doubly Linked List

Each node has pointers to **both** the next and previous nodes. Enables **bidirectional** traversal.

```java
class DoublyNode {
    int val;
    DoublyNode prev;
    DoublyNode next;
    
    DoublyNode(int val) {
        this.val = val;
        this.prev = null;
        this.next = null;
    }
}
```

```
null ← [A] ⇆ [B] ⇆ [C] ⇆ [D] → null
```

### 2.3 Circular Linked List

The **last node** points back to the **first node**, forming a cycle.

```
→ [A] → [B] → [C] → [D] ─┐
  ↑                         │
  └─────────────────────────┘
```

### Comparison

| Type | Extra Pointers | Traversal | Insert at Head | Insert at Tail |
|------|----------------|-----------|----------------|----------------|
| Singly | next | Forward | O(1) | O(n) |
| Doubly | next, prev | Both | O(1) | O(1) with tail |
| Circular | next (last→first) | Circular | O(1) | O(1) with tail |

---

## 3. Singly Linked List — Operations

### 3.1 Traversal

```java
public void display() {
    Node current = head;
    while (current != null) {
        System.out.print(current.val + " → ");
        current = current.next;
    }
    System.out.println("null");
}
```

### 3.2 Insert at Head

```java
public void insertAtHead(int val) {
    Node newNode = new Node(val);
    newNode.next = head;
    head = newNode;
}
```

**Time:** O(1)

```
Before: head → [10] → [20] → [30] → null
Insert 5 at head:
After:  head → [5] → [10] → [20] → [30] → null
```

### 3.3 Insert at Tail

```java
public void insertAtTail(int val) {
    Node newNode = new Node(val);
    
    if (head == null) {
        head = newNode;
        return;
    }
    
    Node current = head;
    while (current.next != null) {
        current = current.next;
    }
    current.next = newNode;
}
```

**Time:** O(n) — must traverse to the end

### 3.4 Insert at Index

```java
public void insertAtIndex(int val, int index) {
    if (index == 0) {
        insertAtHead(val);
        return;
    }
    
    Node current = head;
    for (int i = 0; i < index - 1 && current != null; i++) {
        current = current.next;
    }
    
    if (current == null) return; // Index out of bounds
    
    Node newNode = new Node(val);
    newNode.next = current.next;
    current.next = newNode;
}
```

### 3.5 Delete at Head

```java
public int deleteAtHead() {
    if (head == null) return -1;
    int val = head.val;
    head = head.next;
    return val;
}
```

**Time:** O(1)

### 3.6 Delete at Tail

```java
public int deleteAtTail() {
    if (head == null) return -1;
    if (head.next == null) {
        int val = head.val;
        head = null;
        return val;
    }
    
    Node current = head;
    while (current.next.next != null) {
        current = current.next;
    }
    int val = current.next.val;
    current.next = null;
    return val;
}
```

**Time:** O(n)

### 3.7 Search

```java
public boolean search(int target) {
    Node current = head;
    while (current != null) {
        if (current.val == target) return true;
        current = current.next;
    }
    return false;
}
```

**Time:** O(n)

### 3.8 Size

```java
public int size() {
    int count = 0;
    Node current = head;
    while (current != null) {
        count++;
        current = current.next;
    }
    return count;
}
```

---

## 4. Common Linked List Problems

### 4.1 Reverse a Linked List

```java
public Node reverse(Node head) {
    Node prev = null;
    Node current = head;
    
    while (current != null) {
        Node next = current.next;   // Save next
        current.next = prev;         // Reverse pointer
        prev = current;              // Move prev forward
        current = next;              // Move current forward
    }
    return prev; // New head
}
```

**Visual:**
```
Before: [1] → [2] → [3] → null
Step 1: null ← [1]    [2] → [3] → null
Step 2: null ← [1] ← [2]    [3] → null
Step 3: null ← [1] ← [2] ← [3]
After:  [3] → [2] → [1] → null
```

**Time:** O(n), **Space:** O(1)

### 4.2 Detect Cycle (Floyd's Algorithm)

Use **two pointers** — slow (1 step) and fast (2 steps). If they meet, there's a cycle.

```java
public boolean hasCycle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

### 4.3 Find Middle Element

```java
public Node findMiddle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow; // Middle node
}
```

### 4.4 Merge Two Sorted Lists

```java
public Node mergeSorted(Node l1, Node l2) {
    Node dummy = new Node(0);
    Node current = dummy;
    
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            current.next = l1;
            l1 = l1.next;
        } else {
            current.next = l2;
            l2 = l2.next;
        }
        current = current.next;
    }
    
    current.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
```

### 4.5 Reverse Nodes in k-Group (LeetCode 25 — Hard)

> **Source:** Pages 148–149

Reverse the nodes of a linked list `k` at a time.

```java
public Node reverseKGroup(Node head, int k) {
    // Check if there are k nodes remaining
    Node check = head;
    for (int i = 0; i < k; i++) {
        if (check == null) return head;  // Less than k nodes, don't reverse
        check = check.next;
    }
    
    // Reverse k nodes
    Node prev = null, current = head;
    for (int i = 0; i < k; i++) {
        Node next = current.next;
        current.next = prev;
        prev = current;
        current = next;
    }
    
    // head is now the tail of the reversed group
    // current is the head of the remaining list
    head.next = reverseKGroup(current, k);
    return prev;  // prev is the new head
}
```

**Visual (k=3):**
```
Before: [1] → [2] → [3] → [4] → [5] → null
After:  [3] → [2] → [1] → [4] → [5] → null
        (reversed)         (not enough for k=3)
```

**Time:** O(n), **Space:** O(n/k) recursion stack

---

## 5. Operations Complexity Summary

| Operation | Singly | Doubly |
|-----------|--------|--------|
| Insert at head | O(1) | O(1) |
| Insert at tail | O(n) | O(1)* |
| Insert at index | O(n) | O(n) |
| Delete at head | O(1) | O(1) |
| Delete at tail | O(n) | O(1)* |
| Search | O(n) | O(n) |
| Access by index | O(n) | O(n) |

*\* With tail pointer*

---

## 6. Key Takeaways

1. Linked Lists excel at **insertions/deletions** at the beginning — O(1) vs arrays' O(n).
2. Arrays are better for **random access** — O(1) vs linked lists' O(n).
3. **Doubly linked lists** trade extra memory for O(1) delete-at-tail capability.
4. The **two-pointer technique** (slow/fast) solves many linked list problems:
   - Cycle detection, finding middle, nth node from end.
5. **Reversing** a linked list is a fundamental operation — know it in-place.
6. **Reverse k-group** is a classic hard problem combining reversal with recursion.
7. Linked lists are the building blocks for **Stacks**, **Queues**, and **Hash Maps**.

