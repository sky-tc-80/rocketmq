package com.sky.dev.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileTest {
    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("/Users/apple/self/code/rocketmq/example/src/main/java/com/sky/dev/test/FileTest.java");
            FileChannel channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);

            channel.close();
            fis.close();
            /*byte[] cache = new byte[1024];
            int read = fis.read(cache);

            System.err.println("read length: " + read);
            fis.close();*/



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
