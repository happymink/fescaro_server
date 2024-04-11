package com.example.fescaro_chat;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    Socket socket;
    TextArea textArea;

    public Client(Socket socket, TextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
        receive();
    }

    //클라이언트로부터 메시지를 전달 받음
    public void receive() {
        Runnable thread = () -> {
            try {
                while (true) {
                    InputStream in = socket.getInputStream();
                    byte[] buffer = new byte[512];
                    int length = in.read(buffer);

                    System.out.println("[메세지 수신 성공] "
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                    String message = new String(buffer, 0, length, StandardCharsets.UTF_8);

                    System.out.println("메세지 :" +message);

                    if(message.equals("exit")){
                        textArea.appendText("[IP "+socket.getRemoteSocketAddress() + "] 님이 접속을 종료했습니다.\n");
                        socket.close();
                        ServerApplication.clients.remove(this);
                        break;
                    }

                    for (Client client : ServerApplication.clients) {
                        client.send(message);
                    }

                }
            } catch (IOException e) {
                try {
                    System.out.println("[메세지 수신 오류] "
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }
        };
        ServerApplication.threadPool.submit(thread);
    }

     void send(String message) {

        Runnable thread = () -> {
            try {
                OutputStream out = socket.getOutputStream();
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
            } catch (Exception e) {
                try {
                    System.out.println("[메세지 송신 오류] "
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                    ServerApplication.clients.remove(Client.this);
                    socket.getOutputStream().flush();
                    socket.close();
                } catch (IOException ex) {
                    e.printStackTrace();
                }
            }
        };
        ServerApplication.threadPool.submit(thread);
    }
}
