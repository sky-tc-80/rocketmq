package com.sky.dev.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) {
        try {
            //1 . 客户端 SocketChannel : 先获取 SocketChannel, 相当于 BIO 中的 Socket, 设置非阻塞模式
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            //服务器地址 : 服务器的 IP 地址 和 端口号
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8888);

            //2 . 连接服务器 : 连接成功, 返回 true; 连接失败, 返回 false;
            boolean isConnect = socketChannel.connect(address);

            //没有连接成功
            if (!isConnect) {
                while (!socketChannel.finishConnect()) {
                    System.out.println("等待连接成功");
                }
            }

            //当前时刻状态分析 : 执行到该位置, 此时肯定是连接成功了
            System.out.println("服务器连接成功");

            //3 . 发送数据 : 如果连接成功 , 发送数据到服务器端
            ByteBuffer buffer = ByteBuffer.wrap("Hello World".getBytes());
            System.out.println("客户端向服务器端发送数据 \"Hello World\"");
            socketChannel.write(buffer);

            //目的是为了阻塞客户端, 不能让客户端退出
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
