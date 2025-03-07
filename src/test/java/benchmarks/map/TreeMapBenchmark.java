package benchmarks.map;

import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TreeMapBenchmark {
    @State(Scope.Benchmark)
    public static class InsertionState {
        public TreeMap<String, String> map;

        public String key;
        public String value;

        @Setup(Level.Trial)
        public void setUp() {
            map = new TreeMap<>();
//            Increase map size as you test different capacities for benchmarking.
            initializeMap(1000);
//            initializeMap(10000);
//            initializeMap(100000);
//            initializeMap(1000000);
//            initializeMap(10000000);
        }

        private void initializeMap(int size) {
            for (int i = 0; i < size; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();
                map.put(key, value);
            }
        }

        @Setup(Level.Invocation)
        public void onEach() {
            key = UUID.randomUUID().toString();
            value = UUID.randomUUID().toString();
        }
    }

    @State(Scope.Benchmark)
    public static class QueryState {
        TreeMap<String, String> map;
        HashSet<String> keys;
        String key;

        @Setup(Level.Trial)
        public void setUp() {
            map = new TreeMap<>();
            keys = new HashSet<>();

//            Increase map size as you test different capacities for benchmarking.
            initializeMap(1000);
//            initializeMap(10000);
//            initializeMap(100000);
//            initializeMap(1000000);
//            initializeMap(10000000);
        }

        private void initializeMap(int size) {
            for (int i = 0; i < size; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();
                map.put(key, value);
                keys.add(key);
            }
        }

        @Setup(Level.Invocation)
        public void onEach() {
            key = keys.stream().findAny().get();
        }
    }

    @State(Scope.Benchmark)
    public static class DeleteState {
        TreeMap<String, String> map;
        HashSet<String> keys;
        String key;

        @Setup(Level.Trial)
        public void setUp() {
            map = new TreeMap<>();
            keys = new HashSet<>();

//            Increase map size as you test different capacities for benchmarking.
            initializeMap(1000);
//            initializeMap(10000);
//            initializeMap(100000);
//            initializeMap(1000000);
//            initializeMap(10000000);
        }

        private void initializeMap(int size) {
            for (int i = 0; i < size; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();
                map.put(key, value);
                keys.add(key);
            }
        }

        @Setup(Level.Invocation)
        public void onEach() {
            String newKey = UUID.randomUUID().toString();
            map.put(newKey, UUID.randomUUID().toString());
            keys.add(newKey);
            key = keys.stream().findAny().get();
            keys.remove(key);
        }
    }


    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    @Fork(3)
    @Threads(1)
    @Measurement(iterations = 100000, timeUnit = TimeUnit.NANOSECONDS, batchSize = 1)
    public void TreeMap_Insertions(InsertionState state) {
        state.map.put(state.key, state.value);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    @Fork(3)
    @Threads(1)
    @Measurement(iterations = 100000, timeUnit = TimeUnit.NANOSECONDS, batchSize = 1)
    public void TreeMap_Queries(QueryState state) {
        state.map.get(state.key);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    @Fork(3)
    @Threads(1)
    @Measurement(iterations = 100000, timeUnit = TimeUnit.NANOSECONDS, batchSize = 1)
    public void TreeMap_Deletes(DeleteState state) {
        state.map.remove(state.key);
    }

}
