package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue principale (menu).
 * Gère la navigation vers les différentes sections de l'application.
 */
public class MainvueController implements Initializable {

    @FXML
    private Button generateBtn;
    
    @FXML
    private Button statisticsBtn;
    
    @FXML
    private Button historyBtn;
    
    @FXML
    private Button configurationBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupButtonActions();
        System.out.println("✓ MainvueController initialisé");
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupButtonActions() {
        // Bouton Générer Paquet
        generateBtn.setOnAction(event -> {
            try {
                App.loadScene("/com/mycompany/parfeu/Views/Mahran/paquetgenerator.fxml", 800, 700);
            } catch (IOException e) {
                showError("Erreur de navigation", "Impossible de charger la vue Générateur de Paquets");
                e.printStackTrace();
            }
        });

        // Bouton Statistiques
        statisticsBtn.setOnAction(event -> {
            try {
                App.loadScene("/com/mycompany/parfeu/Views/Rawen/statistics.fxml", 900, 700);
            } catch (IOException e) {
                showError("Erreur de navigation", "Impossible de charger la vue Statistiques");
                e.printStackTrace();
            }
        });

        // Bouton Historique (Blockchain)
        historyBtn.setOnAction(event -> {
            try {
                App.loadScene("/com/mycompany/parfeu/Views/Rawen/blockchain.fxml", 850, 800);
            } catch (IOException e) {
                showError("Erreur de navigation", "Impossible de charger la vue Blockchain");
                e.printStackTrace();
            }
        });

        // Bouton Configuration
        configurationBtn.setOnAction(event -> {
            try {
                App.loadScene("/com/mycompany/parfeu/Views/Mahran/configuration.fxml", 700, 800);
            } catch (IOException e) {
                showError("Erreur de navigation", "Impossible de charger la vue Configuration");
                e.printStackTrace();
            }
        });
    }

    /**
     * Affiche une alerte d'erreur.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}