package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Rawen.persistence.SharedDataManager;
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

/**
 * Contrôleur pour la configuration du pare-feu - Version avec persistance
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
    @FXML private Button saveConfigBtn;
    @FXML private Button loadConfigBtn;
    @FXML private Button exportConfigBtn;
    @FXML private Button resetConfigBtn;

    private FirewallConfig config;
    private SharedDataManager sharedData;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            System.out.println("\n⚙️  ========== CONFIGURATION CONTROLLER INIT ==========");
            
            // 🔥 UTILISER LE SHARED DATA MANAGER
            sharedData = SharedDataManager.getInstance();
            
            // Initialiser les composants
            initializeComponents();
            
            // 🔥 CHARGER LA CONFIGURATION DEPUIS LE FICHIER
            loadConfiguration();
            
            // Configurer les boutons
            setupButtonActions();
            
            System.out.println("✅ ConfigurationController initialisé avec configuration chargée");
            System.out.println("================================================\n");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        if (blockThresholdSpinner != null) {
            blockThresholdSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3)
            );
        }
        
        if (alertThresholdSpinner != null) {
            alertThresholdSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2)
            );
        }
    }

    /**
     * 🔥 CHARGEMENT DE LA CONFIGURATION depuis SharedDataManager
     */
    private void loadConfiguration() {
        try {
            System.out.println("📋 Chargement de la configuration...");
            
            // Charger depuis SharedDataManager (qui a déjà chargé depuis le fichier)
            config = sharedData.getConfiguration();
            
            if (config == null) {
                System.out.println("  ⚠️  Configuration non trouvée, création par défaut");
                config = new FirewallConfig();
            } else {
                System.out.println("  ✓ Configuration chargée:");
                System.out.println("    - Seuil blocage: " + config.getBlockThreshold());
                System.out.println("    - Seuil alerte: " + config.getAlertThreshold());
                System.out.println("    - Min size: " + config.getMinPacketSize());
                System.out.println("    - Max size: " + config.getMaxPacketSize());
                System.out.println("    - Mots suspects: " + config.getSuspiciousWords().size());
                System.out.println("    - IPs blacklistées: " + config.getBlacklistedIPs().size());
                System.out.println("    - Ports surveillés: " + config.getMonitoredPorts().size());
            }
        } catch (Exception e) {
            System.out.println("  ⚠️  Erreur de chargement, utilisation des valeurs par défaut");
            config = new FirewallConfig();
        }
        
        // Appliquer la configuration à l'interface
        applyConfigToUI();
    }

    /**
     * Applique la configuration chargée à l'interface
     */
    private void applyConfigToUI() {
        System.out.println("🎨 Application de la configuration à l'interface...");
        
        if (blockThresholdSpinner != null) {
            blockThresholdSpinner.getValueFactory().setValue(config.getBlockThreshold());
        }
        
        if (alertThresholdSpinner != null) {
            alertThresholdSpinner.getValueFactory().setValue(config.getAlertThreshold());
        }

        if (minPacketSizeField != null) {
            minPacketSizeField.setText(String.valueOf(config.getMinPacketSize()));
        }
        
        if (maxPacketSizeField != null) {
            maxPacketSizeField.setText(String.valueOf(config.getMaxPacketSize()));
        }

        if (suspiciousWordsList != null) {
            suspiciousWordsList.getItems().clear();
            suspiciousWordsList.getItems().addAll(config.getSuspiciousWords());
            System.out.println("  ✓ " + config.getSuspiciousWords().size() + " mots suspects chargés");
        }
        
        if (blacklistedIPsList != null) {
            blacklistedIPsList.getItems().clear();
            blacklistedIPsList.getItems().addAll(config.getBlacklistedIPs());
            System.out.println("  ✓ " + config.getBlacklistedIPs().size() + " IPs blacklistées chargées");
        }
        
        if (monitoredPortsList != null) {
            monitoredPortsList.getItems().clear();
            monitoredPortsList.getItems().addAll(config.getMonitoredPorts());
            System.out.println("  ✓ " + config.getMonitoredPorts().size() + " ports surveillés chargés");
        }
        
        System.out.println("✓ Interface mise à jour avec la configuration");
    }

    private void setupButtonActions() {
        // Bouton Back
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Erreur", "Impossible de retourner au menu");
                }
            });
        }

        // Bouton Save Configuration
        if (saveConfigBtn != null) {
            saveConfigBtn.setOnAction(event -> saveConfiguration());
        }

        // Bouton Load Configuration
        if (loadConfigBtn != null) {
            loadConfigBtn.setOnAction(event -> reloadConfiguration());
        }

        // Bouton Export Configuration
        if (exportConfigBtn != null) {
            exportConfigBtn.setOnAction(event -> exportConfiguration());
        }

        // Bouton Reset Configuration
        if (resetConfigBtn != null) {
            resetConfigBtn.setOnAction(event -> resetConfiguration());
        }

        setupWordButtons();
        setupIPButtons();
        setupPortButtons();
        setupSpinnerListeners();
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

    private void setupSpinnerListeners() {
        if (blockThresholdSpinner != null) {
            blockThresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        config.setBlockThreshold(newVal);
                        System.out.println("Seuil de blocage: " + newVal);
                    } catch (IllegalArgumentException e) {
                        showError("Valeur invalide", e.getMessage());
                        blockThresholdSpinner.getValueFactory().setValue(oldVal);
                    }
                }
            });
        }

        if (alertThresholdSpinner != null) {
            alertThresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        config.setAlertThreshold(newVal);
                        System.out.println("Seuil d'alerte: " + newVal);
                    } catch (IllegalArgumentException e) {
                        showError("Valeur invalide", e.getMessage());
                        alertThresholdSpinner.getValueFactory().setValue(oldVal);
                    }
                }
            });
        }
    }

    /**
     * 🔥 SAUVEGARDE via SharedDataManager
     */
    private void saveConfiguration() {
        try {
            System.out.println("\n💾 Sauvegarde de la configuration...");
            
            // Sauvegarder via SharedDataManager
            sharedData.saveConfiguration(config);
            
            showSuccess("Sauvegardé", 
                "Configuration sauvegardée avec succès!\n\n" +
                "Seuil blocage: " + config.getBlockThreshold() + "\n" +
                "Seuil alerte: " + config.getAlertThreshold() + "\n" +
                "Mots suspects: " + config.getSuspiciousWords().size() + "\n" +
                "IPs blacklistées: " + config.getBlacklistedIPs().size());
            
            System.out.println("✓ Configuration sauvegardée");
        } catch (Exception e) {
            showError("Erreur", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    private void reloadConfiguration() {
        try {
            System.out.println("\n🔄 Rechargement de la configuration...");
            loadConfiguration();
            showSuccess("Chargé", "Configuration rechargée depuis le fichier!");
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger: " + e.getMessage());
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
                writer.write("═══════════════════════════════════════════════════════════\n");
                writer.write("         CONFIGURATION DU PARE-FEU\n");
                writer.write("         Exporté le: " + LocalDateTime.now().format(formatter) + "\n");
                writer.write("═══════════════════════════════════════════════════════════\n\n");
                
                writer.write("📊 SEUILS DE DÉCISION\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                writer.write("Seuil de blocage  : " + config.getBlockThreshold() + "\n");
                writer.write("Seuil d'alerte    : " + config.getAlertThreshold() + "\n\n");
                
                writer.write("📦 LIMITES DE TAILLE\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                writer.write("Taille minimale   : " + config.getMinPacketSize() + " bytes\n");
                writer.write("Taille maximale   : " + config.getMaxPacketSize() + " bytes\n\n");
                
                writer.write("🔍 MOTS SUSPECTS (" + config.getSuspiciousWords().size() + ")\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                for (String word : config.getSuspiciousWords()) {
                    writer.write("  • " + word + "\n");
                }
                writer.write("\n");
                
                writer.write("🚫 IPs BLACKLISTÉES (" + config.getBlacklistedIPs().size() + ")\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                for (String ip : config.getBlacklistedIPs()) {
                    writer.write("  • " + ip + "\n");
                }
                writer.write("\n");
                
                writer.write("🔌 PORTS SURVEILLÉS (" + config.getMonitoredPorts().size() + ")\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                for (Integer port : config.getMonitoredPorts()) {
                    writer.write("  • " + port + "\n");
                }
                
                writer.write("\n═══════════════════════════════════════════════════════════\n");
                
                showSuccess("Exporté", "Configuration exportée vers:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Erreur d'export", "Impossible d'exporter: " + e.getMessage());
            }
        }
    }

    private void resetConfiguration() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Réinitialiser la configuration");
        confirmation.setContentText("Voulez-vous vraiment réinitialiser la configuration aux valeurs par défaut?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                config = new FirewallConfig();
                applyConfigToUI();
                showSuccess("Réinitialisé", "Configuration réinitialisée aux valeurs par défaut");
            }
        });
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