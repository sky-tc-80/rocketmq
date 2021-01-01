package com.sky.dev.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnSafes {
    public static Unsafe getUnsafe()   {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            return unsafe;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
