package com.sky.dev.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOFileServerDemo {
    public static void main(String[] args) {
        try {
            // 1. 创建并配置 服务器套接字通道 ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(8888));
            // 注意这里使用阻塞模式, 不调用该代码
            //serverSocketChannel.configureBlocking(false);
            // 2. 获取文件通道
            FileChannel fileChannel = new FileOutputStream("/Users/apple/self/pdf/go语言标准库12.pdf").getChannel();

            // 3. 阻塞等待
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 4. 零拷贝核心操作
            fileChannel.transferFrom(socketChannel, 0, 1024 * 1024 * 32);

            // 5. 释放资源
            //socketChannel.close();
            //fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
