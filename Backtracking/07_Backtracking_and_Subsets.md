# Backtracking & Subsets

> **Source:** Handwritten DSA Notes – Pages 101–128

---

## 1. What is Backtracking?

Backtracking is a **systematic trial-and-error** approach to problem solving. It builds solutions **incrementally**, abandoning a candidate ("backtracking") as soon as it determines the candidate cannot lead to a valid solution.

### Core Idea

```
1. Choose   → Make a decision (add element, move in direction)
2. Explore  → Recurse with the choice
3. Un-choose → Undo the decision (backtrack) and try the next option
```

### General Template

```java
void backtrack(state, choices) {
    if (isGoal(state)) {
        recordSolution(state);
        return;
    }
    
    for (choice in choices) {
        if (isValid(choice)) {
            makeChoice(choice);          // Choose
            backtrack(newState, remaining);  // Explore
            undoChoice(choice);          // Un-choose (Backtrack)
        }
    }
}
```

---

## 2. Subsets (Power Set)

### Problem

Given an array/string, find **all possible subsets** (the power set).

For `[1, 2, 3]`, subsets are: `[], [1], [2], [3], [1,2], [1,3], [2,3], [1,2,3]`

Total subsets = $2^n$ (each element is either included or excluded).

### Approach 1: Recursive (Include/Exclude)

```java
public static void subsets(int[] arr, List<Integer> current, int index) {
    if (index == arr.length) {
        System.out.println(current);
        return;
    }
    
    // Include current element
    current.add(arr[index]);
    subsets(arr, current, index + 1);
    
    // Exclude current element (backtrack)
    current.remove(current.size() - 1);
    subsets(arr, current, index + 1);
}
```

### Recursion Tree

```
                        []
                      /    \
                   [1]      []
                  /   \    /   \
              [1,2]  [1] [2]   []
              / \   / \  / \  / \
         [1,2,3][1,2][1,3][1][2,3][2][3][]
```

### Approach 2: Iterative (Bit Masking)

Use numbers from `0` to `2^n - 1` as bitmasks:

```java
public static List<List<Integer>> subsets(int[] arr) {
    List<List<Integer>> result = new ArrayList<>();
    int n = arr.length;
    
    for (int mask = 0; mask < (1 << n); mask++) {
        List<Integer> subset = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) != 0) {
                subset.add(arr[i]);
            }
        }
        result.add(subset);
    }
    return result;
}
```

**Example for [1, 2, 3]:**

| mask (decimal) | mask (binary) | Subset |
|----------------|---------------|--------|
| 0 | 000 | [] |
| 1 | 001 | [1] |
| 2 | 010 | [2] |
| 3 | 011 | [1, 2] |
| 4 | 100 | [3] |
| 5 | 101 | [1, 3] |
| 6 | 110 | [2, 3] |
| 7 | 111 | [1, 2, 3] |

---

## 3. Subsets with Duplicates

### Problem

Given an array with duplicates, find all **unique** subsets.

### Strategy

1. **Sort** the array first.
2. When iterating, **skip duplicate elements** at the same level of recursion.

```java
public static void subsetsWithDup(int[] arr, List<Integer> current, int index, List<List<Integer>> result) {
    result.add(new ArrayList<>(current));
    
    for (int i = index; i < arr.length; i++) {
        // Skip duplicates at the same recursion level
        if (i > index && arr[i] == arr[i - 1]) continue;
        
        current.add(arr[i]);
        subsetsWithDup(arr, current, i + 1, result);
        current.remove(current.size() - 1);
    }
}
```

**Usage:**
```java
Arrays.sort(arr);  // MUST sort first
subsetsWithDup(arr, new ArrayList<>(), 0, result);
```

---

## 4. Permutations

### Problem

Find **all arrangements** of the elements. For `[1, 2, 3]`, there are `3! = 6` permutations.

### Approach: Swap-based

```java
public static void permutations(int[] arr, int index) {
    if (index == arr.length) {
        System.out.println(Arrays.toString(arr));
        return;
    }
    
    for (int i = index; i < arr.length; i++) {
        swap(arr, index, i);            // Choose
        permutations(arr, index + 1);   // Explore
        swap(arr, index, i);            // Un-choose (Backtrack)
    }
}

private static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
}
```

### Recursion Tree for [1, 2, 3]

```
                    [1, 2, 3]
                  /     |      \
           [1,2,3]   [2,1,3]   [3,2,1]
            / \       / \        / \
      [1,2,3][1,3,2][2,1,3][2,3,1][3,2,1][3,1,2]
```

### Complexity

- **Time:** O(n × n!) — n! permutations, each takes O(n) to print.
- **Space:** O(n) — recursion depth.

---

## 5. Maze Problems

### Problem

Given an `m × n` grid, find all paths from top-left `(0,0)` to bottom-right `(m-1, n-1)`.

### Allowed Moves

| Direction | Row Change | Col Change |
|-----------|-----------|------------|
| Right | 0 | +1 |
| Down | +1 | 0 |
| Diagonal | +1 | +1 |

### Basic Implementation (Right + Down)

