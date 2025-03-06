package dev.schakr.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class HashMapTest {

    @Test
    public void WHEN_initialized_THEN_shouldBeEmpty() {
        HashMap<String, String> map = new HashMap<>();
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void WHEN_initializedWithEmptyRoot_THEN_shouldBeEmpty() {
        IndirectionNode<String, String> root = new IndirectionNode<>(new LinkedList<>(), 0);
        HashMap<String, String> map = new HashMap<>(root);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void WHEN_initializedWithNonEmptyRoot_THEN_shouldNotBeEmpty() {
        LinkedList<Node<String, String>> nodes = new LinkedList<>();
        nodes.add(new LeafNode<>("foo", "bar"));

        IndirectionNode<String, String> root = new IndirectionNode<>(nodes, 1);
        HashMap<String, String> map = new HashMap<>(root);
        Assertions.assertFalse(map.isEmpty());
    }
}
