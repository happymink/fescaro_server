package com.example.fescaro_chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketFixture {

    private final Socket socket = new Socket("127.0.0.1", 3000);

    public Socket getSocket() {
        return socket;
    }

    public SocketFixture() throws IOException {
    }

    public void sendMessage(String message) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
        out.write(buffer);
        out.flush();
    }

    public String receiveMessage() throws IOException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[512];
        int length = in.read(buffer);
        if (length == -1) throw new IOException();
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }

    public Client client(){
        return new Client(socket);
    }


    public Socket socket() throws IOException {
        return new Socket("127.0.0.1", 3000);
    }
}
