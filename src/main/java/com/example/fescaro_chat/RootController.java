package com.example.fescaro_chat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class RootController  {

    @FXML
    TextArea textArea = new TextArea();

    @FXML
    Button toggleButton = new Button("시작하기");

    @FXML
    private Label welcomeText;

    @FXML
    protected void startServer() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

}
