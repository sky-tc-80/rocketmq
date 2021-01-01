package io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

public class PoolBuffTest {
    public static void main(String[] args) {
        ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

        //tiny规格内存分配 会变成大于等于16的整数倍的数：这里254 会规格化为256
        ByteBuf byteBuf = alloc.directBuffer(254);
        byteBuf.writeInt(126);
        System.err.println(byteBuf.readInt());

        byteBuf.release();
    }
}
