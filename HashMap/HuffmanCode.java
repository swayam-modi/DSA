import java.util.HashMap;

public class HuffmanCode {
    private HashMap<Character, String> encodeMap;
    private HashMap<String, Character> decodeMap;
    private String inputString;

    private class TreeNode implements Comparable<TreeNode> {

        Character data;
        int cost;
        TreeNode left;
        TreeNode right;

        public TreeNode(Character data, int cost) {
            this.data = data;
            this.cost = cost;
            this.left = null;
            this.right = null;
        }

        @Override
        public int compareTo(TreeNode other) {
            return this.cost - other.cost;
        }
    }

    public HuffmanCode(String inputString) throws Exception {

        this.inputString = inputString;
        HashMap<Character, Integer> frequencyMap = new HashMap<>();

        // storing frequency of every character in HashMap
        for (int i = 0; i < inputString.length(); i++) {
            Character ch = inputString.charAt(i);

            if (frequencyMap.containsKey(ch)) {
                frequencyMap.put(ch, frequencyMap.get(ch) + 1);
            } else {
                frequencyMap.put(ch, 1);
            }
        }

        // storing in minHeap by their fequency
        Heap<TreeNode> heap = new Heap<>();

        for (char ch : frequencyMap.keySet()) {
            TreeNode node = new TreeNode(ch, frequencyMap.get(ch));
            heap.insert(node);
        }

        // removing every two nodes from min heap and merge them and store them again
        // until heap.size() == 1
        while (heap.size() != 1) {
            TreeNode first = heap.remove();
            TreeNode second = heap.remove();

            TreeNode newNode = new TreeNode('\0', first.cost + second.cost);
            newNode.left = first;
            newNode.right = second;

            heap.insert(newNode);
        }

        TreeNode tree = heap.remove();

        this.encodeMap = new HashMap<>();
        this.decodeMap = new HashMap<>();

        this.initEncodingDecoding(tree, "");

    }

    private void initEncodingDecoding(TreeNode node, String code) {

        if (node == null) {
            return;
        }

        if (node.left == null && node.right == null) {

            this.encodeMap.put(node.data, code);
            this.decodeMap.put(code, node.data);
        }
        initEncodingDecoding(node.left, code + '0');
        initEncodingDecoding(node.right, code + '1');
    }

    public StringBuilder encode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < inputString.length(); i++) {
            sb.append(this.encodeMap.get(inputString.charAt(i)));
        }

        return sb;
    }

    public String decode(StringBuilder source) {

        StringBuilder sb = new StringBuilder();
        String key = "";

        for (int i = 0; i < source.length(); i++) {
            key = key + source.charAt(i);

            if (decodeMap.containsKey(key)) {
                sb.append(decodeMap.get(key));
                key = "";
            }
        }

        return sb.toString();
    }
}
