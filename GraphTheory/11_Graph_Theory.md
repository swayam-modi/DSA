# Graph Theory

> Covers Graph representation, traversals, shortest paths, MST, and advanced algorithms.

---

## 1. Graph Representation

### Adjacency List (Primary)


Each vertex stores a list of its edges. Memory-efficient for sparse graphs.

```java
private class Edge {
    int src;
    int dst;
    Edge(int src, int dst) {
        this.src = src;
        this.dst = dst;
    }
}

private ArrayList<Edge>[] vertices;

public Graph(int vertices) {
    this.vertices = new ArrayList[vertices];
    for (int i = 0; i < vertices; i++) {
        this.vertices[i] = new ArrayList<>();
    }
}

public void addStructure(int src, int dst) {
    this.vertices[src].add(new Edge(src, dst));
}
```

### Adjacency Matrix


A 2D array where `graph[i][j] = 1` (or weight) means edge from `i` to `j`. Good for dense graphs.

```java
private int[][] graph;

public void addEdge(int src, int dst) {
    this.graph[src][dst] = 1;
}

public void addEdge(int src, int dst, int weight) {
    this.graph[src][dst] = weight;
}
```

### Weighted Graph (Adjacency List)


Extends the adjacency list to store edge weights — needed for Dijkstra, Bellman-Ford, Prim's.

```java
private class Edge {
    int src, dst, weight;
    Edge(int src, int dst, int weight) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
    }
}
```

### Comparison

| Feature | Adjacency List | Adjacency Matrix |
|---------|---------------|-----------------|
| Space | O(V + E) | O(V²) |
| Add edge | O(1) | O(1) |
| Check edge | O(degree) | O(1) |
| Best for | Sparse graphs | Dense graphs |

---

## 2. Graph Traversals

### 2.1 Breadth-First Search (BFS)


Explores level by level using a **Queue**. Handles disconnected components.

```java
public void bfs() {
    boolean[] visited = new boolean[this.vertices.length];
    // Handle disconnected graphs
    for (int i = 0; i < visited.length; i++) {
        if (!visited[i]) {
            bfs(visited, i);
        }
    }
}

private void bfs(boolean[] visited, int vertex) {
    Queue<Integer> queue = new LinkedList<>();
    queue.offer(vertex);

    while (!queue.isEmpty()) {
        int v = queue.poll();
        if (!visited[v]) {
            visited[v] = true;
            System.out.print(v + " ");
            for (Edge edge : this.vertices[v]) {
                queue.offer(edge.dst);
            }
        }
    }
}
```

**Time:** O(V + E), **Space:** O(V)

### 2.2 Depth-First Search (DFS)


Explores as deep as possible using **recursion** (implicit stack).

```java
public void dfs() {
    dfs(new boolean[this.vertices.length], 0);
}

private void dfs(boolean[] vertex, int i) {
    vertex[i] = true;
    System.out.print(i + " ");

    for (Edge edge : this.vertices[i]) {
        if (!vertex[edge.dst]) {
            dfs(vertex, edge.dst);
        }
    }
}
```

**Time:** O(V + E), **Space:** O(V)

### Visual Comparison

```
Graph:    0 --- 1
         /|     |
        3 |     2
         \|
          4

BFS (from 0): 0 → 1, 3, 4 → 2       (level by level)
DFS (from 0): 0 → 1 → 2 → 3 → 4     (go deep first)
```

---

## 3. All Possible Paths


Find ALL paths from source to destination using **backtracking DFS**.

```java
public void allPossiblePaths(int src, int dst) {
    boolean[] visited = new boolean[vertices.length];
    visited[src] = true;
    allPossiblePaths("" + src, visited, src, dst);
}

private void allPossiblePaths(String s, boolean[] visited, int src, int dst) {
    if (src == dst) {
        System.out.println(s);
        return;
    }
    for (Edge edge : this.vertices[src]) {
        if (!visited[edge.dst]) {
            visited[edge.dst] = true;
            allPossiblePaths(s + " -> " + edge.dst, visited, edge.dst, dst);
            visited[edge.dst] = false;  // Backtrack
        }
    }
}
```

> **Key:** String immutability is used instead of StringBuilder — each path gets its own copy naturally.

---

## 4. Cycle Detection

### 4.1 Undirected Graph


A cycle exists if we find a visited neighbor that is **not the parent** of the current node.

```java
private boolean isCyclicHelper(boolean[] visited, int src, int pre) {
    for (Edge edge : this.vertices[src]) {
        if (visited[edge.dst] && edge.dst != pre) {
            return true;  // Cycle found!
        }
        if (!visited[edge.dst]) {
            visited[edge.dst] = true;
            if (isCyclicHelper(visited, edge.dst, src)) {
                return true;
            }
            visited[edge.dst] = false;
        }
    }
    return false;
}
```

### 4.2 Directed Graph


Uses **recursion stack tracking** — a cycle exists if we encounter a node currently in the recursion stack.

