module com.example.fescaro_chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fescaro_chat to javafx.fxml;
    exports com.example.fescaro_chat;
}