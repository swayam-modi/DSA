package GraphTheory;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class DijkstraAlgo {

    private int[] distance;
    PriorityQueue<Integer> queue;
    private boolean[] visited;
    private StringBuilder[] allShortestPaths;

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

            if (visited[node]) {
                continue;
            }
            visited[node] = true;

            System.out.println(Arrays.toString(distance) + "\t" + node);

            for (int i = 0; i < graph[node].size(); i++) {
                int neighbour = graph[node].get(i)[0];
                int curDistance = graph[node].get(i)[1];

                if (!visited[neighbour]) {
                    int current = distance[neighbour];
                    distance[neighbour] = Math.min(distance[node] + curDistance, distance[neighbour]);

                    if (current != distance[neighbour]) {

                        allShortestPaths[neighbour]
                                .delete(0, allShortestPaths[neighbour].length())
                                .append(allShortestPaths[node])
                                .append(" -> ")
                                .append(neighbour);
                    }
                    queue.offer(neighbour);
                }
            }
        }
    }

    public int shortestDistance(int dst) {
        return this.distance[dst];
    }

    public void allShortestPaths() {
        for (StringBuilder path : allShortestPaths) {
            if (path.isEmpty()) {
                System.out.println("no path found");
            } else {
                System.out.println(path.toString());
            }
        }
    }

    public String shortestPath(int node) {
        String path = allShortestPaths[node].toString();

        return path.isEmpty() ? "No Path exist" : path;
    }

}