package com.sky.dev.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author hongxp
 */
public class BIOFileClientDemo {
    public static void main(String[] args) {
        try {
            // 1. 客户端连接服务器
            Socket socket = new Socket();
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(Inet4Address.getLocalHost(), 8888);
            socket.connect(inetSocketAddress);
            System.out.println("客户端连接服务器成功, 开始传输文件 ...");
            long startTime = System.currentTimeMillis();

            // 2. 从文件中读取数据数据并传给服务器
            FileInputStream fileInputStream = new FileInputStream("/Users/apple/self/pdf/go语言标准库11.pdf");
            byte[] buffer = new byte[4096];

            int readLen;
            // 读取的字节个数大于等于 0 才写出数据
            while ((readLen = fileInputStream.read(buffer)) >= 0) {
                // 写出数据到服务器
                socket.getOutputStream().write(buffer, 0, readLen);
            }
            System.out.println("文件传输完毕, 用时 : " + (System.currentTimeMillis() - startTime) + " ms");

            //3. 关闭连接
            socket.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
