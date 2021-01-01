package com.sky.dev.sort;

import com.sky.dev.util.Integers;
import com.sky.dev.util.Times;

public class DefaultSort {
    public static void main(String[] args) {
        Integer[] arr1 = Integers.tailAscOrder(1, 10000, 2000);
        Integer[] arr2 = Integers.copy(arr1);
        Integer[] arr3 = Integers.copy(arr1);
        Times.test("bubbleSort1", () -> {
            bubbleSort1(arr1);
        });
        Times.test("bubbleSort2", () -> {
            bubbleSort2(arr2);
        });
        Times.test("bubbleSort3", () -> {
            bubbleSort3(arr3);
        });

        Integer[] arr4 = Integers.random(50, 1, 100);
        Integer[] arr5 = Integers.copy(arr4);

        Integers.println(arr4);
        Times.test("SelectionSort", () -> {
            selectionSort(arr4);
        });
        Integers.println(arr4);

        Integers.println(arr5);
        Times.test("BubbleSort2", () -> {
            selectionSort(arr5);
        });
        Integers.println(arr5);
    }

    public static void selectionSort(Integer[] array) {
        for (int end = array.length - 1; end > 0; end--) {
            int maxIndex = end;
            for (int begin = 0; begin <= end; begin++) {
                if (array[maxIndex] < array[begin]) {
                    maxIndex = begin;
                }
            }
            // 交换
            int tmp = array[maxIndex];
            array[maxIndex] = array[end];
            array[end] = tmp;
        }
    }

    public static void bubbleSort1(Integer[] array) {
        for (int end = array.length - 1; end > 0; end--) {
            for (int begin = 1; begin <= end; begin++) {
                if (array[begin] < array[begin - 1]) {
                    int tmp = array[begin];
                    array[begin] = array[begin - 1];
                    array[begin - 1] = tmp;

                }
            }
        }
    }

    public static void bubbleSort2(Integer[] array) {
        for (int end = array.length - 1; end > 0; end--) {
            boolean sorted = true;
            for (int begin = 1; begin <= end; begin++) {
                if (array[begin] < array[begin - 1]) {
                    int tmp = array[begin];
                    array[begin] = array[begin - 1];
                    array[begin - 1] = tmp;
                    sorted = false;
                }
            }

            if (sorted) break;
        }
    }

    public static void bubbleSort3(Integer[] array) {
        for (int end = array.length - 1; end > 0; end--) {
            // 初始值在完全有序的情况下有用
            int lastSwapIndex = 0;
            for (int begin = 1; begin <= end; begin++) {
                if (array[begin] < array[begin - 1]) {
                    int tmp = array[begin];
                    array[begin] = array[begin - 1];
                    array[begin - 1] = tmp;
                    lastSwapIndex = begin;
                }
            }
            end = lastSwapIndex;
        }
    }
}
