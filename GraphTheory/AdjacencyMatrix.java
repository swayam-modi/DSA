package GraphTheory;

// in adjacency metrix edge is denoted by 1 in graph or if weightedGraph then weight is denoted in place of 1
// matrix is n X n where n = no.of nodes in graph
import java.util.Arrays;

public class AdjacencyMatrix {
    private int[][] graph;

    public AdjacencyMatrix(int vertex) {
        this.graph = new int[vertex][vertex];
    }

    public void addEdge(int src, int dst) {
        this.graph[src][dst] = 1;
    }

    public void addEdge(int src, int dst, int weight) {
        this.graph[src][dst] = weight;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int[] arr : graph) {
            sb.append(Arrays.toString(arr));
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
