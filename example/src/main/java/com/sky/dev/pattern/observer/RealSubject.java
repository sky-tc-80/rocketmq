package com.sky.dev.pattern.observer;

import com.sky.dev.util.ArrayList;

import java.util.List;

public class RealSubject implements Subject {
    private List<Observer> observerList = new ArrayList<>();

    @Override
    public void attach(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notifyChanged() {
        observerList.forEach(ob -> ob.update());
    }
}
