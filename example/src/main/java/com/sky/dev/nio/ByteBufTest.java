package com.sky.dev.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buf = Unpooled.directBuffer(512);
        System.out.println(buf);
        // SimpleLeakAwareByteBuf(UnpooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 512))
        // SimpleLeakAwareByteBuf是包装类，使用了装饰模式，内部维护一个UnpooledUnsafeDirectByteBuf,
        // 该类与UnpooledDirectByteBuf类似。首先在创建SimpleLeakAwareByteBuf的时候，会将该引用加入到内存泄漏检测
        // 的引用链中。
        try {
            //使用业务

        } finally {
            buf.release();//该方法首先调用直接内存UnpooledDirectByteBuf方法，释放所占用的堆外内存，
            //然后调用leak.close方法，通知内存泄漏检测程序，该引用所指向的堆外内存已经释放，没有泄漏。
            //如果 release没有调用，则当UnpooledDirectBytebuf被垃圾回收器收集号，该ByteBuf
            //申请的堆外内存将再也不受应用程序所掌控了，会引起内存泄漏。
            /*
             *
             * public boolean release() {
                    boolean deallocated =  super.release();
                    if (deallocated) {
                        leak.close();
                    }
                    return deallocated;
                }
             */
        }
    }
}
