package com.sky.dev.tree;

public class SearchUtils {
    public static int binarySearch(int[] num, int number) {
        if (num == null || num.length == 0)
            return -1;


        int start, end, mid;
        start = 0;
        end = num.length - 1;

        while (start <= end) {
            mid = (start + end) / 2;
            if (num[mid] == number)
                return mid;
            else if (num[mid] > number) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        return -1;
    }

    public static int RecursiveBinarySearch(int num[], int start, int end, int key) {
        int mid = (start + end) / 2;
        return mid;
    }
}
