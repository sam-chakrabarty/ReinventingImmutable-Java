package dev.schakr.map;

import io.vavr.collection.List;
import io.vavr.control.Either;

public class HashMap<A, B> {
    private final IndirectionNode<A, B> root;
    private final int MAX_LEVEL = 6;

    HashMap(IndirectionNode<A, B> root) {
        this.root = root;
    }

    public HashMap() {
        root = IndirectionNode.empty();
    };

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public Either<Throwable, HashMap<A, B>> put(A key, B value) {
        return insert(key, value, 0, root).map(v -> new HashMap<>((IndirectionNode<A, B>) v));
    }

    private Either<Throwable, Node<A, B>> insert(A key, B value, int level, IndirectionNode<A, B> node) {
        int hash = (key.hashCode() >> 5 * level) & 0b11111;

        if (collisionExists(node.bitmap, hash)) {
            Node<A, B> collisionNode = getNode(node.bitmap, hash, node.nodes);

            return switch (collisionNode) {
                case IndirectionNode<A, B> indirectionNode -> insert(key, value, level + 1, indirectionNode)
                        .flatMap(a -> updateNode(node, a, hash));

                case LeafNode<A, B> leafNode -> (level == MAX_LEVEL - 1) ?
                        updateNode(node, extendLeaf(leafNode, key, value), hash) :
                        forkLeaf(leafNode, key, value, level + 1)
                                .flatMap(a -> updateNode(node, a, hash));

                case Node.EmptyNode<?, ?> _ -> insertNode(node, new LeafNode<>(key, value), hash);

                default -> Either.left(new IllegalStateException(
                        "Encountered unexpected node type: " + collisionNode.getClass().getSimpleName()));
            };
        } else {
            return insertNode(node, new LeafNode<>(key, value), hash);
        }
    }

    private boolean collisionExists(int bitmap, int hash) {
        return ((bitmap >> hash) & 0b1) == 1;
    }


    /**
     * Fetch the relevant node by getting its index from the bitmap.
     * @param bitmap
     * @param hash
     * @param nodes
     * @return
     */
    private Node<A, B> getNode(int bitmap, int hash, List<Node<A, B>> nodes) {
        return nodes.get(getIndex(bitmap, hash));
    }

    /**
     * Get the index of a node from its chunked hash (5 bits) using the bitmap.
     * @param bitmap
     * @param hash
     * @return
     */
    private int getIndex(int bitmap, int hash) {
        int index = (hash == 0) ? 0 : bitmap & 1;
        for (int i = 1; i < hash; i++) {
            bitmap = bitmap >> 1;
            index += bitmap & 1;
        }

        return index;
    }

    /**
     * Replace an existing node K/V pair with a new child node.
     * @param node
     * @param child
     * @param hash
     * @return
     */
    private Either<Throwable, Node<A, B>> updateNode(IndirectionNode<A, B> node, Node<A, B> child, int hash) {
        int index = getIndex(node.bitmap, hash);
        if (index < 0 || index >= node.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid update index: " + index));

        List<Node<A, B>> updatedNodes = node.nodes.update(index, child);
        return Either.right(new IndirectionNode<>(updatedNodes, (node.bitmap | (1 << hash))));
    }

    /**
     * Insert a new child node K/V pair.
     * @param node
     * @param child
     * @param hash
     * @return
     */
    private Either<Throwable, Node<A, B>> insertNode(IndirectionNode<A, B> node, Node<A, B> child, int hash) {
        int index = getIndex(node.bitmap, hash);
        if (index < 0 || index > node.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid insert index: " + index));

        List<Node<A, B>> updatedNodes = (index == node.nodes.size()) ?
                node.nodes.append(child) :
                node.nodes.insert(index, child);

        return Either.right(new IndirectionNode<>(updatedNodes, (node.bitmap | (1 << hash))));
    }

    /**
     * Extend a leaf node to support a new K/V pair.
     * @param node
     * @param key
     * @param value
     * @return
     */
    private LeafNode<A, B> extendLeaf(LeafNode<A, B> node, A key, B value) {
        boolean keyExists = keyExists(node, key);
        if (keyExists) {
            LeafNode<A, B> head = new LeafNode<>(node);
            LeafNode<A, B> it = head;
            while (it.maybeNext.isDefined()) {
                A nextKey = it.maybeNext.get().key;
                if (nextKey.equals(key)) {
                    it.maybeNext = it.maybeNext.map(a -> new LeafNode<>(key, value, a.maybeNext));
                    break;
                } else {
                    it.maybeNext = it.maybeNext.map(LeafNode::copy);
                }
                it = it.maybeNext.get();
            }

            return head.maybeNext.get();
        } else {
            return new LeafNode<>(key, value, node);
        }
    }

    /**
     * Check whether a key exists in a LeafNode chain.
     * @param node
     * @param key
     * @return
     */
    private boolean keyExists(LeafNode<A, B> node, A key) {
        LeafNode<A, B> it = new LeafNode<>(node);

        while (it.maybeNext.isDefined()) {
            A nextKey = it.maybeNext.get().key;
            if (nextKey.equals(key)) return true;
            it = it.maybeNext.get();
        }

        return false;
    }

    /**
     * Forks an existing LeafNode to support a new colliding K/V node.
     * @param leaf
     * @param key
     * @param value
     * @param level
     * @return
     */
    private Either<Throwable, Node<A, B>> forkLeaf(LeafNode<A, B> leaf, A key, B value, int level) {
        A leafKey = leaf.key;
        B leafValue = leaf.value;

        if (leafKey.equals(key)) {
            return Either.right(new LeafNode<>(key, value, leaf.maybeNext));
        }

        return insert(leafKey, leafValue, level, IndirectionNode.empty())
                .flatMap(node -> insert(key, value, level, (IndirectionNode<A, B>) node))
                .map(v -> (IndirectionNode<A, B>) v);
    }

}
