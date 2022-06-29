module com.example.snakejfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.redych.snakejfx to javafx.fxml;
    exports com.redych.snakejfx;
}