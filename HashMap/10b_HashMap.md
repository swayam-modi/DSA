# HashMap

> **Source:** Handwritten DSA Notes – Pages 176–177

---

## 1. What is a HashMap?

A HashMap stores data as **key-value pairs** with **O(1) average** lookup, insertion, and deletion by using a **hash function** to map keys to array indices.

### Internal Structure

```
Array of Buckets (LinkedLists):

Index 0: → [K₁,V₁] → [K₅,V₅] → null     (collision — chaining)
Index 1: → [K₂,V₂] → null
Index 2: → null                             (empty bucket)
Index 3: → [K₃,V₃] → null
Index 4: → [K₄,V₄] → [K₆,V₆] → null
```

### How it Works

1. **Hash Function:** `index = hashCode(key) % array.length`
2. **Store:** Place the key-value pair at the computed index.
3. **Collision:** When two keys hash to the same index → store in a **LinkedList** (chaining).

---

## 2. Custom HashMap Implementation

```java
public class CustomHashMap<K, V> {

    private LinkedList<Entity>[] arr;
    private int size;
    private final int defaultSize = 10;
    private final float loadFactor = 0.75f;
    private final int PRIME = 101;

    // Entity = one key-value pair
    private class Entity {
        K key;
        V value;
        public Entity(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    CustomHashMap() {
        this.arr = new LinkedList[defaultSize];
        this.size = 0;
        for (int i = 0; i < defaultSize; i++) {
            arr[i] = new LinkedList<>();
        }
    }
}
```

### Hash Function

```java
int hash = Math.abs(key.hashCode()) * PRIME % arr.length;
```

- `hashCode()` → Java's built-in hash for any object
- Multiply by a **prime** (101) → reduces clustering
- `% arr.length` → keeps index within bounds
- `Math.abs()` → ensures non-negative index

### Put Operation

```java
public void put(K key, V value) {
    int hash = Math.abs(key.hashCode()) * PRIME % arr.length;
    LinkedList<Entity> entities = arr[hash];

    // If key already exists, update its value
    for (Entity entity : entities) {
        if (entity.key.equals(key)) {
            entity.value = value;
            return;
        }
    }

    // Check load factor → rehash if needed
    if ((float) size / arr.length > loadFactor) {
        entities.add(new Entity(key, value));
        reHashing();
        return;
    }

    entities.add(new Entity(key, value));
    size++;
}
```

### Get Operation

```java
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
```

### Remove Operation

```java
public V remove(K key) {
    int hash = Math.abs(key.hashCode()) * PRIME % arr.length;
    LinkedList<Entity> entities = arr[hash];

    for (Entity entity : entities) {
        if (entity.key.equals(key)) {
            V v = entity.value;
            entities.remove(entity);
            size--;
            return v;
        }
    }
    return null;
}
```

### Rehashing (Dynamic Resizing)

When load exceeds `loadFactor` (75%), double the array and redistribute all entries:

```java
private void reHashing() {
    LinkedList<Entity>[] old = arr;
    size = 0;
    arr = new LinkedList[old.length * 2];

    for (int i = 0; i < old.length * 2; i++) {
        arr[i] = new LinkedList<>();
    }

    // Re-insert all entries (hashes change with new array size)
    for (LinkedList<Entity> entities : old) {
        for (Entity entity : entities) {
            put(entity.key, entity.value);
        }
    }
}
```

---

## 3. HashCode & Equals Contract

```java
// Rule: If a.equals(b) → a.hashCode() == b.hashCode()
// But: a.hashCode() == b.hashCode() does NOT mean a.equals(b)

@Override
public int hashCode() {
    return Objects.hash(name, age);
}

@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Person other = (Person) obj;
    return age == other.age && Objects.equals(name, other.name);
}
```

---

## 4. Complexity

| Operation | Average | Worst (many collisions) |
|-----------|---------|------------------------|
| `put()` | O(1) | O(n) |
| `get()` | O(1) | O(n) |
| `remove()` | O(1) | O(n) |
| `containsKey()` | O(1) | O(n) |
| Iteration | O(n + buckets) | — |

> **Java 8+:** When a bucket's chain exceeds 8 entries, it converts to a **Red-Black Tree** → worst case improves from O(n) to O(log n).

---

## 5. Key Takeaways

1. HashMap maps keys to indices using **hash function** → O(1) average operations.
2. Collisions are handled by **chaining** (LinkedLists at each bucket).
3. **Load factor** (0.75) triggers rehashing — double array, redistribute entries.
4. Always override **both** `hashCode()` and `equals()` when using custom objects as keys.
5. Java 8+ converts chains to **Red-Black Trees** when bucket size > 8.
