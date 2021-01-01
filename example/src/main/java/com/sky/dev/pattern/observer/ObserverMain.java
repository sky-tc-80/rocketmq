package com.sky.dev.pattern.observer;

public class ObserverMain {
    public static void main(String[] args) {
        Subject subject = new RealSubject();
        Observer observer = new RealObserver();
        subject.attach(observer);

        subject.notifyChanged();
    }
}
