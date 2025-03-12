package dev.schakr.map;

import io.vavr.collection.List;
import io.vavr.control.Option;

/**
 * The IndirectionNode class represents a node structure in a hierarchy where it links to
 * multiple child nodes while utilizing a bitmap for efficient lookup and storage.
 *
 * @param <A> the key type for the nodes
 * @param <B> the value type for the nodes
 */
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

    /**
     * Checks whether the provided hash value is present within the bitmap.
     *
     * @param hash the hash value to check within the bitmap
     * @return true if the hash is present in the bitmap, false otherwise
     */
    boolean containsHash(int hash) {
        return (((bitmap >> hash) & 1) == 1);
    }

    /**
     * Finds and retrieves the node associated with the specified hash value.
     * If the hash is present in the bitmap, the corresponding node is returned.
     * Otherwise, an empty optional value is returned.
     *
     * @param hash the hash value used to locate the node
     * @return an {@code Option<Node<A, B>>} containing the node if it exists, or an empty {@code Option} if it does not
     */
    Option<Node<A, B>> findNode(int hash) {
        return containsHash(hash) ? Option.of(nodes.get(getIndex(hash, true))) : Option.none();
    }

    /**
     * Calculates the index associated with the given hash within the bitmap.
     * If `shouldExist` is true and the hash is not present in the bitmap, the method will return -1.
     *
     * @param hash the hash value used to determine the index within the bitmap
     * @param shouldExist a flag indicating whether the hash must exist in the bitmap
     * @return the index corresponding to the provided hash in the bitmap, or -1 if the hash does not exist and `shouldExist` is true
     */
    int getIndex(int hash, boolean shouldExist) {
        if (shouldExist && !containsHash(hash)) return -1;
        return Integer.bitCount(bitmap & ((1 << hash) - 1));
    }

}