```java
private boolean isCyclicDgHelper(int src, boolean[] visited) {
    for (Edge edge : this.vertices[src]) {
        if (visited[edge.dst]) {
            return true;
        } else {
            visited[edge.dst] = true;
            if (isCyclicDgHelper(edge.dst, visited)) {
                return true;
            }
            visited[edge.dst] = false;  // Remove from recursion stack
        }
    }
    return false;
}
```

---

## 5. Topological Sort


Linear ordering of vertices in a **DAG** (Directed Acyclic Graph) such that for every edge `u → v`, `u` comes before `v`.

```java
public void topologicalSort() {
    Stack<Integer> stack = new Stack<>();
    boolean[] visited = new boolean[this.vertices.length];

    for (int i = 0; i < visited.length; i++) {
        if (!visited[i]) {
            topHelper(visited, stack, i);
        }
    }

    while (!stack.isEmpty()) {
        System.out.print(stack.pop() + " ");
    }
}

private void topHelper(boolean[] visited, Stack<Integer> stack, int node) {
    visited[node] = true;
    for (Edge edge : this.vertices[node]) {
        if (!visited[edge.dst]) {
            topHelper(visited, stack, edge.dst);
        }
    }
    stack.push(node);  // Push AFTER all neighbors processed
}
```

**Time:** O(V + E)

**Applications:** Task scheduling, build systems, course prerequisites.

---

## 6. Shortest Path Algorithms

### 6.1 Dijkstra's Algorithm


Finds shortest paths from a source to **all** vertices. Uses a **Priority Queue** (Min-Heap). Works only with **non-negative weights**.

```java
public DijkstraAlgo(ArrayList<int[]>[] graph, int srcNode) {
    this.distance = new int[graph.length];
    queue = new PriorityQueue<>((a, b) -> this.distance[a] - this.distance[b]);
    this.visited = new boolean[graph.length];
    this.allShortestPaths = new StringBuilder[graph.length];

    for (int i = 0; i < distance.length; i++) {
        allShortestPaths[i] = new StringBuilder();
        distance[i] = Integer.MAX_VALUE;
    }
    distance[srcNode] = 0;
    queue.offer(srcNode);
    allShortestPaths[srcNode].append(srcNode);

    while (!queue.isEmpty()) {
        int node = queue.poll();
        if (visited[node]) continue;
        visited[node] = true;

        for (int i = 0; i < graph[node].size(); i++) {
            int neighbour = graph[node].get(i)[0];
            int curDistance = graph[node].get(i)[1];

            if (!visited[neighbour]) {
                int current = distance[neighbour];
                distance[neighbour] = Math.min(
                    distance[node] + curDistance, distance[neighbour]
                );
                if (current != distance[neighbour]) {
                    // Update shortest path
                    allShortestPaths[neighbour]
                        .delete(0, allShortestPaths[neighbour].length())
                        .append(allShortestPaths[node])
                        .append(" -> ").append(neighbour);
                }
                queue.offer(neighbour);
            }
        }
    }
}
```

| Aspect | Value |
|--------|-------|
| **Time** | O((V + E) log V) with priority queue |
| **Space** | O(V) |
| **Limitation** | No negative weights |

### 6.2 Bellman-Ford Algorithm


Handles **negative edge weights**. Dynamic programming approach — relax all edges `V-1` times.

```java
BellmanFord(ArrayList<int[]>[] graph, int src) {
    int size = graph.length;
    int[] distance = new int[size];
    Arrays.fill(distance, Integer.MAX_VALUE);
    distance[src] = 0;

    for (int i = 0; i < size - 1; i++) {
        boolean flag = false;
        for (int j = 0; j < size; j++) {
            for (int[] edge : graph[j]) {
                if (distance[j] != Integer.MAX_VALUE
                    && distance[j] + edge[1] < distance[edge[0]]) {
                    flag = true;
                    distance[edge[0]] = distance[j] + edge[1];
                }
            }
        }
        if (!flag) break;  // Early termination — no updates
    }
}
```

| Aspect | Value |
|--------|-------|
| **Time** | O(V × E) |
| **Space** | O(V) |
| **Advantage** | Works with negative weights |
| **Limitation** | Fails with negative weight cycles |

### Dijkstra vs Bellman-Ford

| Feature | Dijkstra | Bellman-Ford |
|---------|----------|-------------|
| Negative weights | ❌ | ✅ |
| Time | O((V+E) log V) | O(V × E) |
| Approach | Greedy | Dynamic Programming |
| Detect negative cycles | ❌ | ✅ |

---

## 7. Minimum Spanning Tree (MST) — Prim's Algorithm


Finds the MST — subset of edges connecting all vertices with **minimum total weight**. Uses a **Priority Queue** (greedy approach).

