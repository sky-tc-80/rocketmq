package com.sky.dev.function;


@FunctionalInterface
public interface Supplier<T> {

    /**
     * 提供初始值
     *
     * @return 初始化的值
     */
    T get();
}
