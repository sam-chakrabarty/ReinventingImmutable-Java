package dev.schakr.map;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.util.function.Function;

/**
 * Represents a high-performance, immutable HashMap implementation that supports
 * key-value mapping while ensuring structural sharing and efficient updates.
 * The HashMap provides a series of operations for insertion, removal, and retrieval
 * of key-value pairs. Modelled after the Hash Array Mapped Trie (HAMT) data structure.
 *
 * @param <A> the type of keys used in this HashMap
 * @param <B> the type of values associated with the keys in this HashMap
 */
public class HashMap<A, B> {
    private final IndirectionNode<A, B> root;
    private final int MAX_DEPTH = 6;

    HashMap(IndirectionNode<A, B> root) {
        this.root = root;
    }

    public HashMap() {
        root = IndirectionNode.empty();
    };

    public boolean isEmpty() {
        return root.isEmpty();
    }

    /**
     * Checks if the specified key exists in the HashMap.
     *
     * @param key the key to check for existence in the HashMap
     * @return {@code true} if the key exists in the HashMap, {@code false} otherwise
     */
    public boolean containsKey(A key) {
        return findNode(key, 0, root).isDefined();
    }

    /**
     * Retrieves the value associated with the specified key in the HashMap.
     *
     * @param key the key whose associated value is to be returned
     * @return an {@code Option} containing the value associated with the key,
     *         or {@code Option.none()} if the key is not present in the HashMap
     */
    public Option<B> get(A key) {
        return findNode(key, 0, root).map(v -> v.value);
    }

    /**
     * Inserts the specified key-value pair into the HashMap and returns a result indicating success or failure.
     * A new HashMap is returned with the updated key-value pair if the operation is successful.
     *
     * @param key the key to be inserted into the HashMap
     * @param value the value associated with the specified key
     * @return an {@code Either} containing a {@code Throwable} if an error occurs during the insertion,
     *         or a {@code HashMap<A, B>} with the updated key-value pair upon success
     */
    public Either<Throwable, HashMap<A, B>> put(A key, B value) {
        return insert(key, value, 0, root).map(v -> new HashMap<>((IndirectionNode<A, B>) v));
    }

    /**
     * Removes the specified key from the HashMap, if it exists.
     * If the key is found and removed successfully, returns a new HashMap instance with the updates.
     * Otherwise, returns the current HashMap wrapped in an {@code Either.right}.
     *
     * @param key the key to be removed from the HashMap
     * @return an {@code Either} containing a {@code Throwable} in case of an error during removal,
     *         or a new {@code HashMap<A, B>} without the specified key upon success
     */
    public Either<Throwable, HashMap<A, B>> remove(A key) {
        return containsKey(key) ?
                remove(key, 0, root).map(n -> new HashMap<>((IndirectionNode<A, B>) n)) :
                Either.right(this);
    }

    /**
     * Recursively finds a {@code LeafNode} associated with the specified key in the HashMap
     * starting from the provided {@code Node}.
     *
     * @param key the key to locate within the structure
     * @param level the current depth or level in the node hierarchy
     * @param node the starting node for the search, which can be an {@code IndirectionNode} or {@code LeafNode}
     * @return an {@code Option} containing the found {@code LeafNode} if present,
     *         or {@code Option.none()} if the key is not found or the structure is empty
     */
    private Option<LeafNode<A, B>> findNode(A key, int level, Node<A, B> node) {
        if (node.isEmpty()) return Option.none();
        int hash = (key.hashCode() >> (5 * level)) & 0b11111;
        return switch (node) {
            case IndirectionNode<A, B> indirectionNode -> indirectionNode.findNode(hash)
                    .flatMap(n -> findNode(key, level + 1, n));
            case LeafNode<A, B> leafNode -> leafNode.find(key);
            default -> Option.none();
        };
    }

