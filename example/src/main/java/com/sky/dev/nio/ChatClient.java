package com.sky.dev.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class ChatClient {

    /**
     * 服务器地址
     */
    public final static String SERVER_ADDRESS = "127.0.0.1";

    /**
     * 服务器监听的端口号
     */
    public static final int PORT = 8888;

    /**
     * 监听 SocketChannel 通道的 选择器
     */
    private Selector selector;

    /**
     * 服务器端的套接字通道, 相当于 BIO 中的 ServerSocket
     */
    private SocketChannel socketChannel;


    public ChatClient() {
        initClientSocketChannelAndSelector();
    }

    /**
     * 初始化 服务器套接字通道 和
     */
    private void initClientSocketChannelAndSelector() {
        try {
            // 创建并配置 服务器套接字通道 ServerSocketChannel
            socketChannel = SocketChannel.open(new InetSocketAddress(SERVER_ADDRESS, PORT));
            socketChannel.configureBlocking(false);

            // 获取选择器, 并注册 服务器套接字通道 ServerSocketChannel
            selector = Selector.open();
            //注册通道 : 将 SocketChannel 通道注册给 选择器 ( Selector )
            //关注事件 : 关注事件时读取事件, 服务器端从该通道读取数据
            //关联缓冲区 :
            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务器端发送消息
     *
     * @param message
     */
    public void sendMessageToServer(String message) {
        try {
            socketChannel.write(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessageFromServer() {
        // 阻塞监听, 如果有事件触发, 返回触发的事件个数
        // 被触发的 SelectionKey 事件都存放到了 Set<SelectionKey> selectedKeys 集合中
        // 下面开始遍历上述 selectedKeys 集合
        try {
            int eventTriggerCount = selector.select();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 当前状态说明 :
        // 如果能执行到该位置, 说明 selector.select() 方法返回值大于 0
        // 当前有 1 个或多个事件触发, 下面就是处理事件的逻辑

        // 处理事件集合 :
        // 获取当前发生的事件的 SelectionKey 集合, 通过 SelectionKey 可以获取对应的 通道
        Set<SelectionKey> keys = selector.selectedKeys();
        // 使用迭代器迭代, 涉及到删除操作
        Iterator<SelectionKey> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            // 根据 SelectionKey 的事件类型, 处理对应通道的业务逻辑

            // 客户端写出数据到服务器端, 服务器端需要读取数据
            if (key.isReadable()) {
                // 获取 通道 ( Channel ) : 通过 SelectionKey 获取
                SocketChannel socketChannel = (SocketChannel) key.channel();
                // 获取 缓冲区 ( Buffer ) : 获取到 通道 ( Channel ) 关联的 缓冲区 ( Buffer )
                ByteBuffer byteBuffer = (ByteBuffer) key.attachment();

                String message = null;
                try {
                    // 读取客户端传输的数据
                    int readCount = socketChannel.read(byteBuffer);
                    byte[] messageBytes = new byte[readCount];
                    byteBuffer.flip();
                    byteBuffer.get(messageBytes);
                    // 处理读取的消息
                    message = new String(messageBytes);
                    byteBuffer.flip();
                    System.out.println(String.format(message));
                } catch (IOException e) {
                    //e.printStackTrace();
                    // 客户端连接断开
                    key.cancel();
                    try {
                        socketChannel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }// try

            }// if (key.isReadable())

            // 处理完毕后, 当前的 SelectionKey 已经处理完毕
            // 从 Set 集合中移除该 SelectionKey
            // 防止重复处理
            keyIterator.remove();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();

        // 接收服务器端数据线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //不停地从服务器端读取数据
                    client.readMessageFromServer();
                }
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String message = scanner.nextLine();
            client.sendMessageToServer(message);
        }
    }
}

