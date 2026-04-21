# Stacks & Queues

> **Source:** Handwritten DSA Notes – Pages 141–151

---

## 1. Stack (LIFO)

A Stack is a **Last In, First Out** data structure — like a stack of plates.

```
Push →  │ 30 │ ← Top
        │ 20 │
        │ 10 │ ← Bottom
        Pop removes 30 (top)
```

### Operations — All O(1)

| Operation | Description |
|-----------|-------------|
| `push(item)` | Add to top |
| `pop()` | Remove and return top |
| `peek()` | View top without removing |
| `isEmpty()` | Check if empty |

### Array Implementation

```java
class StackArray {
    int[] arr;
    int top = -1;

    StackArray(int cap) { arr = new int[cap]; }

    void push(int val) { arr[++top] = val; }
    int pop()          { return arr[top--]; }
    int peek()         { return arr[top]; }
    boolean isEmpty()  { return top == -1; }
}
```

### Linked List Implementation

```java
class StackLL {
    Node top;
    class Node { int val; Node next; Node(int v) { val = v; } }

    void push(int val) {
        Node n = new Node(val);
        n.next = top;
        top = n;
    }
    int pop() {
        int val = top.val;
        top = top.next;
        return val;
    }
}
```

### Balanced Parentheses

```java
public static boolean isBalanced(String expr) {
    Stack<Character> stack = new Stack<>();
    for (char c : expr.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') stack.push(c);
        else {
            if (stack.isEmpty()) return false;
            char top = stack.pop();
            if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{'))
                return false;
        }
    }
    return stack.isEmpty();
}
```

### Stack Applications

- Function Call Stack, Undo/Redo, Expression Evaluation, DFS, Backtracking

---

## 2. Queue (FIFO)

A Queue is a **First In, First Out** data structure — like a line of people.

```
Enqueue →  [10 | 20 | 30 | 40]  → Dequeue
           Front            Rear
```

### Operations — All O(1)

| Operation | Description |
|-----------|-------------|
| `enqueue(item)` | Add to rear |
| `dequeue()` | Remove from front |
| `peek()` | View front |
| `isEmpty()` | Check if empty |

### Circular Queue (Array)

```java
class CircularQueue {
    int[] arr; int front = 0, rear = -1, size = 0, cap;

    CircularQueue(int c) { cap = c; arr = new int[c]; }

    void enqueue(int val) {
        rear = (rear + 1) % cap;
        arr[rear] = val;
        size++;
    }
    int dequeue() {
        int val = arr[front];
        front = (front + 1) % cap;
        size--;
        return val;
    }
}
```

### Queue Applications

- BFS, CPU Scheduling, I/O Buffers, Level-order Traversal

---

## 3. Deque & Priority Queue

**Deque** — insert/remove from both ends. **Priority Queue** — serves highest/lowest priority first (heap-based).

```java
// Deque
Deque<Integer> dq = new ArrayDeque<>();
dq.addFirst(10); dq.addLast(20); dq.removeFirst(); dq.removeLast();

// Min-Heap PQ
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(30); pq.offer(10); pq.poll(); // returns 10
```

---

## 4. Summary

| Feature | Stack (LIFO) | Queue (FIFO) |
|---------|-------------|-------------|
| Insert | push (top) | enqueue (rear) |
| Remove | pop (top) | dequeue (front) |
| Use Case | DFS, Undo, Backtracking | BFS, Scheduling |
