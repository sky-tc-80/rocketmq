package com.sky.dev.function;


@FunctionalInterface
public interface BiFunction<R, T, U> {

    /**
     * 函数式接口
     * 类似于 z = F(x,y)
     */
    R apply(T t, U u);
}
