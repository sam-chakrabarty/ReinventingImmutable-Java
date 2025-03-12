package dev.schakr.map;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;

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

    /**
     * Checks if the HashMap is empty.
     *
     * @return true if the HashMap contains no entries, false otherwise.
     */
    public boolean isEmpty() {
        return root.isEmpty();
    }

    /**
     * Checks whether the specified key is present in the HashMap.
     *
     * @param key the key whose presence in the HashMap is to be tested
     * @return true if the HashMap contains the specified key, false otherwise
     */
    public boolean containsKey(A key) {
        return find(key, 0, root).isDefined();
    }

    /**
     * Retrieves the value associated with the specified key, if it exists in the HashMap.
     *
     * @param key the key whose associated value is to be returned
     * @return an {@code Option<B>} containing the value associated with the key if it exists,
     *         or {@code Option.none()} if the key is not found
     */
    public Option<B> get(A key) {
        return find(key, 0, root);
    }

    /**
     * Adds a key-value pair to the HashMap, potentially modifying its internal structure,
     * and returns the updated HashMap wrapped in an Either object indicating success or failure.
     *
     * @param key the key to be added to the HashMap; must not be null
     * @param value the value associated with the specified key; can be null
     * @return an {@code Either<Throwable, HashMap<A, B>>} where the right side contains the updated HashMap
     *         if the addition was successful, or the left side contains a {@code Throwable} if an error occurred
     */
    public Either<Throwable, HashMap<A, B>> put(A key, B value) {
        return insertAtLevel(key, value, 0, root).map(v -> new HashMap<>((IndirectionNode<A, B>) v));
    }


    /**
     * Removes the entry associated with the specified key from the HashMap, if it exists,
     * and returns an updated HashMap wrapped in an {@code Either} object.
     *
     * @param key the key whose associated entry is to be removed; must not be null
     * @return an {@code Either<Throwable, HashMap<A, B>>} where the right side contains the updated HashMap
     *         if the removal was successful, or the left side contains a {@code Throwable} if an error occurred
     */
    public Either<Throwable, HashMap<A, B>> remove(A key) {
        return containsKey(key) ?
                removeAtLevel(key, 0, root).map(t -> t._2 ?
                        new HashMap<>((IndirectionNode<A, B>) t._1) : this) :
                Either.right(this);
    }

    /**
     * Computes a hash value for the given key at a specified level.
     *
     * @param key the input key for which the hash value is to be computed
     * @param level the level used to adjust the hash computation
     * @return the computed hash value for the given key at the specified level
     */
    private int hashAtLevel(A key, int level) {
        return (hash(key) >> (5 * level)) & 0x11111;
    }

    /**
     * Computes a hash value for the given key using its hashCode and an additional
     * transformation to distribute hash codes more evenly.
     *
     * @param key the key for which the hash is to be calculated; can be null
     * @return the hash value, or 0 if the key is null
     */
    private int hash(A key) {
        int h = key.hashCode();
        return (key == null) ? 0 : h ^ (h >> 16);
    }

    /**
     * Finds the corresponding value for the specified key in the provided node structure.
     *
     * @param key The key to look up in the node.
     * @param level The current level of the hash table being searched.
     * @param node The node in which the key is being searched.
     * @return An {@code Option<B>} containing the value associated with the key if found,
     *         or {@code Option.none()} if the key is not found.
     */
    private Option<B> find(A key, int level, Node<A, B> node) {
        if (node.isEmpty()) return Option.none();
        int hash = hashAtLevel(key, level);
        return switch (node) {
            case IndirectionNode<A, B> indirectionNode -> indirectionNode.findNode(hash)
                    .flatMap(n -> find(key, level + 1, n));
            case LeafNode<A, B> leafNode -> leafNode.key.equals(key) ? Option.of(leafNode.value) : Option.none();
            case CollisionNode<A, B> collisionNode -> collisionNode.get(key);
            default -> Option.none();
        };
    }

    /**
     * Inserts a key-value pair into the data structure at the specified level, ensuring the proper
     * organization of nodes based on the key's hash. The method recursively navigates through
     * the structure, creating, updating, or replacing nodes as necessary to maintain integrity.
     * It handles different node types such as {@code IndirectionNode}, {@code CollisionNode},
     * {@code LeafNode}, and {@code EmptyNode}.
     *
     * @param key the key to be added, which determines the position in the structure; must not be null
     * @param value the value associated with the provided key; can be null
     * @param level the current level in the hierarchical structure where the insertion is performed
     * @param parent the {@code IndirectionNode<A, B>} serving as the parent node where the insertion is applied
     * @return an {@code Either<Throwable, Node<A, B>>} where the right side contains the resulting updated
     *         node structure if the operation is successful, or the left side contains a {@code Throwable}
     *         if an error occurs during the operation
     */
    private Either<Throwable, Node<A, B>> insertAtLevel(A key, B value, int level, IndirectionNode<A, B> parent) {
        int hash = hashAtLevel(key, level);

        return parent.findNode(hash).fold(
                () -> insertNode(parent, new LeafNode<>(key, value), hash),
                (Node<A, B> node) -> switch (node) {
                    case IndirectionNode<A, B> indirectionNode ->
                            insertAtLevel(key, value, level + 1, indirectionNode)
                                    .flatMap(a -> updateNode(parent, a, hash));
                    case CollisionNode<A, B> collisionNode -> Either.right(collisionNode.insert(key, value));
                    case LeafNode<A, B> leafNode -> (level == MAX_DEPTH - 1) ?
                            updateNode(parent, extendLeaf(leafNode, key, value), hash) :
                            forkLeaf(leafNode, key, value, level + 1)
                                    .flatMap(n -> updateNode(parent, n, hash));

                    case Node.EmptyNode<?, ?> _ -> insertNode(parent, new LeafNode<>(key, value), hash);

                    default -> Either.left(new IllegalStateException(
                            "Encountered unexpected parent type: " + node.getClass().getSimpleName()));
                });
    }


    /**
     * Removes a node at the specified level within the hierarchy if it matches the given key.
     * Depending on the structure (e.g., `IndirectionNode`, `LeafNode`, `CollisionNode`), this method
     * performs the appropriate operation to remove the key while ensuring the structure's integrity.
     * If successful, it produces an updated version of the parent node with the change applied.
     *
     * @param key the key to be removed from the specified level; must not be null
     * @param level the current level within the hierarchy where the removal operation is performed; must be non-negative
     * @param parent the {@code IndirectionNode<A, B>} containing the nodes being traversed and potentially modified
     * @return an {@code Either<Throwable, Tuple2<Node<A, B>, Boolean>>} where:
     *         - The right side contains a tuple with the updated parent node and a boolean indicating if removal occurred
     *         - The left side contains a {@code Throwable} if an error occurred during the operation
     */
    @SuppressWarnings("unchecked")
    private Either<Throwable, Tuple2<Node<A, B>, Boolean>> removeAtLevel(A key, int level,
                                                                         IndirectionNode<A, B> parent) {
        int hash = hashAtLevel(key, level);
        return parent.findNode(hash).fold(
                () -> Either.right(Tuple.of(parent, false)),
                (Node<A, B> node) -> switch (node) {
                    case IndirectionNode<A, B> indirectionNode -> removeAtLevel(key, level + 1, indirectionNode)
                            .flatMap(t -> t._2 ?
                                    updateNode(parent, t._1, hash).map(n -> Tuple.of(n, true)) :
                                    Either.right(Tuple.of(parent, false)));

                    case LeafNode<A, B> leafNode -> leafNode.key.equals(key) ?
                            updateNode(parent, Node.empty(), hash).map(n -> Tuple.of(n, true)) :
                            Either.right(Tuple.of(parent, false));

                    case CollisionNode<A, B> collisionNode -> (collisionNode.contains(key)) ?
                            updateNode(parent, collisionNode.delete(key), hash).map(a -> Tuple.of(a, true)) :
                            Either.right(Tuple.of(parent, false));

                    case Node.EmptyNode<?, ?> emptyNode -> updateNode(parent, (Node<A, B>) emptyNode, hash)
                            .map(a -> Tuple.of(a, true));

                    default -> Either.left(new IllegalStateException(
                            "Encountered unexpected node type: " + node.getClass().getSimpleName()));
                }
        );
    }

    /**
     * Updates a specified child node within the given parent node based on the provided hash value.
     * If the hash value corresponds to an invalid index, an {@code Either} containing a {@code Throwable} is returned.
     * If the child node is empty, the method removes the corresponding node from the parent and updates its bitmap.
     * Otherwise, it replaces the child at the specified location, updates the bitmap, and returns the modified parent node.
     *
     * @param parent the {@code IndirectionNode<A, B>} that contains the child node to be updated
     * @param child the {@code Node<A, B>} representing the new child node to update within the parent
     * @param hash an integer hash value used to determine the position of the child node within the parent
     * @return an {@code Either<Throwable, Node<A, B>>} where the right side contains the updated parent node
     *         if the operation succeeds, or the left side contains a {@code Throwable} if an error occurs
     */
    private Either<Throwable, Node<A, B>> updateNode(IndirectionNode<A, B> parent, Node<A, B> child, int hash) {
        int index = parent.getIndex(hash, true);
        if (index < 0 || index >= parent.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid update index: " + index));

        if (child.isEmpty()) {
            List<Node<A, B>> updatedNodes = parent.nodes.removeAt(index);
            int updatedBitmap = parent.bitmap & ~(1 << hash);
            return (updatedBitmap == 0) ? Either.right(IndirectionNode.empty()) :
                    Either.right(new IndirectionNode<>(updatedNodes, updatedBitmap));
        } else {
            List<Node<A, B>> updatedNodes = parent.nodes.update(index, child);
            int updatedBitmap = parent.bitmap | (1 << hash);
            return Either.right(new IndirectionNode<>(updatedNodes, updatedBitmap));
        }
    }

    /**
     * Inserts a child node into the specified parent node using the given hash for positioning,
     * and returns either the updated parent node or a {@code Throwable} if an error occurs.
     *
     * @param parent the {@code IndirectionNode<A, B>} in which the child node is to be inserted
     * @param child the {@code Node<A, B>} to be inserted into the parent
     * @param hash the hash value used to determine the position of the child node within the parent
     * @return an {@code Either<Throwable, Node<A, B>>} where the right side contains the updated parent node
     *         if the insertion is successful, or the left side contains a {@code Throwable} in case of an error
     */
    private Either<Throwable, Node<A, B>> insertNode(IndirectionNode<A, B> parent, Node<A, B> child, int hash) {
        int index = parent.getIndex(hash, false);
        if (index < 0 || index > parent.nodes.size())
            return Either.left(new IndexOutOfBoundsException("Invalid insert index: " + index));

        List<Node<A, B>> updatedNodes = (index == parent.nodes.size()) ?
                parent.nodes.append(child) :
                parent.nodes.insert(index, child);

        return Either.right(new IndirectionNode<>(updatedNodes, (parent.bitmap | (1 << hash))));
    }

    /**
     * Creates a new node by extending an existing leaf node with the specified key-value pair.
     * If the key in the existing leaf node matches the provided key, a new leaf node with
     * the updated value is returned. Otherwise, a collision node is created with the existing
     * key-value pair and the new key-value pair.
     *
     * @param node the existing {@code LeafNode<A, B>} to extend; must not be null
     * @param key the key to be added or updated in the node; must not be null
     * @param value the value associated with the provided key; can be null
     * @return a {@code Node<A, B>} instance that is either a new {@code LeafNode<A, B>}
     *         or a {@code CollisionNode<A, B>} containing both key-value pairs
     */
    private Node<A, B> extendLeaf(LeafNode<A, B> node, A key, B value) {
        if (node.key.equals(key)) return new LeafNode<>(key, value);
        return new CollisionNode<>(node, key, value);
    }


    /**
     * Attempts to fork the structure of the given leaf node in the hierarchy by creating
     * a new node that represents a split at a specified level. This method ensures
     * that the new key-value pair is either stored in a new leaf or appropriately added
     * to the hierarchy, creating intermediate nodes as necessary.
     *
     * @param leaf the original {@code LeafNode<A, B>} that needs to be forked
     * @param key the key to be added to the structure; must not be null
     * @param value the value associated with the provided key; can be null
     * @param level the current level in the hierarchy where the operation is being performed
     * @return an {@code Either<Throwable, Node<A, B>>} where the right side contains the updated node
     *         structure if the operation succeeds, or the left side contains a {@code Throwable}
     *         if an error occurs during the operation
     */
    private Either<Throwable, Node<A, B>> forkLeaf(LeafNode<A, B> leaf, A key, B value, int level) {
        if (leaf.key.equals(key)) return Either.right(new LeafNode<>(key, value));

        return insertAtLevel(leaf.key, leaf.value, level, IndirectionNode.empty())
                .flatMap(node -> insertAtLevel(key, value, level, (IndirectionNode<A, B>) node));
    }

}
