package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PacketInput;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la saisie guidée de paquets par l'utilisateur.
 */
public class GuidedPacketInputController implements Initializable {

    @FXML private TextField srcIPField;
    @FXML private TextField destIPField;
    @FXML private Spinner<Integer> srcPortSpinner;
    @FXML private Spinner<Integer> destPortSpinner;
    @FXML private ComboBox<String> protocolCombo;
    @FXML private TextArea payloadArea;
    @FXML private CheckBox maliciousCheckbox;
    @FXML private ComboBox<String> attackTypeCombo;
    @FXML private Button generateButton;
    @FXML private Button backBtn;
    @FXML private Button initialFilterBtn;
    @FXML private TextArea packetDisplayArea;

    // Variable pour stocker le paquet courant
    private Packet currentPacket = null;
    
    // Variable statique pour partager le paquet entre les contrôleurs
    private static Packet sharedPacket = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        setupActions();
        System.out.println("✓ GuidedPacketInputController initialisé");
    }

    private void setupUI() {
        // Spinner pour les ports
        srcPortSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 54321)
        );
        destPortSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 80)
        );

        // Protocoles communs
        protocolCombo.getItems().addAll(
            "HTTP", "HTTPS", "TCP", "UDP", "FTP", "SSH", 
            "SMTP", "DNS", "ICMP", "TELNET"
        );
        protocolCombo.setValue("HTTP");

        // Types d'attaques
        attackTypeCombo.getItems().addAll(
            "SQL_INJECTION", "XSS", "PATH_TRAVERSAL", 
            "COMMAND_INJECTION", "PORT_SCAN", "DOS", "DDOS"
        );
        attackTypeCombo.setDisable(true);

        // Activer/désactiver le type d'attaque selon le checkbox
        maliciousCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            attackTypeCombo.setDisable(!newVal);
            if (newVal && attackTypeCombo.getValue() == null) {
                attackTypeCombo.setValue("SQL_INJECTION");
            }
        });

        // Désactiver le bouton Initial Filtering au départ
        if (initialFilterBtn != null) {
            initialFilterBtn.setDisable(true);
        }
    }

    private void setupActions() {
        // Bouton Générer
        if (generateButton != null) {
            generateButton.setOnAction(event -> generatePacket());
        }

        // Bouton Initial Filtering
        if (initialFilterBtn != null) {
            initialFilterBtn.setOnAction(event -> navigateToInitialFiltering());
        }

        // Bouton Retour
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
    }

    /**
     * Génère le paquet à partir des inputs utilisateur.
     */
    private void generatePacket() {
        try {
            // Validation basique
            String srcIP = srcIPField.getText().trim();
            String destIP = destIPField.getText().trim();
            
            if (srcIP.isEmpty() || destIP.isEmpty()) {
                showWarning("Champs requis", "Veuillez remplir les adresses IP source et destination");
                return;
            }

            // Créer le PacketInput
            PacketInput.Builder builder = PacketInput.builder()
                .srcIP(srcIP)
                .destIP(destIP)
                .srcPort(srcPortSpinner.getValue())
                .destPort(destPortSpinner.getValue())
                .protocol(protocolCombo.getValue())
                .payload(payloadArea.getText());

            if (maliciousCheckbox.isSelected()) {
                builder.attackType(attackTypeCombo.getValue());
            }

            PacketInput input = builder.build();
            currentPacket = input.toPacket();

            // Sauvegarder le paquet pour le partager avec les autres vues
            sharedPacket = currentPacket;

            // Afficher le paquet
            displayPacket(currentPacket);
            
            // Activer le bouton Initial Filtering
            if (initialFilterBtn != null) {
                initialFilterBtn.setDisable(false);
            }
            
            showInfo("Paquet généré", "Le paquet a été créé avec succès. Cliquez sur 'Initial Filtering' pour commencer l'analyse.");

        } catch (IllegalArgumentException e) {
            showError("Erreur de validation", e.getMessage());
        } catch (Exception e) {
            showError("Erreur", "Impossible de générer le paquet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les informations du paquet.
     */
    private void displayPacket(Packet packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append("║              PAQUET GÉNÉRÉ                                ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("🌐 INFORMATIONS RÉSEAU\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("  Source      : %s:%d\n", packet.getSrcIP(), packet.getSrcPort()));
        sb.append(String.format("  Destination : %s:%d\n", packet.getDestIP(), packet.getDestPort()));
        sb.append(String.format("  Protocole   : %s\n", packet.getProtocol()));
        sb.append(String.format("  Taille      : %d bytes\n", packet.getSize()));
        sb.append(String.format("  Timestamp   : %s\n\n", packet.getTimestamp()));
        
        sb.append("📝 CONTENU (PAYLOAD)\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(packet.getPayload()).append("\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if (packetDisplayArea != null) {
            packetDisplayArea.setText(sb.toString());
        }
    }

    /**
     * Navigation vers Initial Filtering.
     */
    private void navigateToInitialFiltering() {
        if (currentPacket == null) {
            showWarning("Aucun paquet", "Veuillez d'abord générer un paquet");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/mycompany/parfeu/Views/Mahran/initial_filtering.fxml")
            );
            Parent root = loader.load();

            // Passer le paquet au contrôleur de filtrage
            InitialFilteringController controller = loader.getController();
            controller.setPacket(currentPacket);

            // Charger la nouvelle scène
            Scene scene = new Scene(root, 900, 800);
            Stage stage = (Stage) initialFilterBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger la vue Initial Filtering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode statique pour récupérer le paquet partagé
    public static Packet getSharedPacket() {
        return sharedPacket;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
}