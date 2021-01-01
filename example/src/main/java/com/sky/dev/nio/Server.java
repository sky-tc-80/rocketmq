package com.sky.dev.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author hongxp
 */
public class Server {
    public static void main(String[] args) {

        try {

            //I . 创建 ServerSocketChannel 监听 8888 端口
            //创建 ServerSocketChannel, 等价于 BIO 中的 ServerSocket
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //绑定本地端口, 获取其内部封装的 ServerSocket, 绑定 ServerSocket 的 8888 端口
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(8888));

            //设置网络通信非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //II . 创建选择器, 并注册监听事件
            //获取 选择器 ( Selector )
            Selector selector = Selector.open();
            //将 serverSocketChannel 通道注册给 选择器 ( Selector ), 这里注册连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //III . 选择器监听
            //选择器 ( Selector ) 开始监听
            while (true) {
                // III . 1 . 判定事件触发 :
                // 阻塞监听, 查看是否有事件触发, 如果有就在下面处理
                // 如果没有 continue 终止循环, 继续下一次循环
                // 阻塞 操作 , 直到其所关联的 通道 ( Channel ) 有一个有事件发生
                System.out.println("服务器端开始阻塞监听 8888 端口事件");
                if (selector.selectNow() <= 0) {
                    continue;
                }

                //当前状态说明 :
                //如果能执行到该位置, 说明 selector.select(1000) 方法返回值大于 0
                //当前有 1 个或多个事件触发, 下面就是处理事件的逻辑

                //III . 2 . 处理事件集合 :
                //获取当前发生的事件的 SelectionKey 集合, 通过 SelectionKey 可以获取对应的 通道
                Set<SelectionKey> keys = selector.selectedKeys();

                //使用迭代器迭代, 涉及到删除操作
                Iterator<SelectionKey> keyIterator = keys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    //根据 SelectionKey 的事件类型, 处理对应通道的业务逻辑

                    //III . 2 . ( 1 ) 客户端连接服务器, 服务器端需要执行 accept 操作
                    if (key.isAcceptable()) {
                        System.out.println("服务器端 选择器 ( Selector ) 监听到客户端连接事件");
                        //创建通道 : 为该客户端创建一个对应的 SocketChannel 通道
                        //不等待 : 当前已经知道有客户端连接服务器, 因此不需要阻塞等待
                        //非阻塞方法 : ServerSocketChannel 的 accept() 是非阻塞的方法
                        SocketChannel sc = serverSocketChannel.accept();
                        //如果 ServerSocketChannel 是非阻塞的, 这里的 SocketChannel 也要设置成非阻塞的
                        //否则会报 java.nio.channels.IllegalBlockingModeException 异常
                        sc.configureBlocking(false);

                        //注册通道 : 将 SocketChannel 通道注册给 选择器 ( Selector )
                        //关注事件 : 关注事件时读取事件, 服务器端从该通道读取数据
                        //关联缓冲区 :
                        sc.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                    }

                    //III . 2 . ( 2 ) 客户端写出数据到服务器端, 服务器端需要读取数据
                    if (key.isReadable()) {
                        System.out.println("服务器端 选择器 ( Selector ) 监听到客户发送数据事件");
                        //获取 通道 ( Channel ) : 通过 SelectionKey 获取
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        //获取 缓冲区 ( Buffer ) : 获取到 通道 ( Channel ) 关联的 缓冲区 ( Buffer )
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        //读取客户端传输的数据 ;
                        socketChannel.read(byteBuffer);
                        System.out.println("客户端向服务器端发送数据 : " + new String(byteBuffer.array()));
                    }

                    //处理完毕后, 从 Set 集合中移除该 SelectionKey
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
