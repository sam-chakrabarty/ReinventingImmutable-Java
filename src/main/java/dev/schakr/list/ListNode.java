package dev.schakr.list;

import io.vavr.control.Option;

public class ListNode<T> implements Node<T> {
    final T value;
    final Option<ListNode<T>> maybeNext;

    ListNode(T value, ListNode<T> next) {
        this.value = value;
        this.maybeNext = Option.of(next);
    }

    ListNode(T value) {
        this.value = value;
        this.maybeNext = Option.none();
    }

    public ListNode<T> withNext(ListNode<T> next) {
        return new ListNode<>(value, next);
    }

    public boolean isEmpty() {
        return false;
    }
}
