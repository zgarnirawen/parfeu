package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Rawen.persistence.StorageManager;
import com.mycompany.parfeu.Model.Rawen.exception.DatabaseException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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
    @FXML private Button saveConfigBtn;
    @FXML private Button loadConfigBtn;
    @FXML private Button exportConfigBtn;
    @FXML private Button resetConfigBtn;

    private FirewallConfig config;
    private StorageManager storageManager;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            storageManager = new StorageManager();
            
            // 🔥 Charger la config existante OU créer une nouvelle
            loadOrCreateConfiguration();
            
            // Initialiser les composants
            initializeComponents();
            
            // Appliquer la config à l'UI
            applyConfigToUI();
            
            // Setup boutons
            setupButtonActions();
            
            System.out.println("✓ ConfigurationController initialisé");
        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation: " + e.getMessage());
            e.printStackTrace();
            config = new FirewallConfig(); // Fallback
        }
    }

    /**
     * 🔥 Charge la config depuis le fichier OU crée une nouvelle
     */
    private void loadOrCreateConfiguration() {
        try {
            if (storageManager.configExists()) {
                config = storageManager.loadConfiguration();
                System.out.println("✓ Configuration chargée depuis le fichier");
            } else {
                config = new FirewallConfig();
                System.out.println("ℹ Nouvelle configuration créée");
            }
        } catch (DatabaseException e) {
            System.err.println("⚠ Erreur chargement config: " + e.getMessage());
            config = new FirewallConfig();
        }
    }

    private void initializeComponents() {
        // 🔥 CORRECTION: Spinners avec valeurs par défaut
        if (blockThresholdSpinner != null) {
            SpinnerValueFactory<Integer> blockFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, config.getBlockThreshold());
            blockThresholdSpinner.setValueFactory(blockFactory);
            blockThresholdSpinner.setEditable(true);
        }
        
        if (alertThresholdSpinner != null) {
            SpinnerValueFactory<Integer> alertFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, config.getAlertThreshold());
            alertThresholdSpinner.setValueFactory(alertFactory);
            alertThresholdSpinner.setEditable(true);
        }
    }

    private void applyConfigToUI() {
        // Spinners (déjà configurés dans initializeComponents)
        
        // TextFields
        if (minPacketSizeField != null) {
            minPacketSizeField.setText(String.valueOf(config.getMinPacketSize()));
        }
        
        if (maxPacketSizeField != null) {
            maxPacketSizeField.setText(String.valueOf(config.getMaxPacketSize()));
        }

        // ListViews
        if (suspiciousWordsList != null) {
            suspiciousWordsList.getItems().clear();
            suspiciousWordsList.getItems().addAll(config.getSuspiciousWords());
        }
        
        if (blacklistedIPsList != null) {
            blacklistedIPsList.getItems().clear();
            blacklistedIPsList.getItems().addAll(config.getBlacklistedIPs());
        }
        
        if (monitoredPortsList != null) {
            monitoredPortsList.getItems().clear();
            monitoredPortsList.getItems().addAll(config.getMonitoredPorts());
        }
    }

    private void setupButtonActions() {
        // Back
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Erreur", "Impossible de retourner au menu");
                }
            });
        }

        // 🔥 SAVE - Sauvegarde sur disque
        if (saveConfigBtn != null) {
            saveConfigBtn.setOnAction(event -> {
                try {
                    // Capturer les valeurs des spinners
                    config.setBlockThreshold(blockThresholdSpinner.getValue());
                    config.setAlertThreshold(alertThresholdSpinner.getValue());
                    
                    // Capturer les TextFields
                    config.setMinPacketSize(Integer.parseInt(minPacketSizeField.getText()));
                    config.setMaxPacketSize(Integer.parseInt(maxPacketSizeField.getText()));
                    
                    // Sauvegarder
                    storageManager.saveConfiguration(config);
                    showSuccess("Sauvegardé", "Configuration sauvegardée avec succès!");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de sauvegarder: " + e.getMessage());
                }
            });
        }

        // Load
        if (loadConfigBtn != null) {
            loadConfigBtn.setOnAction(event -> {
                loadOrCreateConfiguration();
                applyConfigToUI();
                showSuccess("Chargé", "Configuration rechargée!");
            });
        }

        // Export
        if (exportConfigBtn != null) {
            exportConfigBtn.setOnAction(event -> exportConfiguration());
        }

        // Reset
        if (resetConfigBtn != null) {
            resetConfigBtn.setOnAction(event -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation");
                confirmation.setHeaderText("Réinitialiser la configuration");
                confirmation.setContentText("Voulez-vous vraiment réinitialiser?");
                
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        config = new FirewallConfig();
                        applyConfigToUI();
                        showSuccess("Réinitialisé", "Configuration par défaut restaurée");
                    }
                });
            });
        }

        setupWordButtons();
        setupIPButtons();
        setupPortButtons();
    }

    private void setupWordButtons() {
        if (addWordButton != null) {
            addWordButton.setOnAction(event -> {
                String word = newSuspiciousWordField.getText().trim();
                if (!word.isEmpty() && !suspiciousWordsList.getItems().contains(word)) {
                    suspiciousWordsList.getItems().add(word);
                    config.addSuspiciousWord(word);
                    newSuspiciousWordField.clear();
                    showInfo("Ajouté", "Le mot '" + word + "' a été ajouté");
                }
            });
        }

        if (removeWordButton != null) {
            removeWordButton.setOnAction(event -> {
                String selected = suspiciousWordsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    suspiciousWordsList.getItems().remove(selected);
                    config.removeSuspiciousWord(selected);
                    showInfo("Supprimé", "Le mot '" + selected + "' a été retiré");
                }
            });
        }
    }

    private void setupIPButtons() {
        if (addIPButton != null) {
            addIPButton.setOnAction(event -> {
                String ip = newIPField.getText().trim();
                if (isValidIP(ip) && !blacklistedIPsList.getItems().contains(ip)) {
                    blacklistedIPsList.getItems().add(ip);
                    config.addBlacklistedIP(ip);
                    newIPField.clear();
                    showInfo("IP Ajoutée", "L'IP " + ip + " a été blacklistée");
                } else if (!isValidIP(ip)) {
                    showError("IP invalide", "Format invalide (ex: 192.168.1.1)");
                }
            });
        }

        if (removeIPButton != null) {
            removeIPButton.setOnAction(event -> {
                String selected = blacklistedIPsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    blacklistedIPsList.getItems().remove(selected);
                    config.removeBlacklistedIP(selected);
                    showInfo("IP Retirée", "L'IP " + selected + " a été retirée");
                }
            });
        }
    }

    private void setupPortButtons() {
        if (addPortButton != null) {
            addPortButton.setOnAction(event -> {
                try {
                    int port = Integer.parseInt(newPortField.getText().trim());
                    if (port >= 0 && port <= 65535 && !monitoredPortsList.getItems().contains(port)) {
                        monitoredPortsList.getItems().add(port);
                        config.addMonitoredPort(port);
                        newPortField.clear();
                        showInfo("Port Ajouté", "Le port " + port + " est surveillé");
                    } else {
                        showError("Port invalide", "Port doit être entre 0 et 65535");
                    }
                } catch (NumberFormatException e) {
                    showError("Erreur", "Veuillez entrer un nombre valide");
                }
            });
        }

        if (removePortButton != null) {
            removePortButton.setOnAction(event -> {
                Integer selected = monitoredPortsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    monitoredPortsList.getItems().remove(selected);
                    config.removeMonitoredPort(selected);
                    showInfo("Port Retiré", "Le port " + selected + " n'est plus surveillé");
                }
            });
        }
    }

    private void exportConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la Configuration");
        fileChooser.setInitialFileName("firewall_config_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showSaveDialog(exportConfigBtn.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("═══════════════════════════════════════════════════\n");
                writer.write("         CONFIGURATION DU PARE-FEU\n");
                writer.write("         Exporté le: " + LocalDateTime.now().format(formatter) + "\n");
                writer.write("═══════════════════════════════════════════════════\n\n");
                
                writer.write("SEUILS:\n");
                writer.write("  Blocage : " + config.getBlockThreshold() + "\n");
                writer.write("  Alerte  : " + config.getAlertThreshold() + "\n\n");
                
                writer.write("TAILLES:\n");
                writer.write("  Min : " + config.getMinPacketSize() + " bytes\n");
                writer.write("  Max : " + config.getMaxPacketSize() + " bytes\n\n");
                
                writer.write("MOTS SUSPECTS (" + config.getSuspiciousWords().size() + "):\n");
                config.getSuspiciousWords().forEach(w -> {
                    try { writer.write("  • " + w + "\n"); } catch (IOException e) {}
                });
                
                writer.write("\nIPs BLACKLISTÉES (" + config.getBlacklistedIPs().size() + "):\n");
                config.getBlacklistedIPs().forEach(ip -> {
                    try { writer.write("  • " + ip + "\n"); } catch (IOException e) {}
                });
                
                writer.write("\nPORTS SURVEILLÉS (" + config.getMonitoredPorts().size() + "):\n");
                config.getMonitoredPorts().forEach(p -> {
                    try { writer.write("  • " + p + "\n"); } catch (IOException e) {}
                });
                
                showSuccess("Exporté", "Configuration exportée vers:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Erreur d'export", "Impossible d'exporter: " + e.getMessage());
            }
        }
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
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

    private void showSuccess(String title, String content) {
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