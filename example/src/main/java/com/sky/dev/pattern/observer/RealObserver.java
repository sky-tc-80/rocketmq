package com.sky.dev.pattern.observer;

public class RealObserver implements Observer {
    @Override
    public void update() {
        System.err.println(this.getClass() + ": 接收到了通知");
    }
}
