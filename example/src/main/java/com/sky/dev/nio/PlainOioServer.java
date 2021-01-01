package com.sky.dev.nio;


import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @author hongxp
 */
public class PlainOioServer {
    public static void main(String[] args) {
        try {
            new PlainOioServer().server(8081);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void server(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            for (; ; ) {
                final Socket clientSocket = serverSocket.accept();
                System.err.println("Accepted connection from " + clientSocket);

                new Thread(() -> {
                    OutputStream out;
                    try {
                        out = clientSocket.getOutputStream();
                        out.write("Hi!\r\n".getBytes(Charset.forName("utf-8")));
                        out.flush();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            clientSocket.close();
                        } catch (IOException ex) {
                            // ignore on close
                        }
                    }

                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
