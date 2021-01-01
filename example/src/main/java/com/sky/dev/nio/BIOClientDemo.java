package com.sky.dev.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BIOClientDemo {
    public static void main(String[] args) {
        try {
            // 客户端与服务器端连接过程忽略, 主要是分析数据拷贝过程
            Socket socket = new Socket();
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(Inet4Address.getLocalHost(), 8888);
            socket.connect(inetSocketAddress);

            // 分析下面过程中, 数据拷贝次数, 和用户态与内核态的转换次数
            // 1. 从文件中读取数据
            FileInputStream fileInputStream = new FileInputStream("rocketmq-example.iml");
            byte[] buffer = new byte[1024];

            // 首先将硬盘中的文件, 进行 DMA 拷贝, 此处对应 read 方法, 
            // 将文件数据从硬盘中拷贝到 内核缓冲区  ( 用户态切换成内核态 )
            // 将内核缓冲区中的数据, 通过 CPU 拷贝 方式, 拷贝到 用户缓冲区  ( 内核态切换成用户态 )
            int readLen = fileInputStream.read(buffer);

            // 2. 写出数据到服务器
            // 将用户缓冲区中的数据, 再次通过 CPU 拷贝方式, 拷贝到 Socket 缓冲区 ( 用户态切换成内核态 )
            // 再次使用 DMA 拷贝, 将 Socket 缓冲区中的数据拷贝到 协议栈 ( Protocol Engine ) 中
            socket.getOutputStream().write(buffer, 0, readLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
