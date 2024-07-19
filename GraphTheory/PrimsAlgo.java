package GraphTheory;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class PrimsAlgo {
    PrimsAlgo(ArrayList<int[]>[] graph) {
        PriorityQueue<Edge> heap = new PriorityQueue<>();
        boolean[] visited = new boolean[graph.length];
        int mst = 0;

        heap.offer(new Edge(0, 0));

        while (!heap.isEmpty()) {
            Edge current = heap.poll();

            if (visited[current.dst]) {
                continue;
            }

            System.out.println(current.dst + " -> " + current.weight);

            visited[current.dst] = true;
            mst += current.weight;

            for (int[] edge : graph[current.dst]) {
                if (!visited[edge[0]]) {
                    heap.offer(new Edge(edge[0], edge[1]));
                }
            }

        }

        System.out.println(mst);
    }

    private class Edge implements Comparable<Edge> {

        int dst;
        int weight;

        Edge(int dst, int w) {
            this.dst = dst;
            this.weight = w;
        }

        @Override
        public int compareTo(Edge e) {
            return this.weight - e.weight;
        }

    }
}
