package GraphTheory;
import java.util.ArrayList;

// This algorithm is used for finding Bridges in graph
public class TarjansAlgo {
    TarjansAlgo(ArrayList<int[]>[] graph) {
        int[] disTime = new int[graph.length];
        int[] lowTime = new int[graph.length];
        boolean[] visited = new boolean[graph.length];

        dfs(graph, disTime, lowTime, visited, -1, 0);
    }

    private void dfs(ArrayList<int[]>[] graph, int[] disTime, int[] lowTime, boolean[] visited, int neb, int node) {
        visited[node] = true;

        for (int[] edges : graph[node]) {

            if (edges[1] == neb) {
                continue;
            }

            if (!visited[edges[1]]) {
                disTime[edges[1]] = disTime[node] + 1;
                lowTime[edges[1]] = disTime[edges[1]];

                dfs(graph, disTime, lowTime, visited, node, edges[1]);

                lowTime[node] = Math.min(lowTime[node], lowTime[edges[1]]);

                if (disTime[node] < lowTime[edges[1]]) {
                    System.out.println(node + " --- " + edges[1]);
                }
            }

            else if (visited[edges[1]]) {
                lowTime[node] = Math.min(disTime[edges[1]], lowTime[node]);
            }
        }
    }
}
