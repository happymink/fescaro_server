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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientApplication extends Application implements EventHandler<ActionEvent> {

    private Socket socket;
    private TextArea textArea;
    private int port = 3000;
    private Button connectionButton;
    private Button sendButton;
    private TextField IPText;
    private TextField portText;
    private TextField userName;
    private TextField input;
    private PauseTransition pause = new PauseTransition(Duration.millis(350));

    public static void main(String[] args) {
        launch();
    }

    private void startClient(String IP, int port) {
        Thread thread = new Thread(() -> {
            try {
                socket = new Socket(IP, port);
                receive();
            } catch (Exception e) {
                if (socket == null){
                    stopClient();
                    Platform.runLater(() -> textArea.appendText("[서버 연결 실패]\n"));
                    connectionButton.setText("접속하기");
                    input.setDisable(true);
                    sendButton.setDisable(true);
                } else if (!socket.isConnected()) {
                    Platform.runLater(() -> textArea.appendText("[서버 연결 실패]\n"));
                    stopClient();
                    Platform.exit();
                }
            }
        });
        thread.start();
    }

    private void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                OutputStream out = socket.getOutputStream();
                byte[] buffer = ("exit").getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCloseMessage(String message) throws IOException {
        if(message.equals("server close")){
            Platform.runLater(() -> textArea.appendText("서버가 중지됩니다.\n"));
            Platform.runLater(() ->connectionButton.setText("접속하기"));
            Platform.runLater(() -> textArea.appendText("[채팅방 퇴장]\n"));
            stopClient();
            socket.close();
            input.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    private void receive() {
        while (true) {
            try {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[512];
                int length = in.read(buffer);
                if (length == -1) throw new IOException();
                String message = new String(buffer, 0, length, StandardCharsets.UTF_8);

                checkCloseMessage(message);
                Platform.runLater(() -> textArea.appendText(message));

            } catch (Exception e) {
                stopClient();
                break;
            }
        }
    }

    private void send(String message) {
        Thread thread = new Thread(() -> {
            try {
                OutputStream out = socket.getOutputStream();
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
            } catch (Exception e) {
                stopClient();
            }
        });
        thread.start();
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        HBox hBox = new HBox();
        hBox.setSpacing(10);

        userName = new TextField();
        userName.setPrefWidth(100);
        userName.setPromptText("이름을 입력하세요");
        HBox.setHgrow(userName, Priority.ALWAYS);

        IPText = new TextField("127.0.0.1");
        portText = new TextField("3000");
        portText.setPrefWidth(80);

        hBox.getChildren().addAll(userName, IPText, portText);
        root.setTop(hBox);

        textArea = new TextArea();
        textArea.setEditable(false);
        root.setCenter(textArea);

        input = new TextField();
        input.setPrefWidth(Double.MAX_VALUE);
        input.setDisable(true);

        input.setOnAction(this);

        sendButton = new Button("전송");
        sendButton.setDisable(true);

        sendButton.setOnAction(this);

        connectionButton = new Button("접속하기");
        connectionButton.setOnAction(this);

        BorderPane pane = new BorderPane();
        pane.setLeft(connectionButton);
        pane.setCenter(input);
        pane.setRight(sendButton);

        root.setBottom(pane);
        Scene scene = new Scene(root, 800, 400);
        stage.setTitle("GUI ChatClient");
        stage.setOnCloseRequest(windowEvent -> stopClient());
        stage.setScene(scene);

        stage.show();

        connectionButton.requestFocus();
    }

    private void setBtnAfterBtnAction(Button button, String message){
        button.setDisable(true);
        pause.setOnFinished(action -> button.setDisable(false));
        pause.play();
        button.setText(message);

        if(message.equals("종료하기")){
            input.setDisable(false);
            sendButton.setDisable(false);
            input.requestFocus();
        }
        if(message.equals("접속하기")){
            input.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    @Override
    public void handle(ActionEvent actionEvent) {

        if (actionEvent.getSource() == connectionButton) {
            if (connectionButton.getText().equals("접속하기")) {

                setBtnAfterBtnAction(connectionButton, "종료하기");
                try {
                    port = Integer.parseInt(portText.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startClient(IPText.getText(), port);
                Platform.runLater(() -> textArea.appendText("[채팅방 접속]\n"));
            } else {
                setBtnAfterBtnAction(connectionButton, "접속하기");
                stopClient();
                Platform.runLater(() -> textArea.appendText("[채팅방 퇴장]\n"));
            }
        }

        if (actionEvent.getSource() == sendButton) {
            send("["+userName.getText() + "] : " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        }

        if (actionEvent.getSource() == input) {
            send("["+userName.getText() + "] : " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        }
    }
}