package com.sky.dev.stress;

import com.taobao.stresstester.StressTestUtils;
import com.taobao.stresstester.core.StressTask;

public class Tests01 {
    public static void main(String[] args) {
        StressTestUtils.testAndPrint(100, 1000, new StressTask() {
            @Override
            public Object doTask() throws Exception {
                System.out.println("Do my task.");
                return null;
            }
        });
    }
}
