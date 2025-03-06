package dev.schakr.map;

import io.vavr.control.Option;

/**
 * The {@code LeafNode} class is an implementation of the {@code Node} interface,
 * representing a non-empty node in a linked structure. Each {@code LeafNode} contains
 * a key-value pair and optionally references the next node in the chain.
 *
 * @param <A> the type of the key contained in the {@code LeafNode}
 * @param <B> the type of the value contained in the {@code LeafNode}
 */
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

    /**
     * Creates and returns a new {@code LeafNode} instance that is a copy of the current node.
     * The copy has the same key, value, and optional link to the next node as the original.
     *
     * @return a new {@code LeafNode} instance with the same key, value, and maybeNext properties as this node
     */
    public LeafNode<A, B> copy() {
        return new LeafNode<>(this.key, this.value, this.maybeNext);
    }

    /**
     * Searches for a {@code LeafNode} in the chain that matches the given key.
     *
     * @param key the key to search for within the current node and potentially its linked next nodes
     * @return an {@code Option} containing the found {@code LeafNode} if a match is found, or an empty {@code Option} if no match exists
     */
    public Option<LeafNode<A, B>> find(A key) {
        if (this.key.equals(key)) return Option.of(this);
        LeafNode<A, B> it = this.maybeNext.getOrNull();
        while(it != null) {
            if (it.key.equals(key)) return Option.of(it);
            it = it.maybeNext.getOrNull();
        }

        return Option.none();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