    /**
     * Removes the specified key from the indirection node at the given level of the hash map hierarchy.
     * This method traverses through the structure, attempting to locate and remove the key, and
     * updates the node accordingly. Handles different node types, including indirection nodes,
     * leaf nodes, and empty nodes.
     *
     * @param key the key to be removed from the hash map
     * @param level the current depth or level in the hash map hierarchy
     * @param node the indirection node from which the key is to be removed
     * @return an {@code Either} containing a {@code Throwable} if an error occurs during the removal,
     *         or an updated {@code Node<A, B>} after successfully removing the key
     */
    @SuppressWarnings("unchecked")
    private Either<Throwable, Node<A, B>> remove(A key, int level, IndirectionNode<A, B> node) {
        int hash = (key.hashCode() >> (5 * level)) & 0b11111;

        return node.findNode(hash).fold(
                () -> Either.right(node),
                (Node<A, B> collisionNode) -> switch (collisionNode) {
                    case IndirectionNode<A, B> indirectionNode -> remove(key, level + 1, indirectionNode)
                            .flatMap(a -> updateNode(node, a, hash));

                    case LeafNode<A, B> leafNode -> leafNode.find(key).fold(
                            () -> Either.<Throwable, Node<A, B>>right(leafNode),
                            (_) ->  Either.<Throwable, Node<A, B>>right(removeLeaf(key, leafNode)))
                            .flatMap(a -> updateNode(node, a, hash));

                    case Node.EmptyNode<?, ?> emptyNode -> updateNode(node, (Node<A, B>) emptyNode, hash);

                    default -> Either.left(new IllegalStateException(
                            "Encountered unexpected node type: " + collisionNode.getClass().getSimpleName()));
                }
        );
    }

    /**
     * Removes a leaf node from the linked structure associated with the specified key.
     * If the key matches the provided leaf node's key, it replaces the node with the next node in the chain,
     * or returns an empty node if there is no next node.
     * Otherwise, it updates the chain by removing the key at the matched position and preserving the rest.
     *
     * @param key the key of the leaf node to be removed
     * @param leafNode the current {@code LeafNode} to begin the removal process
     * @return a {@code Node} representing the updated structure after removal of the key,
     *         or an empty node if the key's leaf node was the only element in the chain
     */
    private Node<A, B> removeLeaf(A key, LeafNode<A, B> leafNode) {
        if (leafNode.key.equals(key)) {
            return leafNode.maybeNext.fold(Node::empty, Function.identity());
        } else {
            Option<LeafNode<A, B>> updatedNext = leafNode.maybeNext
                    .map(n -> removeLeaf(key, n))
                    .filter(n -> !n.isEmpty())
                    .map(n -> (LeafNode<A, B>) n);

            return new LeafNode<>(leafNode.key, leafNode.value, updatedNext);
        }
    }

    /**
     * Inserts a key-value pair into the given indirection node at the specified level of the hash map structure.
     * Handles collisions by either extending, forking, or updating nodes based on the provided key, value, and level.
     *
     * @param key the key to be inserted
     * @param value the value associated with the specified key
     * @param level the current depth or level in the hash map hierarchy
     * @param node the indirection node where the key-value pair is to be inserted
     * @return an {@code Either} containing a {@code Throwable} if the operation fails, or an updated {@code Node<A, B>}
     *         containing the new key-value pair upon success
     */
    private Either<Throwable, Node<A, B>> insert(A key, B value, int level, IndirectionNode<A, B> node) {
        int hash = (key.hashCode() >> (5 * level)) & 0b11111;

        return node.findNode(hash).fold(
                () -> insertNode(node, new LeafNode<>(key, value), hash),
                (Node<A, B> collisionNode) -> switch (collisionNode) {
                    case IndirectionNode<A, B> indirectionNode -> insert(key, value, level + 1, indirectionNode)
                            .flatMap(a -> updateNode(node, a, hash));

                    case LeafNode<A, B> leafNode -> (level == MAX_DEPTH - 1) ?
                            updateNode(node, extendLeaf(leafNode, key, value), hash) :
                            forkLeaf(leafNode, key, value, level + 1)
                                    .flatMap(a -> updateNode(node, a, hash));

                    case Node.EmptyNode<?, ?> _ -> insertNode(node, new LeafNode<>(key, value), hash);

                    default -> Either.left(new IllegalStateException(
                            "Encountered unexpected node type: " + collisionNode.getClass().getSimpleName()));
                });
    }