```java
PrimsAlgo(ArrayList<int[]>[] graph) {
    PriorityQueue<Edge> heap = new PriorityQueue<>();
    boolean[] visited = new boolean[graph.length];
    int mst = 0;

    heap.offer(new Edge(0, 0));

    while (!heap.isEmpty()) {
        Edge current = heap.poll();
        if (visited[current.dst]) continue;

        visited[current.dst] = true;
        mst += current.weight;

        for (int[] edge : graph[current.dst]) {
            if (!visited[edge[0]]) {
                heap.offer(new Edge(edge[0], edge[1]));
            }
        }
    }
    System.out.println("MST weight: " + mst);
}

private class Edge implements Comparable<Edge> {
    int dst, weight;
    @Override
    public int compareTo(Edge e) { return this.weight - e.weight; }
}
```

**Time:** O(E log V), **Space:** O(V)

---

## 8. Advanced Graph Algorithms

### 8.1 Tarjan's Algorithm — Finding Bridges


A **bridge** is an edge whose removal disconnects the graph. Uses DFS with **discovery time** and **low-link values**.

```java
private void dfs(ArrayList<int[]>[] graph, int[] disTime, int[] lowTime,
                 boolean[] visited, int parent, int node) {
    visited[node] = true;

    for (int[] edges : graph[node]) {
        if (edges[1] == parent) continue;

        if (!visited[edges[1]]) {
            disTime[edges[1]] = disTime[node] + 1;
            lowTime[edges[1]] = disTime[edges[1]];
            dfs(graph, disTime, lowTime, visited, node, edges[1]);
            lowTime[node] = Math.min(lowTime[node], lowTime[edges[1]]);

            if (disTime[node] < lowTime[edges[1]]) {
                System.out.println(node + " --- " + edges[1]);  // Bridge!
            }
        } else {
            lowTime[node] = Math.min(disTime[edges[1]], lowTime[node]);
        }
    }
}
```

**Bridge condition:** `disTime[u] < lowTime[v]` — means no back edge from `v`'s subtree can reach `u` or above.

### 8.2 Articulation Points


An **articulation point** is a vertex whose removal disconnects the graph. Two rules:

1. **Non-root:** `u` is articulation point if `dt[u] ≤ low[v]` for any child `v`.
2. **Root:** Root is articulation point if it has **> 1 children** in DFS tree.

```java
// Non-root check
if (dt[current] <= low[neigh] && parent != -1) {
    ap[current] = true;
}
// Root check
if (parent == -1 && children > 1) {
    ap[current] = true;
}
```

### 8.3 Strongly Connected Components (Kosaraju's Algorithm)


Finds all **SCCs** in a directed graph. An SCC is a maximal set of vertices where every vertex is reachable from every other.

**Three Steps:**
1. **Topological sort** the original graph.
2. **Transpose** the graph (reverse all edges).
3. **DFS** on the transpose in topological order — each DFS tree is an SCC.

```java
SCC(ArrayList<int[]>[] graph) {
    // Step 1: Topological sort
    Stack<Integer> topologicalSort = topSort(0, graph, new Stack<>());

    // Step 2: Transpose the graph (reverse edges)
    ArrayList<int[]>[] transposeGraph = getTranspose(graph);

    // Step 3: DFS on transpose in topological order
    boolean[] visited = new boolean[graph.length];
    while (!topologicalSort.isEmpty()) {
        int node = topologicalSort.pop();
        if (!visited[node]) {
            reversedDfs(transposeGraph, visited, node);  // One SCC
            System.out.println();
        }
    }
}
```

**Time:** O(V + E)

---

## 9. Algorithms Summary

| Algorithm | Purpose | Time | Works With |
|-----------|---------|------|------------|
| **BFS** | Traversal, shortest path (unweighted) | O(V+E) | All graphs |
| **DFS** | Traversal, cycle detection, topo sort | O(V+E) | All graphs |
| **Dijkstra** | Shortest path (weighted) | O((V+E) log V) | Non-negative weights |
| **Bellman-Ford** | Shortest path (negative weights) | O(V×E) | Negative weights |
| **Prim's** | Minimum Spanning Tree | O(E log V) | Weighted, undirected |
| **Topological Sort** | Linear ordering of DAG | O(V+E) | DAG only |
| **Tarjan's** | Finding bridges | O(V+E) | Undirected |
| **Articulation Points** | Critical vertices | O(V+E) | Undirected |
| **Kosaraju's (SCC)** | Strongly connected components | O(V+E) | Directed |

---

## 10. Key Takeaways

1. **Adjacency List** is preferred for sparse graphs — O(V+E) space vs O(V²) for matrix.
2. **BFS** uses Queue, **DFS** uses Stack/recursion — both are O(V+E).
3. **Dijkstra** is greedy with a PriorityQueue — fails with negative weights.
4. **Bellman-Ford** handles negatives with DP — relax all edges V-1 times.
5. **Prim's** builds MST greedily — always pick the cheapest edge to unvisited node.
6. **Topological Sort** is essential for DAGs — push to stack AFTER processing all neighbors.
7. **Tarjan's** uses discovery/low-link times for bridges and articulation points.
8. **Kosaraju's** finds SCCs in 3 passes — topSort → transpose → DFS.
