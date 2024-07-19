import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        String para = "swayam";
        Huffman huffman = new Huffman(para);
        System.out.println(Arrays.toString(huffman.getEncode()));
        System.out.println(huffman.getDecode());
    }
}