    /**
     * Updates a node within the specified {@code IndirectionNode} by replacing or removing
     * a child node at the index computed using the provided hash. This may result in a new
     * {@code IndirectionNode} with an updated bitmap and list of child nodes or an empty node
     * if all entries are removed.
     *
     * @param node the {@code IndirectionNode} containing the current child nodes
     * @param child the new child node to replace the existing node at the specified index,
     *              or an empty node to indicate removal
     * @param hash the hash value used to calculate the index in the bitmap for updating the child node
     * @return an {@code Either} containing a {@code Throwable} in case of an invalid index or
     *         an updated {@code Node<A, B>} with the replaced or removed child node
     */
    private Either<Throwable, Node<A, B>> updateNode(IndirectionNode<A, B> node, Node<A, B> child, int hash) {
        int index = node.getIndex(hash, true);
        if (index < 0 || index >= node.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid update index: " + index));

        if (child.isEmpty()) {
            List<Node<A, B>> updatedNodes = node.nodes.removeAt(index);
            int updatedBitmap = node.bitmap & ~(1 << hash);
            return (updatedBitmap == 0) ? Either.right(IndirectionNode.empty()) :
                    Either.right(new IndirectionNode<>(updatedNodes, updatedBitmap));
        } else {
            List<Node<A, B>> updatedNodes = node.nodes.update(index, child);
            int updatedBitmap = node.bitmap | (1 << hash);
            return Either.right(new IndirectionNode<>(updatedNodes, updatedBitmap));
        }
    }

    /**
     * Inserts a child node into the specified {@code IndirectionNode} at the index computed
     * based on the provided hash. If the hash's index is invalid (e.g., out of bounds),
     * an error is returned. Otherwise, a new {@code IndirectionNode} containing the updated
     * list of nodes is returned.
     *
     * @param node the {@code IndirectionNode} where the child node is to be inserted
     * @param child the new child node to insert into the indirection node
     * @param hash the hash value used to determine the index for inserting the child node
     * @return an {@code Either} containing a {@code Throwable} if an error occurs (e.g., invalid index),
     *         or an updated {@code Node<A, B>} with the inserted child node upon success
     */
    private Either<Throwable, Node<A, B>> insertNode(IndirectionNode<A, B> node, Node<A, B> child, int hash) {
        int index = node.getIndex(hash, false);
        if (index < 0 || index > node.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid insert index: " + index));

        List<Node<A, B>> updatedNodes = (index == node.nodes.size()) ?
                node.nodes.append(child) :
                node.nodes.insert(index, child);

        return Either.right(new IndirectionNode<>(updatedNodes, (node.bitmap | (1 << hash))));
    }

    /**
     * Extends the provided {@code LeafNode} by either updating the value associated with
     * the given key if it exists, or inserting a new key-value pair into the chain of nodes.
     *
     * @param node the {@code LeafNode} to be extended, which serves as the starting node for the operation
     * @param key the key to be updated or inserted into the node chain
     * @param value the value associated with the specified key
     * @return a new {@code LeafNode} representing the updated structure with the key-value pair
     */
    private LeafNode<A, B> extendLeaf(LeafNode<A, B> node, A key, B value) {
        boolean keyExists = node.find(key).isDefined();
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
     * Handles the forking operation on a {@code LeafNode} when a collision occurs.
     * If the key of the given {@code LeafNode} matches the provided key, a new {@code LeafNode}
     * is created with the updated value. Otherwise, the method uses the {@code insert} operation
     * to handle the collision based on the provided key, value, and level.
     *
     * @param leaf the {@code LeafNode} to be forked
     * @param key the key to be inserted or updated
     * @param value the value associated with the specified key
     * @param level the current depth or level in the hash map hierarchy
     * @return an {@code Either} containing a {@code Throwable} if an error occurs during the operation,
     *         or a {@code Node<A, B>} representing the forked or updated structure
     */
    private Either<Throwable, Node<A, B>> forkLeaf(LeafNode<A, B> leaf, A key, B value, int level) {
        if (leaf.key.equals(key)) return Either.right(new LeafNode<>(key, value, leaf.maybeNext));

        return insert(leaf.key, leaf.value, level, IndirectionNode.empty())
                .flatMap(node -> insert(key, value, level, (IndirectionNode<A, B>) node))
                .map(v -> (IndirectionNode<A, B>) v);
    }

}
