package com.sky.dev.sort;

import com.sky.dev.util.Integers;
import com.sky.dev.util.Times;

public class InsertionSort<E extends Comparable<E>> extends Sort<E> {
    public static void main(String[] args) {
        Integer[] array = Integers.random(10000, 1, 20000);
        Integer[] array1 = Integers.copy(array);

        Integers.println(array);
        Times.test("insertion sort", () -> {
            new InsertionSort<Integer>().sort(array);
        });
        Integers.println(array);
    }

    @Override
    protected void sort() {
        for (int begin = 1; begin < array.length; begin++) {
            int cur = begin;
            while (cur > 0 && cmp(cur, cur - 1) < 0) {
                swap(cur, cur - 1);
                cur--;
            }
        }
    }

    protected void sort2() {
        for (int begin = 1; begin < array.length; begin++) {
            int cur = begin;
            E v = array[cur];
            while (cur > 0 && cmp(v, array[cur - 1]) < 0) {
                // 比v大的往后移动一位
                array[cur] = array[cur - 1];
                cur--;
            }
            array[cur] = v;
        }
    }
}
