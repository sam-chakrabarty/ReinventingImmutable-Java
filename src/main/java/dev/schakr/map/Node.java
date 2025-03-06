package dev.schakr.map;

interface Node<A, B> {
    static <A, B> Node<A, B> empty() {
        return EmptyNode.instance();
    }

    boolean isEmpty();

    final class EmptyNode<A, B> implements Node<A, B> {
        private static final EmptyNode<?, ?> INSTANCE = new EmptyNode<>();
        private EmptyNode() {};

        @SuppressWarnings("unchecked")
        static <A, B> EmptyNode<A, B> instance() {
            return (EmptyNode<A, B>) INSTANCE;
        }

        public boolean isEmpty() { return true; }
    }
}
