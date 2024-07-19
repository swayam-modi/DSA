package GraphTheory;

import java.util.ArrayList;

public class ArticulationPoint {

    ArticulationPoint(ArrayList<int[]>[] graph) {
        int[] discovry = new int[graph.length];
        int[] low = new int[graph.length];
        boolean[] ap = new boolean[graph.length];
        boolean[] visited = new boolean[graph.length];

        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) {
                dfs(graph, discovry, low, visited, ap, i, -1);
            }
        }

        for (int i = 0; i < ap.length; i++) {
            if (ap[i]) {
                System.out.println(i);
            }
        }
    }

    private void dfs(ArrayList<int[]>[] graph, int[] dt, int[] low, boolean[] visited, boolean[] ap, int current,
            int parent) {

        visited[current] = true;
        int children = 0;

        for (int[] edges : graph[current]) {
            int neigh = edges[1];

            if (neigh == parent) {
                continue;
            }

            if (visited[neigh]) {
                low[current] = Math.min(low[current], dt[neigh]);
            }

            if (!visited[neigh]) {
                low[neigh] = dt[neigh] = dt[current] + 1;

                dfs(graph, dt, low, visited, ap, neigh, current);

                low[current] = Math.min(low[current], low[neigh]);

                if (dt[current] <= low[neigh] && parent != -1) {
                    ap[current] = true;
                }
                children++;

            }
        }

        if (parent == -1 && children > 1) {
            ap[current] = true;
        }
    }
}
