import java.util.LinkedList;

public class CustomHashMap<K, V> {

    private LinkedList<Entity>[] arr;
    private int size;
    private final int defaultSize = 10;
    private final float loadFactor = 0.75f;
    private final int PRIME = 101;

    CustomHashMap() {
        this.arr = new LinkedList[defaultSize];
        this.size = 0;

        for (int i = 0; i < defaultSize; i++) {
            arr[i] = new LinkedList<>();
        }
    }

    // puting key and value in array
    public void put(K key, V value) {
        int hash = Math.abs(key.hashCode()) * PRIME % arr.length;

        LinkedList<Entity> entities = arr[hash];

        for (Entity entity : entities) {
            if (entity.key.equals(key)) {
                entity.value = value;
                return;
            }
        }

        if ((float) size / arr.length > loadFactor) {
            entities.add(new Entity(key, value));
            reHashing();
            return;
        }

        entities.add(new Entity(key, value));

        size++;
    }

    private void reHashing() {

        LinkedList<Entity>[] old = arr;
        size = 0;

        arr = new LinkedList[old.length * 2];

        for (int i = 0; i < old.length * 2; i++) {
            arr[i] = new LinkedList<>();
        }

        for (LinkedList<Entity> entities : old) {
            for (Entity entity : entities) {
                put(entity.key, entity.value);
            }
        }

    }

    // getting value from map on the key value
    public V get(K key) {
        int hash = Math.abs(key.hashCode()) * PRIME % arr.length;

        LinkedList<Entity> entities = arr[hash];

        for (Entity entity : entities) {
            if (entity.key.equals(key)) {
                return entity.value;
            }
        }

        return null;
    }

    // remove the Entity from HashMap
    public V remove(K key) {
        int hash = Math.abs(key.hashCode()) * PRIME % arr.length;
        V v = null;

        LinkedList<Entity> entities = arr[hash];

        for (Entity entity : entities) {
            if (entity.key.equals(key)) {
                v = get(entity.key);
                entities.remove(entity);
                size--;
                return v;
            }
        }

        return v;
    }

    // return String value of map
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ ");

        for (LinkedList<Entity> linkedList : arr) {

            for (Entity entity : linkedList) {
                sb.append(entity.key).append(" = ").append(entity.value).append(" ,");
            }

        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");

        return sb.toString();
    }

    public int size() {
        return this.size;
    }

    // Entity internal class
    private class Entity {
        K key;
        V value;

        public Entity(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}