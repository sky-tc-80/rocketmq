/*
package com.sky.dev.util;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class MapBenchmark extends SimpleBenchmark {
    private HashMap<Key, Integer> map;
    @Param
    private int mapSize;

    @Override
    protected void setUp() throws Exception {
        map = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; ++i) {
            map.put(Keys.of(i), i);
        }
    }

    public void timeMapGet(int reps) {
        for (int i = 0; i < reps; i++) {
            map.get(Keys.of(i % mapSize));
        }
    }
}*/
