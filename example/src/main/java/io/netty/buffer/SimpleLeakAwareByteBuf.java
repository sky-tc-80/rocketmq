/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.buffer;

import io.netty.util.ResourceLeak;

import java.nio.ByteOrder;

/**
 * 首先，Netty 的直接内存 ByteBuf 的数据结构：ByteBuf 对象内部维护一个 java.nio.ByteBuffer 的堆外内存。
 * java.nio.ByteBuffer所占用的实际内存，JVM 虚拟机无法直接干预，JVM虚拟机GC只能回收 ByteBuf 对象本身，
 * 而无法回收 ByteBuf 所指向的堆外内存。
 *
 * Netty封装基本的堆外内存是用 UnpooledDirectByteBuf 对象，Netty 在每次创建一个 UnpooledDirectByteBuf 时，
 * 为了能够追踪到 UnpooledDirectByteBuf 的垃圾回收，需要将该对象用一个虚拟引用指向它，将其注册到一条引用链中。
 * 然后需要将该引用对象与 ByteBuf 对象保存起来，所以 Netty 使用装饰模式，
 * 利用一个包装类 SimpleLeakAwareByteBuf 对象，将原 ByteBuf 包装一下，但对外表现的特性，就是一个ByteBuf

 */
final class SimpleLeakAwareByteBuf extends WrappedByteBuf {

    private final ResourceLeak leak;

    SimpleLeakAwareByteBuf(ByteBuf buf, ResourceLeak leak) {
        super(buf);
        this.leak = leak;
    }

    @Override
    public boolean release() {
        boolean deallocated =  super.release();
        if (deallocated) {
            leak.close();
        }
        return deallocated;
    }

    @Override
    public boolean release(int decrement) {
        boolean deallocated = super.release(decrement);
        if (deallocated) {
            leak.close();
        }
        return deallocated;
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        leak.record();
        if (order() == endianness) {
            return this;
        } else {
            return new SimpleLeakAwareByteBuf(super.order(endianness), leak);
        }
    }

    @Override
    public ByteBuf slice() {
        return new SimpleLeakAwareByteBuf(super.slice(), leak);
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return new SimpleLeakAwareByteBuf(super.slice(index, length), leak);
    }

    @Override
    public ByteBuf duplicate() {
        return new SimpleLeakAwareByteBuf(super.duplicate(), leak);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return new SimpleLeakAwareByteBuf(super.readSlice(length), leak);
    }
}
