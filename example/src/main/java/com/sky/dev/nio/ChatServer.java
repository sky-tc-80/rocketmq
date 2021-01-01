package com.sky.dev.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;


/**
 * 聊天室服务器端
 */
public class ChatServer {

    /**
     * 服务器监听的端口号
     */
    public static final int PORT = 8888;

    /**
     * 监听 ServerSocketChannel 通道和各个客户端对应的 SocketChannel 通道
     */
    private Selector selector;

    /**
     * 服务器端的套接字通道, 相当于 BIO 中的 ServerSocket
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * 初始化服务器相关操作
     */
    public ChatServer() {
        initServerSocketChannelAndSelector();
    }

    /**
     * 初始化 服务器套接字通道 和
     */
    private void initServerSocketChannelAndSelector() {
        try {
            // 创建并配置 服务器套接字通道 ServerSocketChannel
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            // 获取选择器, 并注册 服务器套接字通道 ServerSocketChannel
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selector 开始执行 监听工作
     */
    private void selectorStartSelectOperation() {
        System.out.println("服务器端启动监听 :");
        while (true) {
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

                // 客户端连接服务器, 服务器端需要执行 accept 操作
                if (key.isAcceptable()) {
                    //创建通道 : 为该客户端创建一个对应的 SocketChannel 通道
                    //不等待 : 当前已经知道有客户端连接服务器, 因此不需要阻塞等待
                    //非阻塞方法 : ServerSocketChannel 的 accept() 是非阻塞的方法
                    SocketChannel socketChannel = null;
                    try {
                        socketChannel = serverSocketChannel.accept();
                        //如果 ServerSocketChannel 是非阻塞的, 这里的 SocketChannel 也要设置成非阻塞的
                        //否则会报 java.nio.channels.IllegalBlockingModeException 异常
                        socketChannel.configureBlocking(false);

                        //注册通道 : 将 SocketChannel 通道注册给 选择器 ( Selector )
                        //关注事件 : 关注事件时读取事件, 服务器端从该通道读取数据
                        //关联缓冲区 :
                        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                        System.out.println(String.format("用户 %s 进入聊天室", socketChannel.getRemoteAddress()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                // 客户端写出数据到服务器端, 服务器端需要读取数据
                if (key.isReadable()) {
                    // 获取 通道 ( Channel ) : 通过 SelectionKey 获取
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // 获取 缓冲区 ( Buffer ) : 获取到 通道 ( Channel ) 关联的 缓冲区 ( Buffer )
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();

                    String remoteAddress = null;
                    String message = null;
                    try {
                        // 读取客户端传输的数据
                        int readCount = socketChannel.read(byteBuffer);
                        byte[] messageBytes = new byte[readCount];
                        byteBuffer.flip();
                        byteBuffer.get(messageBytes);
                        // 处理读取的消息
                        message = new String(messageBytes);
                        //重置以便下次使用
                        byteBuffer.flip();
                        remoteAddress = socketChannel.getRemoteAddress().toString();
                        System.out.println(String.format("%s : %s", remoteAddress, message));
                    } catch (IOException e) {
                        //e.printStackTrace();
                        // 如果此处出现异常, 说明该客户端离线了, 服务器提示, 取消选择器上的注册信息, 关闭通道
                        try {
                            System.out.println(String.format("%s 用户离线 !", socketChannel.getRemoteAddress()));
                            key.cancel();
                            socketChannel.close();
                            //继续下一次循环
                            continue;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    // 向其它客户端转发消息, 发送消息的客户端自己就不用再发送该消息了
                    // 遍历所有注册到 选择器 Selector 的 SocketChannel
                    Set<SelectionKey> selectionKeys = selector.keys();
                    for (SelectionKey selectionKey : selectionKeys) {
                        // 获取客户端对应的 套接字通道
                        // 这里不能强转成 SocketChannel, 因为这里可能存在 ServerSocketChannel
                        Channel channel = selectionKey.channel();
                        // 将自己排除在外, 注意这里是地址对比, 就是这两个类不能是同一个地址的类
                        // 这个类的类型必须是 SocketChannel, 排除之前注册的 ServerSocketChannel 干扰
                        if (socketChannel != channel && channel instanceof SocketChannel) {
                            // 将通道转为 SocketChannel, 之后将字符串发送到客户端
                            SocketChannel clientSocketChannel = (SocketChannel) channel;

                            // 写出字符串到其它客户端
                            try {
                                clientSocketChannel.write(ByteBuffer.wrap((remoteAddress + " : " + message).getBytes()));
                            } catch (IOException e) {
                                //e.printStackTrace();
                                // 如果此处出现异常, 说明该客户端离线了, 服务器提示, 取消选择器上的注册信息, 关闭通道
                                try {
                                    System.out.println(String.format("%s 用户离线 !", clientSocketChannel.getRemoteAddress()));
                                    selectionKey.cancel();
                                    clientSocketChannel.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        }

                    }

                }

                // 处理完毕后, 当前的 SelectionKey 已经处理完毕
                // 从 Set 集合中移除该 SelectionKey
                // 防止重复处理
                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.selectorStartSelectOperation();
    }
}

