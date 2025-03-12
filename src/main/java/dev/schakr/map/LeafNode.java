package dev.schakr.map;

/**
 * Represents a leaf node in a tree-like structure, holding a key and its associated value.
 *
 * This class is a concrete implementation of the {@code Node} interface and is used
 * to store key-value pairs. A leaf node is not considered empty.
 *
 * @param <A> the type of the key
 * @param <B> the type of the value
 */
class LeafNode<A, B> implements Node<A, B> {
    final A key;
    final B value;

    LeafNode(A a, B b) {
        this.key = a;
        this.value = b;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
