package com.sky.dev.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author hongxp
 */
public class MappedByteBufferDemo {
    public static void main(String[] args) {
        RandomAccessFile file = null;

        try {
            file = new RandomAccessFile("/Users/apple/self/code/rocketmq/example/src/main/resources/tt.txt",
                    "rw");
            FileChannel fc = file.getChannel();
            //FileChannel.MapMode.READ_WRITE : 指的是读写模式
            //0 : 将文件从 0 位置开始映射到内存中
            //10 : 将文件从 0 位置开始映射到内存中的大小
            //即 将 file.txt 文件从 0 开始的 10 字节映射到内存中
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, 10);
            byteBuffer.put(0, (byte) 'N');
            byteBuffer.put(1, (byte) 'Y');

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
