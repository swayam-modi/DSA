import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Huffman {

    TreeNode root;
    boolean[] encodedData;
    HashMap<Character, ArrayList<Boolean>> encodedTable = new HashMap<>();

    private class TreeNode implements Comparable<TreeNode> {
        TreeNode left;
        TreeNode right;
        char data;
        int val;

        public TreeNode(char data, int val) {
            this.data = data;
            this.val = val;
            this.left = null;
            this.right = null;
        }

        public void setChildren(TreeNode left, TreeNode right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(TreeNode node) {
            return this.val - node.val;
        }
    }

    public Huffman(String str) {

        HashMap<Character, Integer> freq = new HashMap<>();
        PriorityQueue<TreeNode> minHeap = new PriorityQueue<>();

        // O(L)
        for (int i = 0; i < str.length(); i++) {
            if (freq.containsKey(str.charAt(i))) {
                freq.put(str.charAt(i), freq.get(str.charAt(i)) + 1);
                continue;
            }
            freq.put(str.charAt(i), 1);
        }

        for (char key : freq.keySet()) {
            TreeNode node = new TreeNode(key, freq.get(key));
            minHeap.offer(node);
        }

        while (minHeap.size() > 1) {
            TreeNode leftNode = minHeap.poll();
            TreeNode rightNode = minHeap.poll();

            TreeNode node = new TreeNode('#', leftNode.val + rightNode.val);
            node.setChildren(leftNode, rightNode);

            minHeap.offer(node);
        }

        this.root = minHeap.poll();

        int size = createEncodeTable(root, new ArrayList<>(),freq);
        this.encodedData = new boolean[size];

        createEncodeData(str);
    }

    private int createEncodeTable(TreeNode node, ArrayList<Boolean> list,HashMap<Character,Integer> freq) {
        int size = 0;

        if (node == null) {
            return size;
        }

        list.add(false);
        size += createEncodeTable(node.left, list,freq);
        list.removeLast();

        list.add(true);
        size += createEncodeTable(node.right, list,freq);
        list.removeLast();

        if (node.left == null && node.right == null) {
            size += freq.get(node.data) * list.size();
            encodedTable.put(node.data, new ArrayList<>(list));
        }

        return size;
    }

    private void createEncodeData(String data) {
        int idx = 0;
        for (int i = 0; i < data.length(); i++) {
            for(boolean val: encodedTable.get(data.charAt(i))){
                encodedData[idx++] = val;
            }
        }
    }

    public boolean[] getEncode() {
        return encodedData;
    }

    public String getDecode() {
        return getDecode(this.root, 0);
    }

    private String getDecode(TreeNode root, int idx) {
        StringBuilder sb = new StringBuilder();
        while (idx < encodedData.length) {
            TreeNode node = root;
            while (node.left != null && node.right != null) {
                node = encodedData[idx++] ? node.right : node.left;
            }
            sb.append(node.data);
        }
        return sb.toString();
    }

    public void printTree() {
        printTree(root, 0);
    }

    private void printTree(TreeNode node, int level) {
        if (node == null) {
            return;
        }
        printTree(node.right, level + 1);

        if (level != 0) {
            for (int i = 0; i < level - 1; i++) {
                System.out.print("|\t");
            }
            System.out.println("|-----> " + node.data + "[" + node.val + "]");
        } else {
            System.out.println(node.data + "[" + node.val + "]");
        }
        printTree(node.left, level + 1);
    }
}