package dev.schakr.map;

import io.vavr.control.Option;

class LeafNode<A, B> implements Node<A, B> {
    final A key;
    final B value;
    Option<LeafNode<A, B>> maybeNext = Option.none();

    LeafNode(A a, B b) {
        this.key = a;
        this.value = b;
        this.maybeNext = Option.none();
    }

    LeafNode(A a, B b, LeafNode<A, B> next) {
        this.key = a;
        this.value = b;
        this.maybeNext = Option.of(next);
    }

    LeafNode(A a, B b, Option<LeafNode<A, B>> maybeNext) {
        this.key = a;
        this.value = b;
        this.maybeNext = maybeNext;
    }

    LeafNode(LeafNode<A, B> next) {
        this.key = null;
        this.value = null;
        this.maybeNext = Option.of(next);
    }

    public LeafNode<A, B> copy() {
        return new LeafNode<>(this.key, this.value, this.maybeNext);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
