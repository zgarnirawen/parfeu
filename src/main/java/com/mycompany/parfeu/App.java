package com.mycompany.parfeu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée principal de l'application Pare-feu.
 * Lance l'interface graphique avec la vue principale.
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Charger la vue principale (mainvue.fxml)
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/mycompany/parfeu/Views/Mahran/mainvue.fxml")
        );
        
        Scene scene = new Scene(loader.load(), 800, 600);
        
        // Configuration de la fenêtre
        stage.setTitle("🔥 Firewall Intelligent - Système de Sécurité Réseau");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        
        // Icône (optionnel)
        // stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon.png")));
        
        stage.show();
        
        System.out.println("✓ Application Pare-feu démarrée avec succès!");
    }

    /**
     * Méthode utilitaire pour changer de scène.
     * @param fxmlPath chemin vers le fichier FXML
     * @param width largeur de la fenêtre
     * @param height hauteur de la fenêtre
     */
    public static void loadScene(String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), width, height);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    /**
     * Méthode utilitaire pour changer de scène (taille par défaut).
     */
    public static void loadScene(String fxmlPath) throws IOException {
        loadScene(fxmlPath, 800, 600);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}