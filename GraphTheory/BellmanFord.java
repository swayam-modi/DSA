package GraphTheory;

//Bellmanford algorithm used when weight is negative present in graph
//Its a dynamic programming approach O(V*E);
//Its not working when negative weight cycle present in graph

import java.util.ArrayList;
import java.util.Arrays;

public class BellmanFord {
    BellmanFord(ArrayList<int[]>[] graph, int src) {
        int size = graph.length;
        int[] distance = new int[size];

        for (int i = 0; i < size; i++) {
            distance[i] = Integer.MAX_VALUE;
        }
        distance[src] = 0;

        for (int i = 0; i < size - 1; i++) {
            boolean flag = false;

            for (int j = 0; j < size; j++) {
                for (int[] edge : graph[j]) {
                    if (distance[j] != Integer.MAX_VALUE && distance[j] + edge[1] < distance[edge[0]]) {
                        if (!flag) {
                            flag = true;
                        }
                        distance[edge[0]] = distance[j] + edge[1];
                    }
                }
            }
            System.out.println(Arrays.toString(distance));
            if (!flag) {
                break;
            }
        }
    }
}