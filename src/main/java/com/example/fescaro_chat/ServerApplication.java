package com.example.fescaro_chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApplication extends Application implements EventHandler<ActionEvent> {

    //다양한 클라이언트가 접속했을 때, 쓰레드를 효과적으로 관리하기 위해
    public static ExecutorService threadPool;
    public static Vector<Client> clients = new Vector<>();
    private final String IP = "127.0.0.1";
    private final int port = 9876;
    ServerSocket serverSocket;
    TextArea textArea = new TextArea();
    Button toggleButton;

    public static void main(String[] args) {
        launch();
    }

    // 서버를 구동시켜 클라이언트의 연결을 기다림
    public void startServer(String IP, int port, TextArea textArea) {
        try {
            //todo
            System.out.println("서버 구동");

            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP, port));
        } catch (Exception e) {
            e.printStackTrace();
            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }
        Runnable thread = () -> {
            while(true){
                try{
                    System.out.println("서버 쓰레드 동작");
                    textArea.appendText("클라이언트 접속 대기중.. \n");
                    Socket socket = serverSocket.accept();
                    clients.add(new Client(socket, textArea));
                    textArea.appendText("[IP " + socket.getRemoteSocketAddress() + "] 님이 접속했습니다. " + "\n");
                } catch (Exception e){
                    if(!serverSocket.isClosed()){
                        stopServer();
                    }
                    break;
                }
            }
        };
        System.out.println("쓰레드풀 생성");
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(thread);
    }

    // 서버의 작동을 중지시킴
    public void stopServer() {
        try{
            textArea.appendText("서버가 중지됩니다. \n");
            Iterator<Client> iterator = clients.iterator();

            for (Client client : clients) {
                client.send("server close");
            }

            while (iterator.hasNext()){
                Client client = iterator.next();
                client.send("server close");
                client.socket.close();
                iterator.remove();
            }

            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }

            if(threadPool != null && !threadPool.isShutdown()){
                threadPool.shutdown();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕", 15));
        root.setCenter(textArea);

        toggleButton = new Button("시작하기");
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(toggleButton, new Insets(1, 0,0,0));
        root.setBottom(toggleButton);


        toggleButton.setOnAction(this);
        Scene scene = new Scene(root, 400, 400);
        stage.setTitle("[채팅 서버]");
        stage.setOnCloseRequest(windowEvent -> stopServer());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handle(ActionEvent actionEvent) {

        if(actionEvent.getSource() == toggleButton){
            if(toggleButton.getText().equals("시작하기")){
                startServer(IP, port, textArea);
                Platform.runLater(() -> {
                    String message = String.format("[서버시작]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("종료하기");
                });
            } else {
                stopServer();
                Platform.runLater(() -> {
                    String message = String.format("[서버종료]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("시작하기");
                });
            }
        }
    }
}