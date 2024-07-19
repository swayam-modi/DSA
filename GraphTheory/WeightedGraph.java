package GraphTheory;

import java.util.ArrayList;

public class WeightedGraph {

    private class Edge {
        int src;
        int dst;
        int weight;

        Edge(int src, int dst, int weight) {
            this.src = src;
            this.dst = dst;
            this.weight = weight;
        }
    }

    private ArrayList<Edge>[] nodes;

    public WeightedGraph(int vertices) {
        this.nodes = new ArrayList[vertices];

        for (int i = 0; i < vertices; i++) {
            this.nodes[i] = new ArrayList<>();
        }
    }

    public void addEdge(int src, int dst, int weight) {
        this.nodes[src].add(new Edge(src, dst, weight));
    }

    private int size() {
        return this.nodes.length;
    }

    public ArrayList<int[]>[] getGraph() {
        ArrayList<int[]>[] graphStructure = new ArrayList[size()];

        for (int i = 0; i < size(); i++) {
            graphStructure[i] = new ArrayList<>();
        }

        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < this.nodes[i].size(); j++) {
                graphStructure[i].add(new int[] { this.nodes[i].get(j).dst, this.nodes[i].get(j).weight });
            }
        }

        return graphStructure;
    }

}
