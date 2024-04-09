package com.example.fescaro_chat;

import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApplication extends Application implements EventHandler<ActionEvent> {

    public static ExecutorService threadPool;
    public static Vector<Client> clients = new Vector<>();
    private final String IP = "127.0.0.1";
    private final int port = 3000;
    private ServerSocket serverSocket;
    private TextArea textArea = new TextArea();
    private Button toggleButton;
    private PauseTransition pause = new PauseTransition(Duration.millis(350));

    public static void main(String[] args) {
        launch();
    }

    public void startServer(String IP, int port, TextArea textArea) {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP, port));
        } catch (Exception e) {
            toggleButton.setText("접속하기");
            textArea.appendText("IP 주소가 이미 사용중입니다\n");
            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }
        Runnable thread = () -> {
            while(true){
                try{
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
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(thread);
    }

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
        stage.setTitle("GUI ChatServer");
        stage.setOnCloseRequest(windowEvent -> stopServer());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handle(ActionEvent actionEvent) {

        if(actionEvent.getSource() == toggleButton){
            if(toggleButton.getText().equals("시작하기")){
                toggleButton.setDisable(true);
                pause.setOnFinished(action -> toggleButton.setDisable(false));
                pause.play();
                startServer(IP, port, textArea);
                Platform.runLater(() -> {
                    toggleButton.setText("종료하기");
                });
            } else {
                toggleButton.setDisable(true);
                pause.setOnFinished(action -> toggleButton.setDisable(false));
                pause.play();
                Platform.runLater(() -> {
                    toggleButton.setText("시작하기");
                });
                stopServer();
            }
        }
    }
}