import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class Heap<T extends Comparable<T>> {

    // storing the elements used ArrayList
    private ArrayList<T> list;

    // Constructor with no parameters
    public Heap() {
        list = new ArrayList<>();
    }

    // Constructor with insert an array of elements
    public Heap(T[] arr) {
        this();
        insertArray(arr);
    }

    // insert item method
    public void insert(T item) {
        this.list.add(item);
        upHeap();
    }

    // removing element method
    public T remove() throws NoSuchElementException {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Heap is Null !!");
        }
        if (size() == 1) {
            return list.removeFirst();
        }
        T max = get(0);
        list.set(0, list.removeLast());

        downHeap();

        return max;
    }

    // heapSort method
    public void sort() {

        int size = size();
        int ptr = size - 1;

        for (int i = 0; i < size - 1; i++) {
            T removed = sortHelper(size - i - 1);
            list.add(ptr, removed);
            ptr--;
        }

    }

    // sortHelper
    private T sortHelper(int last) {
        T max = get(0);
        list.set(0, list.remove(last));

        int index = 0;

        while (true) {
            int i = index;
            int left = left(i);
            int right = right(i);

            if (left < last && get(i).compareTo(get(left)) <= 0) {
                i = left;
            }
            if (right < last && get(i).compareTo(get(right)) <= 0) {
                i = right;
            }

            if (i != index) {
                swap(i, index);
                index = i;
            } else {
                break;
            }
        }
        return max;
    }

    // Insertion helper function
    private void upHeap() {
        int i = size() - 1;

        if (i == 0) {
            return;
        }

        while (i > 0) {

            int j = parent(i);

            // if parent is smaller than child
            if (get(j).compareTo(get(i)) < 0) {
                swap(i, j);
                i = j;
            } else {
                break;
            }
        }
    }

    // removing helper function
    private void downHeap() {
        int index = 0;

        while (true) {
            int i = index;
            int left = left(i);
            int right = right(i);

            if (left < size() && get(i).compareTo(get(left)) < 0) {
                i = left;
            }
            if (right < size() && get(i).compareTo(get(right)) < 0) {
                i = right;
            }

            if (index != i) {
                swap(index, i);
                index = i;
            } else {
                break;
            }

        }
    }

    // insert array directly
    public void insertArray(T[] arr) {

        list.addAll(Arrays.asList(arr));

        for (int i = size() / 2; i >= 0; i--) {
            heapify(i);
        }
    }

    // heaify method
    private void heapify(int index) {

        while (true) {
            int i = index;
            int left = left(index);
            int right = right(index);

            if (left < size() && get(index).compareTo(get(left)) < 0) {
                swap(index, left);
                i = left;
            }
            if (right < size() && get(index).compareTo(get(right)) < 0) {
                swap(index, right);
                i = right;
            }
            if (i != index) {
                index = i;
            } else {
                break;
            }
        }
    }

    // printing Heap
    public void printHeap() {
        System.out.println("Heap in array: " + list);
        System.out.println();
        prettyPrint();
    }

    public void prettyPrint() {
        int i = 0;
        while (i < size()) {
            for (int j = i; j <= i * 2 && j < size(); j++) {
                System.out.print("(" + get(j) + ")\t");
            }
            System.out.println();
            i = left(i);
        }
    }

    // above all the helper methods ----------------------

    // this method return size of list
    public int size() {
        return list.size();
    }

    // only returns peek value not remove
    public T peek() {
        return get(0);
    }

    // this is simple list get method that's return particular index value
    private T get(int i) {
        return this.list.get(i);
    }

    // this is swap elements in list
    private void swap(int a, int b) {
        T temp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, temp);
    }

    // this is return left child of node FORMULA: left = i * 2 + 1 (for zero indexed
    // array)
    private int left(int i) {
        return i * 2 + 1;
    }

    // this is return right child of node FORMULA: right = i * 2 + 2 (for zero
    // indexed array)
    private int right(int i) {
        return i * 2 + 2;
    }

    // this is return parent of node FORMULA: right = (i - 1) / 2 (for zero indexed
    // array)
    private int parent(int i) {
        return (i - 1) / 2;
    }
}
