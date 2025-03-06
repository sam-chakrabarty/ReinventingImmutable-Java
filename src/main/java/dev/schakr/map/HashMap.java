package dev.schakr.map;

public class HashMap<A, B> {
    private final IndirectionNode<A, B> root;

    HashMap(IndirectionNode<A, B> root) {
        this.root = root;
    }

    public HashMap() {
        root = IndirectionNode.empty();
    };

    public boolean isEmpty() {
        return root.isEmpty();
    }

}
