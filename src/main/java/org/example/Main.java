package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX App
 */
public class Main extends Application {

    // Метод определения графического интерфейса, в котором мы обращаемся к .fxml файлу.
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("primary.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 630, 380);
        stage.setTitle("Hashtable");
        stage.getIcons().add(new Image(Objects.requireNonNull
                (Main.class.getResourceAsStream("icon.png"))));
        stage.setScene(scene);
        stage.show();
    }

    // Точка входа в программу. Метод из которого запускается программа.
    public static void main(String[] args) {
        launch();
    }

}