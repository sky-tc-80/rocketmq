package com.sky.dev.pattern.observer;


/**
 * 观察者主题对象
 */
public interface Subject {

    /**
     * 订阅操作
     */
    void attach(Observer observer);

    /**
     * 取消订阅操作
     */
    void detach(Observer observer);

    /**
     * 通知变动
     */
    void notifyChanged();
}
