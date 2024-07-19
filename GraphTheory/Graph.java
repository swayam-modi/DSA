package GraphTheory;

// Adjucency list Method
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Graph {

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

    public void showStructure() {

        boolean flag;

        int edge = 0;

        do {
            int vertex = 0;
            flag = false;

            while (vertex < this.vertices.length) {
                if (edge < this.vertices[vertex].size()) {
                    System.out.print(
                            "(" + vertices[vertex].get(edge).src + ", " + vertices[vertex].get(edge).dst + ")\t");
                    if (!flag) {
                        flag = true;
                    }
                } else {
                    System.out.print("\t");
                }
                vertex++;
            }
            edge++;
            System.out.println();

        } while (flag);
    }

    // Breath First Search
    public void bfs() {
        boolean[] visited = new boolean[this.vertices.length];

        // this loop needed bcz in graph some nodes may be not connected any node so it
        // is not counted in visited array so we can esaily find that node
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

    // Depth First Search
    public void dfs() {
        // their is may be some free components so apply loop for that
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

    public void allPossiblePaths(int src, int dst) {
        boolean[] visited = new boolean[vertices.length];
        visited[src] = true;
        allPossiblePaths("" + src, visited, src, dst);
    }

    // Here String is used for taking node if path not present empty string will be
    // return bcz of immutability of String
    private void allPossiblePaths(String s, boolean[] visited, int src, int dst) {

        if (src == dst) {
            System.out.println(s);
            return;
        }

        for (Edge edge : this.vertices[src]) {
            if (!visited[edge.dst]) {
                visited[edge.dst] = true;
                allPossiblePaths(s + " -> " + edge.dst, visited, edge.dst, dst);
                visited[edge.dst] = false;
            }
        }

    }

    // Cycle detection for non Directed Graph
    public boolean isCyclic() {
        boolean[] visited = new boolean[vertices.length];
        visited[0] = true;

        return isCyclicHelper(visited, 0, -1);
    }

    private boolean isCyclicHelper(boolean[] visited, int src, int pre) {

        for (Edge edge : this.vertices[src]) {

            if (visited[edge.dst] && edge.dst != pre) {
                return true;
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

    // Cycle detection for Directed Graph
    public boolean isCyclicDg() {
        boolean[] visited = new boolean[vertices.length];

        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) {
                if (isCyclicDgHelper(i, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isCyclicDgHelper(int src, boolean[] visited) {

        for (Edge edge : this.vertices[src]) {
            if (visited[edge.dst]) {
                return true;
            } else {
                visited[edge.dst] = true;
                if (isCyclicDgHelper(edge.dst, visited)) {
                    return true;
                }
                visited[edge.dst] = false;
            }
        }

        return false;
    }

    // Topological sort for DAG (Directed Acyclic Graph)
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
        stack.push(node);
    }

    public ArrayList<int[]>[] getGraph() {
        ArrayList<int[]>[] graphStructure = new ArrayList[size()];

        for (int i = 0; i < size(); i++) {
            graphStructure[i] = new ArrayList<>();
        }

        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < this.vertices[i].size(); j++) {
                graphStructure[i].add(new int[] { this.vertices[i].get(j).src, this.vertices[i].get(j).dst });
            }
        }

        return graphStructure;
    }

    private int size() {
        return this.vertices.length;
    }
}
