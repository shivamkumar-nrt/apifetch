package com.apiforge.studio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load main FXML
            URL fxmlUrl = getClass().getResource("/com/apiforge/studio/main.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Could not find main.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            Scene scene = new Scene(root, 1280, 840);

            // Load CSS stylesheet
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("WARNING: Could not find styles.css stylesheet!");
            }

            // Set app window icon (taskbar + title bar)
            URL logoUrl = getClass().getResource("/com/apiforge/studio/logo.png");
            if (logoUrl != null) {
                primaryStage.getIcons().add(new Image(logoUrl.toExternalForm()));
            }

            primaryStage.setTitle("APIForge Studio — Advanced API Client");
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
