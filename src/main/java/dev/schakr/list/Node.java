package dev.schakr.list;

public interface Node<T> {
    static <T> Node<T> empty() {
        return EmptyNode.instance();
    }

    boolean isEmpty();
}
