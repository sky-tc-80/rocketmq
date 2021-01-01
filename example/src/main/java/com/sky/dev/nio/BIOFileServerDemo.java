package com.sky.dev.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author hongxp
 */
public class BIOFileServerDemo {
    public static void main(String[] args) {
        try {
            // 1. 创建服务器套接字, 并等待客户端连接
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("服务器启动,监听 8888 端口");
            //阻塞, 等待客户端连接请求 ( 此处是第一个阻塞点 )
            Socket socket = serverSocket.accept();
            long startTime = System.currentTimeMillis();
            System.out.println("客户端连接成功");

            // 2. 接收客户端传输的数据, 并写出到文件中
            InputStream inputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream("/Users/apple/self/pdf/go语言标准库11.pdf");
            byte[] buffer = new byte[4096];
            int readLen;
            // 读取的字节个数大于等于 0 才写出数据
            while ((readLen = inputStream.read(buffer)) >= 0) {
                // 写出数据到服务器
                fileOutputStream.write(buffer, 0, readLen);
            }
            System.out.println("文件传输完毕, 用时 : " + (System.currentTimeMillis() - startTime) + " ms");
            // 3. 关闭流
            socket.close();
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
