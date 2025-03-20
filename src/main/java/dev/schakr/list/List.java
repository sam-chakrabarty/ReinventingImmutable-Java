package dev.schakr.list;

import io.vavr.NotImplementedError;
import io.vavr.control.Either;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Work in Progress
 * @param <T>
 */
public class List<T> {
    private Node<T> head = Node.empty();

    private List() {}

    private List(Node<T> head) {
        this.head = head;
    }

    public static <T> List<T> of(T... values) {
        Iterator<T> it = Arrays.stream(values).iterator();
        return new List<>(prepend(it, Node.empty()));
    }

    public static <T> List<T> ofAll(Iterable<T> values) {
        Iterator<T> it = values.iterator();
        return new List<>(prepend(it, Node.empty()));
    }

    public boolean isEmpty() {
        return head.isEmpty();
    }

    public List<T> add(T value) {
        return head.isEmpty() ?
                new List<>(new ListNode<>(value)) :
                new List<>(new ListNode<>(value, (ListNode<T>) head));
    }

    public T get(int index) {
        throw new NotImplementedError();
    }

    public Either<Throwable, Boolean> remove(int index) {
        throw new NotImplementedError();
    }

    public boolean contains(T value) {
        throw new NotImplementedError();
    }

    private static <T> Node<T> prepend(Iterator<T> it, Node<T> acc) {
        if (!it.hasNext()) return acc;
        T value = it.next();
        return acc.isEmpty() ?
                prepend(it, new ListNode<>(value)) :
                prepend(it, new ListNode<>(value, (ListNode<T>) acc));
    }

}
