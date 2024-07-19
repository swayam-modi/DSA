package GraphTheory;

import java.util.ArrayList;
import java.util.Stack;

// This Class is the application of KosaRaju's algorithm
public class SCC {
    SCC(ArrayList<int[]>[] graph) {

        // first find the topologicalSort of the graph
        Stack<Integer> topologicalSort = topSort(0, graph, new Stack<>());

        // then find transpose of a graph mean if (0 -> 1) then (1 -> 0) means reverse
        // the graph
        ArrayList<int[]>[] transposeGraph = getTranspose(graph);

        // then after apply dfs on the topological sequence on the trasposed of the
        // graph
        boolean[] visited = new boolean[graph.length];

        while (!topologicalSort.isEmpty()) {
            int node = topologicalSort.pop();

            if (!visited[node]) {
                reversedDfs(transposeGraph, visited, node);
                System.out.println();
            }

        }
    }

    // Topological sort
    private Stack<Integer> topSort(int node, ArrayList<int[]>[] graph, Stack<Integer> stack) {
        boolean[] visited = new boolean[graph.length];

        for (int i = 0; i < graph.length; i++) {
            if (!visited[i]) {
                helper(i, graph, stack, visited);
            }
        }

        return stack;
    }

    // Topological helper
    private void helper(int node, ArrayList<int[]>[] graph, Stack<Integer> stack, boolean[] visited) {
        if (visited[node]) {
            return;
        }

        visited[node] = true;

        for (int[] arr : graph[node]) {
            helper(arr[1], graph, stack, visited);
        }

        stack.push(node);
    }

    // Transpose of a graph
    private ArrayList<int[]>[] getTranspose(ArrayList<int[]>[] graph) {
        ArrayList<int[]>[] transpose = new ArrayList[graph.length];

        for (int i = 0; i < graph.length; i++) {
            transpose[i] = new ArrayList<>();
        }

        for (int i = 0; i < graph.length; i++) {
            for (int[] arr : graph[i]) {
                transpose[arr[1]].add(new int[] { arr[1], arr[0] });
            }
        }

        return transpose;
    }

    // simple dfs on the topological sequence
    private void reversedDfs(ArrayList<int[]>[] graph, boolean[] visited, int node) {
        if (visited[node]) {
            return;
        }

        System.out.print(node + " ");
        visited[node] = true;

        for (int[] edges : graph[node]) {
            reversedDfs(graph, visited, edges[1]);
        }
    }
}