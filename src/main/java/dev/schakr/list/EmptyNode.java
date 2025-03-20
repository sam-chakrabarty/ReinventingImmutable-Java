package dev.schakr.list;

class EmptyNode<T> implements Node<T> {
    private static final EmptyNode<?> INSTANCE = new EmptyNode<>();

    @SuppressWarnings("unchecked")
    static <T> EmptyNode<T> instance() {
        return (EmptyNode<T>) INSTANCE;
    }

    public boolean isEmpty() {
        return true;
    }
}