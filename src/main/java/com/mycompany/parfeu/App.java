package com.mycompany.parfeu;
import  com.mycompany.parfeu.Model.Rawen.persistence.SharedDataManager ;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    @Override
public void start(Stage stage) throws IOException {
    primaryStage = stage;
    
    SharedDataManager.getInstance(); // Ceci initialise et charge tout
    
    scene = new Scene(loadFXML("/com/mycompany/parfeu/Views/Mahran/mainvue.fxml"), 800, 600);
    stage.setScene(scene);
    stage.setTitle("Firewall Application");
    stage.show();
}

    static void setRoot(String fxmlPath) throws IOException {
        scene.setRoot(loadFXML(fxmlPath));
    }

    private static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlPath));
        return fxmlLoader.load();
    }

    public static void loadScene(String fxmlPath, int width, int height) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene newScene = new Scene(root, width, height);
        primaryStage.setScene(newScene);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();
    }

    public static void loadMainMenu() throws IOException {
        loadScene("/com/mycompany/parfeu/Views/Mahran/mainvue.fxml", 800, 600);
    }

    public static void main(String[] args) {
        launch();
    }
}
