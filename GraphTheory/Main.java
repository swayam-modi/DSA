package GraphTheory;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph(5);

        graph.addStructure(0, 1);
        graph.addStructure(0, 2);
        graph.addStructure(0, 3);

        graph.addStructure(1, 0);
        graph.addStructure(1, 2);

        graph.addStructure(2, 0);
        graph.addStructure(2, 1);

        graph.addStructure(3, 0);
        graph.addStructure(3, 4);

        graph.addStructure(4, 3);

        ArticulationPoint ap = new ArticulationPoint(graph.getGraph());

    }
}
