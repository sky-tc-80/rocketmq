package com.sky.dev.test;

import com.sky.dev.util.Arrays;

import javax.swing.*;
import java.util.EmptyStackException;

public class ArrayStack {
    private Object[] elementData;
    // 指向栈顶的指针
    private int top;
    // 栈的总容量
    private int size;


    // 默认构造一个容量为10的栈
    public ArrayStack() {
        this.elementData = new Object[10];
        this.top = -1;
        this.size = 10;
    }

    public ArrayStack(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("栈初始容量不能小于0: " + initialCapacity);
        }

        this.elementData = new Object[initialCapacity];
        this.top = -1;
        this.size = initialCapacity;
    }

    public Object push(Object item) {
        isGrow(top + 1);
        elementData[++top] = item;
        return item;
    }

    //弹出栈顶元素
    public Object pop() {
        Object obj = peek();
        remove(top);
        return obj;
    }

    //获取栈顶元素
    public Object peek() {
        if (top == -1) {
            throw new EmptyStackException();
        }
        return elementData[top];
    }

    //判断栈是否为空
    public boolean isEmpty() {
        return (top == -1);
    }

    //删除栈顶元素
    public void remove(int top) {
        //栈顶元素置为null
        elementData[top] = null;
        this.top--;
    }

    public boolean isGrow(int minCapacity) {
        int oldCapacity = size;
        if (minCapacity >= oldCapacity) {
            int newCapacity = 0;
            if ((oldCapacity << 1) - Integer.MAX_VALUE > 0) {
                newCapacity = Integer.MAX_VALUE;
            } else {
                //左移一位，相当于*2
                newCapacity = (oldCapacity << 1);
            }
            this.size = newCapacity;
            elementData = Arrays.copyOf(elementData, size);
            return true;
        } else {
            return false;
        }
    }
}
