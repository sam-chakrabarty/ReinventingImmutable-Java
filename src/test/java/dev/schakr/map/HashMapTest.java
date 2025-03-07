package dev.schakr.map;

import io.vavr.collection.List;
import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HashMapTest {

    @Test
    public void WHEN_initialized_THEN_shouldBeEmpty() {
        HashMap<String, String> map = new HashMap<>();
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void WHEN_initializedWithEmptyRoot_THEN_shouldBeEmpty() {
        IndirectionNode<String, String> root = new IndirectionNode<>(List.empty(), 0);
        HashMap<String, String> map = new HashMap<>(root);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void WHEN_initializedWithNonEmptyRoot_THEN_shouldNotBeEmpty() {
        IndirectionNode<String, String> root = new IndirectionNode<>(
                List.of(new LeafNode<>("foo", "bar")), 1);
        HashMap<String, String> map = new HashMap<>(root);
        Assertions.assertFalse(map.isEmpty());
    }

    @Test
    public void WHEN_puttingElements_THEN_shouldSucceed() {
        HashMap<String, String> map = new HashMap<>();
        Either<Throwable, HashMap<String, String>> map1 = map.put("1", "a");
        Either<Throwable, HashMap<String, String>> map2 = map1.get().put("2", "b");
        Either<Throwable, HashMap<String, String>> map3 = map2.get().put("1", "c");

        Assertions.assertTrue(map.isEmpty());
        Assertions.assertTrue(!map1.isEmpty() && map1.isRight());
        Assertions.assertTrue(!map2.isEmpty() && map2.isRight());
        Assertions.assertTrue(!map3.isEmpty() && map3.isRight());
    }

    @Test
    public void WHEN_puttingElements_THEN_shouldContainValues() {
        var map = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .get();

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertTrue(map.containsKey("1"));
        Assertions.assertEquals("a", map.get("1").get());
        Assertions.assertTrue(map.containsKey("2"));
        Assertions.assertEquals("b", map.get("2").get());
    }

    @Test
    public void WHEN_overwritingElements_THEN_shouldContainValues() {
        var map = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .flatMap(m -> m.put("1", "c"))
                .get();

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertTrue(map.containsKey("1"));
        Assertions.assertEquals("c", map.get("1").get());
        Assertions.assertTrue(map.containsKey("2"));
        Assertions.assertEquals("b", map.get("2").get());
    }

    @Test
    public void WHEN_overwritingElements_THEN_shouldPersistOldValues() {
        var map1 = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .get();

        var map2 = map1.put("1", "c").get();

        Assertions.assertFalse(map1.isEmpty());
        Assertions.assertTrue(map1.containsKey("1"));
        Assertions.assertEquals("a", map1.get("1").get());
        Assertions.assertTrue(map1.containsKey("2"));
        Assertions.assertEquals("b", map1.get("2").get());

        Assertions.assertFalse(map2.isEmpty());
        Assertions.assertTrue(map2.containsKey("1"));
        Assertions.assertEquals("c", map2.get("1").get());
        Assertions.assertTrue(map2.containsKey("2"));
        Assertions.assertEquals("b", map2.get("2").get());
    }

    @Test
    public void WHEN_removingElements_THEN_shouldNotContainValues() {
        var map = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .flatMap(m -> m.remove("1"))
                .get();

        Assertions.assertFalse(map.isEmpty());
        Assertions.assertFalse(map.containsKey("1"));
        Assertions.assertTrue(map.containsKey("2"));
        Assertions.assertEquals("b", map.get("2").get());
    }

    @Test
    public void WHEN_removingElements_THEN_shouldPersistOldValues() {
        var map1 = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .get();
        var map2 = map1.remove("1").get();

        Assertions.assertFalse(map1.isEmpty());
        Assertions.assertTrue(map1.containsKey("1"));
        Assertions.assertEquals("a", map1.get("1").get());
        Assertions.assertTrue(map1.containsKey("2"));
        Assertions.assertEquals("b", map1.get("2").get());

        Assertions.assertFalse(map2.isEmpty());
        Assertions.assertFalse(map2.containsKey("1"));
        Assertions.assertTrue(map2.containsKey("2"));
        Assertions.assertEquals("b", map2.get("2").get());
    }

    @Test
    public void WHEN_removingAllElements_THEN_shouldBeEmpty() {
        var map = new HashMap<String, String>()
                .put("1", "a")
                .flatMap(m -> m.put("2", "b"))
                .flatMap(m -> m.remove("1"))
                .flatMap(m -> m.remove("2"))
                .get();

        Assertions.assertTrue(map.isEmpty());
    }
}
