package dev.schakr.map;

import io.vavr.control.Option;

/**
 * CollisionNode is a data structure that implements the Node interface, designed to manage
 * key-value pairs with collision handling capabilities. This node type stores keys and their
 * associated values in arrays and enables operations such as insertion, deletion, retrieval,
 * and checking for key existence.
 *
 * @param <A> The type of the keys stored in the node.
 * @param <B> The type of the values associated with the keys.
 */
public class CollisionNode<A, B> implements Node<A, B> {
    A[] keys;
    B[] vals;
    int size;
    int capacity;

    CollisionNode(A[] keys, B[] vals, int size, int capacity) {
        this.keys = keys;
        this.vals = vals;
        this.size = size;
        this.capacity = capacity;
    }

    @SuppressWarnings("unchecked")
    CollisionNode(LeafNode<A, B> node, A a, B b) {
        capacity = 10;
        size = 2;
        keys = (A[]) new Object[capacity];
        vals = (B[]) new Object[capacity];
        keys[0] = node.key; vals[0] = node.value;
        keys[1] = a; vals[1] = b;
    }

    /**
     * Removes the entry corresponding to the specified key from the current node.
     * If the key does not exist in the node, the current node is returned unchanged.
     * If the node becomes empty after removal, an empty node is returned.
     *
     * @param key the key to be removed from the node
     * @return a new node with the specified key removed, or the current node if the key is not found
     */
    Node<A, B> delete(A key) {
        int index = indexOf(key);

        if (index == -1) return this;
        if (size == 1) return Node.empty();

        A[] newKeys = keys.clone();
        B[] newVals = vals.clone();
        for (int j = index; j < size - 1; j++) {
            newKeys[j] = newKeys[j + 1];
            newVals[j] = newVals[j + 1];
        }
        newKeys[size - 1] = null;
        newVals[size - 1] = null;

        if (size == 2) return new LeafNode<>(newKeys[0], newVals[0]);
        return new CollisionNode<>(newKeys, newVals, size - 1, capacity);
    }

    /**
     * Inserts a key-value pair into the node. If the current node exceeds its capacity threshold,
     * it resizes and inserts the pair in a resized node. Otherwise, it inserts
     * the pair in a copy of the current node.
     *
     * @param key the key to be inserted into the node
     * @param value the value associated with the key to be inserted
     * @return a new node containing the inserted key-value pair
     */
    Node<A, B> insert(A key, B value) {
        if (size >= capacity * 0.75d) {
            return resize().insertMutable(key, value);
        } else {
            return copy().insertMutable(key, value);
        }
    }

    /**
     * Retrieves the value associated with the specified key from the node.
     * If the key is not present, an empty {@code Option} is returned.
     *
     * @param key the key whose associated value is to be retrieved
     * @return an {@code Option} containing the value associated with the specified key,
     *         or {@code Option.none()} if the key is not found
     */
    Option<B> get(A key) {
        int index = indexOf(key);
        if (index == -1) return Option.none();
        return Option.of(vals[index]);
    }

    /**
     * Checks if the specified key exists in the current node.
     *
     * @param key the key to check for presence in the node
     * @return true if the key is found in the node, false otherwise
     */
    boolean contains(A key) {
        return indexOf(key) != -1;
    }

    /**
     * Determines if the current collision node is empty.
     *
     * @return true if the node contains no elements, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return size > 0;
    }

    /**
     * Creates and returns a copy of the current collision node.
     *
     * @return a new CollisionNode containing the same keys, values, size, and capacity as the current one
     */
    private CollisionNode<A, B> copy() {
        return new CollisionNode<>(keys, vals, size, capacity);
    }

    /**
     * Inserts a key-value pair into the current collision node. If the key already exists,
     * the corresponding value is updated. If the key does not exist, it is added to the node.
     * The insertion or update is performed in a mutable manner without creating a new node.
     *
     * @param key the key to be inserted or updated in the node
     * @param value the value associated with the key to be inserted or updated
     * @return the current collision node after the key-value pair has been inserted or updated
     */
    private CollisionNode<A, B> insertMutable(A key, B value) {
        int keyIndex = indexOf(key);
        if (keyIndex == -1) {
            keys[size] = key; vals[size] = value;
            size++;
        } else {
            vals[keyIndex] = value;
        }
        return this;
    }

    /**
     * Finds the index of the specified key in the current collision node.
     * If the key is found, returns its index. If the key is not found, returns -1.
     *
     * @param key the key whose index is to be determined in the node
     * @return the index of the specified key if it exists in the node, or -1 if the key is not present
     */
    private int indexOf(A key) {
        if (size == 0) return -1;
        int ecountered = 0;
        for (int i = 0; i < capacity; i++) {
            if (keys[i] != null) {
                ecountered++;
                if (keys[i].equals(key)) return i;
            }
            if (ecountered == size) return -1;
        }
        return -1;
    }

    /**
     * Resizes the current collision node by doubling its capacity.
     * This method creates new key and value arrays with updated capacity,
     * transfers the existing keys and values into the new arrays, and
     * returns a new collision node with the updated capacity.
     *
     * @return a new CollisionNode instance with increased capacity and copied contents
     */
    @SuppressWarnings("unchecked")
    private CollisionNode<A, B> resize() {
        int newCapacity = capacity * 2;
        A[] newKeys = (A[]) new Object[newCapacity];
        B[] newVals = (B[]) new Object[newCapacity];
        for (int i = 0; i < capacity; i++) {
            if (keys[i] != null) {
                newKeys[i] = keys[i];
                newVals[i] = vals[i];
            }
        }

        return new CollisionNode<>(newKeys, newVals, size, newCapacity);
    }

}
