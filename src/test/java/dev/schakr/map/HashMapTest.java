package dev.schakr.map;

import io.vavr.control.Either;
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

    @Test
    public void WHEN_puttingElement_THEN_shouldSucceed() {
        HashMap<String, String> map = new HashMap<>();
        Either<Throwable, HashMap<String, String>> map1 = map.put("1", "a");
        Either<Throwable, HashMap<String, String>> map2 = map1.get().put("2", "b");
        Either<Throwable, HashMap<String, String>> map3 = map2.get().put("1", "c");

        Assertions.assertTrue(map.isEmpty());
        Assertions.assertTrue(!map1.isEmpty() && map1.isRight());
        Assertions.assertTrue(!map2.isEmpty() && map2.isRight());
        Assertions.assertTrue(!map3.isEmpty() && map3.isRight());
    }
}
