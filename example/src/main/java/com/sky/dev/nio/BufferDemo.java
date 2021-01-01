package com.sky.dev.nio;

import java.nio.ByteBuffer;

public class BufferDemo {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(888);
        buffer.putDouble(88.888);
        buffer.putShort((short) 888);

        buffer.flip();

        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        int intValue = readOnlyBuffer.getInt();
        double doubleValue = readOnlyBuffer.getDouble();
        short shortValue = readOnlyBuffer.getShort();

        // readOnlyBuffer.getInt(); //BufferUnderflowException
        readOnlyBuffer.putInt(11);
        System.out.println(String.format("intValue = %d, doubleValue = %f, shortValue = %d",
                intValue, doubleValue, shortValue));

    }
}
