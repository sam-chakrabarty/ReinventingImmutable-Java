package dev.schakr.map;

import java.util.Optional;

class LeafNode<A, B> implements Node<A, B> {
    final A key;
    final B value;
    final Optional<LeafNode<A, B>> maybeNext;

    LeafNode(A a, B b) {
        this.key = a;
        this.value = b;
        this.maybeNext = Optional.empty();
    }

    LeafNode(A a, B b, LeafNode<A, B> next) {
        this.key = a;
        this.value = b;
        this.maybeNext = Optional.ofNullable(next);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
