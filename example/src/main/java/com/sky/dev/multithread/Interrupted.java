package com.sky.dev.multithread;

public class Interrupted {
    public static void main(String[] args) {
        Thread sleepThread = new Thread(new SleepRunner(), "SleepThread");
        Thread busyThread = new Thread(new BusyRunner(), "BusyThread");

        busyThread.setDaemon(true);
        sleepThread.start();
        busyThread.start();

        SleepUtils.second(5);
        sleepThread.interrupt();
        busyThread.interrupt();

        System.err.println("SleepThread interrupted is: " + sleepThread.isInterrupted());
        System.err.println("BusyThread interrupted is: " + busyThread.isInterrupted());

        SleepUtils.second(2);
    }

    static class SleepRunner implements Runnable {
        @Override
        public void run() {
            while (true) {
                SleepUtils.second(10);
            }
        }
    }

    static class BusyRunner implements Runnable {
        @Override
        public void run() {
            while (true) {
            }
        }

    }
}
