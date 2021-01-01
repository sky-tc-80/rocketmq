package com.sky.dev.proxy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Client {
    public static void main(String[] args) throws NoSuchMethodException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Tank t = new Tank();
        InvocationHandler h = new TimeHandler(t);
        Movable m = (Movable) Proxy.newProxyInstance(Movable.class, h);
        m.move();
    }
}
