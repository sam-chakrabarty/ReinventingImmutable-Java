package dev.schakr.map;


import io.vavr.collection.List;

class IndirectionNode<A, B> implements Node<A, B> {
    final List<Node<A, B>> nodes;
    final int bitmap;

    final static IndirectionNode<?, ?> EMPTY = new IndirectionNode<>();

    @SuppressWarnings("unchecked")
    static <A, B> IndirectionNode<A, B> empty() {
        return (IndirectionNode<A, B>) EMPTY;
    }

    private IndirectionNode() {
        this.nodes = List.empty();
        this.bitmap = 0;
    }

    IndirectionNode(List<Node<A, B>> nodes, int bitmap) {
        this.nodes = nodes;
        this.bitmap = bitmap;
    }

    @Override
    public boolean isEmpty() {
        return bitmap == 0;
    }

}
