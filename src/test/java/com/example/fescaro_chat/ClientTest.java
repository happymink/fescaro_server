package com.example.fescaro_chat;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

class ClientTest {

    SocketFixture socketFixture = new SocketFixture();

    ClientTest() throws IOException {
    }

    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() throws IOException {

        Client client = socketFixture.client();

    }
}