```java
public static void mazePaths(int row, int col, int m, int n, String path) {
    if (row == m - 1 && col == n - 1) {
        System.out.println(path);
        return;
    }
    
    // Move Right
    if (col < n - 1) {
        mazePaths(row, col + 1, m, n, path + "R");
    }
    
    // Move Down
    if (row < m - 1) {
        mazePaths(row + 1, col, m, n, path + "D");
    }
}
```

### With Diagonal Movement

```java
public static void mazePaths(int row, int col, int m, int n, String path) {
    if (row == m - 1 && col == n - 1) {
        System.out.println(path);
        return;
    }
    
    if (col < n - 1)                     mazePaths(row, col + 1, m, n, path + "R");
    if (row < m - 1)                     mazePaths(row + 1, col, m, n, path + "D");
    if (row < m - 1 && col < n - 1)      mazePaths(row + 1, col + 1, m, n, path + "\\");
}
```

### Maze with Obstacles

Add a `boolean[][] visited` or check `grid[row][col] != 0`:

```java
public static void mazeWithObstacles(int[][] grid, int row, int col, String path, boolean[][] visited) {
    int m = grid.length, n = grid[0].length;
    
    if (row == m - 1 && col == n - 1) {
        System.out.println(path);
        return;
    }
    
    if (row < 0 || col < 0 || row >= m || col >= n) return;
    if (grid[row][col] == 0 || visited[row][col]) return;
    
    visited[row][col] = true;
    
    mazeWithObstacles(grid, row, col + 1, path + "R", visited);  // Right
    mazeWithObstacles(grid, row + 1, col, path + "D", visited);  // Down
    mazeWithObstacles(grid, row, col - 1, path + "L", visited);  // Left
    mazeWithObstacles(grid, row - 1, col, path + "U", visited);  // Up
    
    visited[row][col] = false;  // Backtrack
}
```

### Maze Visualization (3×3 grid)

```
(0,0) → (0,1) → (0,2)
  ↓                ↓
(1,0)   (1,1)   (1,2)
  ↓                ↓
(2,0) → (2,1) → (2,2)  ← Goal

Paths: RRDD, RDRD, RDDR, DRRD, DRDR, DDRR
```

---

## 6. N-Queens Problem

### Problem

Place N queens on an N×N chessboard so that **no two queens attack each other** (no shared row, column, or diagonal).

### Strategy

Place queens **one row at a time**. For each row, try each column. If safe, place the queen and recurse to the next row. If no column works, **backtrack**.

### Safety Check

A position `(row, col)` is safe if no queen exists in:
- Same column (above)
- Upper-left diagonal
- Upper-right diagonal

```java
public static boolean isSafe(char[][] board, int row, int col, int n) {
    // Check column above
    for (int i = 0; i < row; i++)
        if (board[i][col] == 'Q') return false;
    
    // Check upper-left diagonal
    for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--)
        if (board[i][j] == 'Q') return false;
    
    // Check upper-right diagonal
    for (int i = row - 1, j = col + 1; i >= 0 && j < n; i--, j++)
        if (board[i][j] == 'Q') return false;
    
    return true;
}
```

### Solution

```java
public static void solveNQueens(char[][] board, int row, int n) {
    if (row == n) {
        printBoard(board, n);
        return;
    }
    
    for (int col = 0; col < n; col++) {
        if (isSafe(board, row, col, n)) {
            board[row][col] = 'Q';           // Place queen
            solveNQueens(board, row + 1, n);  // Recurse
            board[row][col] = '.';           // Backtrack
        }
    }
}
```

### 4-Queens Solution

```
Solution 1:          Solution 2:
. Q . .              . . Q .
. . . Q              Q . . .
Q . . .              . . . Q
. . Q .              . Q . .
```

**Complexity:** O(n!) in worst case (significantly pruned by constraints).

---

## 7. Sudoku Solver

### Strategy

1. Find an empty cell.
2. Try digits 1–9.
3. Check if the digit is valid (row, column, 3×3 box).
4. If valid, place and recurse.
5. If no digit works, **backtrack**.

```java
public static boolean solveSudoku(int[][] board) {
    for (int row = 0; row < 9; row++) {
        for (int col = 0; col < 9; col++) {
            if (board[row][col] == 0) {
                for (int num = 1; num <= 9; num++) {
                    if (isValid(board, row, col, num)) {
                        board[row][col] = num;
                        if (solveSudoku(board)) return true;
                        board[row][col] = 0;  // Backtrack
                    }
                }
                return false;  // No valid number — dead end
            }
        }
    }
    return true;  // All cells filled
}
```

---

## 8. Backtracking vs Other Approaches

| Approach | Description | When to Use |
|----------|-------------|-------------|
| **Brute Force** | Try all possibilities | Small input, no constraints |
| **Backtracking** | Prune invalid paths early | Constraint satisfaction |
| **Dynamic Programming** | Overlapping subproblems | Optimization problems |
| **Greedy** | Locally optimal choice | When greedy works globally |

---

## 9. Key Takeaways

1. Backtracking follows the **Choose → Explore → Un-choose** pattern.
2. **Subsets** have 2^n possibilities; **permutations** have n!.
3. **Sort first** to handle duplicates in subsets/combinations.
4. For maze problems, use a `visited` array to prevent revisiting cells.
5. N-Queens and Sudoku are classic backtracking problems — understand the **constraint checking**.
6. Backtracking significantly **prunes the search space** compared to brute force.
