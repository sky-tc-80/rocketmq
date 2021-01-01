package com.sky.dev.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author hongxp
 */
public class BufferDemo4 {
    public static void main(String[] args) {
        FileOutputStream fos = null;
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(8888);
            ssc.socket().bind(address);
            //2 . 创建 2 个 ByteBuffer, 并放入数组中
            ByteBuffer[] buffers = new ByteBuffer[2];
            buffers[0] = ByteBuffer.allocate(4);
            buffers[1] = ByteBuffer.allocate(8);

            SocketChannel sc = ssc.accept();
            sc.read(buffers);

            buffers[0].flip();
            buffers[1].flip();


            fos = new FileOutputStream("/Users/apple/self/code/rocketmq/example/src/main/resources/tt.txt");
            FileChannel fc = fos.getChannel();
            fc.write(buffers);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
