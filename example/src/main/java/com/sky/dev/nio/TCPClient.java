package com.sky.dev.nio;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author hongxp
 */
public class TCPClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket();
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(
                            Inet4Address.getLocalHost(),
                            8888
                    );
            System.out.println("客户端开始连接 ...");
            //此处会阻塞等待连接成功
            socket.connect(inetSocketAddress);
            System.out.println("客户端连接成功");
            //连接成功后, 开始执行后续操作
            socket.getOutputStream().write("Hello World".getBytes());
            System.out.println("客户端写出 Hello World 成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
