package com.sky.dev.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NIOFileClientDemo {
    public static void main(String[] args) {
        try {
            // 1. 创建并配置 服务器套接字通道 ServerSocketChannel
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
            //socketChannel.configureBlocking(false);

            // 2. 从文件输入流中获取文件通道 ( FileChannel )
            FileChannel fileChannel = new FileInputStream("/Users/apple/self/pdf/go语言标准库.pdf").getChannel();
            long startTime = System.currentTimeMillis();

            // 3. 零拷贝传输数据, 注意记录每次拷贝的起始位置
            long transferLen;
            long totalCount = 0;
            // 使用零拷贝将文件数据传到服务器, 循环终止条件是传输结果小于等于 0
            while ((transferLen = fileChannel.transferTo(totalCount, 1024 * 1024 * 32, socketChannel)) > 0) {
                totalCount += transferLen;
            }

            System.out.println("文件传输完毕, 用时 : " + (System.currentTimeMillis() - startTime) + " ms");

            // 4. 关闭连接
            socketChannel.close();
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
