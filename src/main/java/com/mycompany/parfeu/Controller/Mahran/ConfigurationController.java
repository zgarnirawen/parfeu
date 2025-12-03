package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la configuration du pare-feu.
 */
public class ConfigurationController implements Initializable {

    @FXML private Spinner<Integer> blockThresholdSpinner;
    @FXML private Spinner<Integer> alertThresholdSpinner;
    @FXML private TextField minPacketSizeField;
    @FXML private TextField maxPacketSizeField;
    @FXML private ListView<String> suspiciousWordsList;
    @FXML private TextField newSuspiciousWordField;
    @FXML private Button addWordButton;
    @FXML private Button removeWordButton;
    @FXML private ListView<String> blacklistedIPsList;
    @FXML private TextField newIPField;
    @FXML private Button addIPButton;
    @FXML private Button removeIPButton;
    @FXML private ListView<Integer> monitoredPortsList;
    @FXML private TextField newPortField;
    @FXML private Button addPortButton;
    @FXML private Button removePortButton;
    @FXML private Button backBtn;

    private FirewallConfig config;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        config = new FirewallConfig();
        loadConfiguration();
        setupButtonActions();
        System.out.println("✓ ConfigurationController initialisé");
    }

    private void loadConfiguration() {
        // Charger les spinners
        blockThresholdSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, config.getBlockThreshold())
        );
        alertThresholdSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, config.getAlertThreshold())
        );

        // Charger les champs de taille
        minPacketSizeField.setText(String.valueOf(config.getMinPacketSize()));
        maxPacketSizeField.setText(String.valueOf(config.getMaxPacketSize()));

        // Charger les listes
        suspiciousWordsList.getItems().addAll(config.getSuspiciousWords());
        blacklistedIPsList.getItems().addAll(config.getBlacklistedIPs());
        monitoredPortsList.getItems().addAll(config.getMonitoredPorts());
    }

    private void setupButtonActions() {
        // Bouton Back
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Erreur de navigation", "Impossible de retourner au menu principal");
                    e.printStackTrace();
                }
            });
        }

        // Mots suspects
        addWordButton.setOnAction(event -> {
            String word = newSuspiciousWordField.getText().trim();
            if (!word.isEmpty() && !suspiciousWordsList.getItems().contains(word)) {
                suspiciousWordsList.getItems().add(word);
                config.addSuspiciousWord(word);
                newSuspiciousWordField.clear();
                showInfo("Ajouté", "Le mot '" + word + "' a été ajouté à la liste");
            }
        });

        removeWordButton.setOnAction(event -> {
            String selected = suspiciousWordsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                suspiciousWordsList.getItems().remove(selected);
                config.removeSuspiciousWord(selected);
                showInfo("Supprimé", "Le mot '" + selected + "' a été retiré");
            }
        });

        // IPs blacklistées
        addIPButton.setOnAction(event -> {
            String ip = newIPField.getText().trim();
            if (isValidIP(ip) && !blacklistedIPsList.getItems().contains(ip)) {
                blacklistedIPsList.getItems().add(ip);
                config.addBlacklistedIP(ip);
                newIPField.clear();
                showInfo("IP Ajoutée", "L'adresse IP " + ip + " a été blacklistée");
            } else {
                showError("IP invalide", "Veuillez entrer une adresse IP valide (ex: 192.168.1.1)");
            }
        });

        removeIPButton.setOnAction(event -> {
            String selected = blacklistedIPsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                blacklistedIPsList.getItems().remove(selected);
                config.removeBlacklistedIP(selected);
                showInfo("IP Retirée", "L'adresse IP " + selected + " a été retirée de la blacklist");
            }
        });

        // Ports surveillés
        addPortButton.setOnAction(event -> {
            try {
                int port = Integer.parseInt(newPortField.getText().trim());
                if (port >= 0 && port <= 65535 && !monitoredPortsList.getItems().contains(port)) {
                    monitoredPortsList.getItems().add(port);
                    config.addMonitoredPort(port);
                    newPortField.clear();
                    showInfo("Port Ajouté", "Le port " + port + " est maintenant surveillé");
                } else {
                    showError("Port invalide", "Le port doit être entre 0 et 65535 et ne pas être déjà dans la liste");
                }
            } catch (NumberFormatException e) {
                showError("Erreur de format", "Veuillez entrer un nombre valide");
            }
        });

        removePortButton.setOnAction(event -> {
            Integer selected = monitoredPortsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                monitoredPortsList.getItems().remove(selected);
                config.removeMonitoredPort(selected);
                showInfo("Port Retiré", "Le port " + selected + " n'est plus surveillé");
            }
        });

        // Listener pour les spinners
        blockThresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                config.setBlockThreshold(newVal);
                System.out.println("Seuil de blocage mis à jour: " + newVal);
            }
        });

        alertThresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                config.setAlertThreshold(newVal);
                System.out.println("Seuil d'alerte mis à jour: " + newVal);
            }
        });
    }

    private boolean isValidIP(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public FirewallConfig getConfig() {
        return config;
    }
}