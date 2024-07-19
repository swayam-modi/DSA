
public class Trie {

    // child class of Trie which has two property
    // 1. TrieNode[] array which marks the character
    // 2.boolean endOfWord that mark as word is ended here
    private class TrieNode {
        TrieNode[] childNodes;
        boolean endOfWord;

        TrieNode() {
            this.childNodes = new TrieNode[26];
            for (int i = 0; i < 26; i++) {
                this.childNodes[i] = null;
            }
        }

    }

    TrieNode root;

    // constructor for Trie Structure
    public Trie() {
        this.root = new TrieNode();
    }

    // Insertion of a word
    // O(L) length of word
    public void insert(String word) {
        TrieNode node = this.root;

        for (int i = 0; i < word.length(); i++) {
            int index = word.charAt(i) - 'a';

            // if node is not created then create it
            if (node.childNodes[index] == null) {
                node.childNodes[index] = new TrieNode();
            }

            // if word reaches last character then mark that node as endOfWord = true
            if (i == word.length() - 1) {
                node.childNodes[index].endOfWord = true;
            }

            node = node.childNodes[index];
        }
    }

    // Insertion of array of String
    public void insert(String[] arr) {

        for (String str : arr) {
            insert(str);
        }
    }

    // Searching word method
    // O(L) length of word
    public boolean search(String word) {
        TrieNode node = this.root;

        for (int i = 0; i < word.length(); i++) {
            int index = word.charAt(i) - 'a';

            // if node(char) of that perticular word is null then return false
            if (node.childNodes[index] == null) {
                return false;
            }
            // if last node of word and endOfWord is false then return false
            if (i == word.length() - 1 && !node.childNodes[index].endOfWord) {
                return false;
            }

            node = node.childNodes[index];
        }
        return true;
    }

    // checks StartsWith method
    // O(P) prefix length
    // in this method all logic same as search method only endOfWord is not checked
    // in this
    public boolean startsWith(String prefix) {
        TrieNode node = root;

        for (int i = 0; i < prefix.length(); i++) {
            int index = prefix.charAt(i) - 'a';

            if (node.childNodes[index] == null) {
                return false;
            }

            node = node.childNodes[index];
        }

        return true;
    }
}
