package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PacketInput;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.analyzer.PacketAnalyzer;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.persistence.StorageManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
    @FXML private Button analyzeButton;
    @FXML private Button saveButton;
    @FXML private Button backBtn;
    @FXML private TextArea packetDisplayArea;
    @FXML private TextArea analysisResultArea;

    private FirewallConfig config;
    private PacketAnalyzer analyzer;
    private DecisionEngine decisionEngine;
    private StorageManager storageManager;
    private Packet currentPacket;
    private DecisionResult currentDecision;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            config = new FirewallConfig();
            analyzer = new PacketAnalyzer(
                config.getMinPacketSize(),
                config.getMaxPacketSize(),
                config.getSuspiciousWords()
            );
            decisionEngine = new DecisionEngine(config);
            storageManager = new StorageManager();
            
            setupUI();
            setupActions();
            
            System.out.println("✓ GuidedPacketInputController initialisé");
        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
            e.printStackTrace();
        }
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

        // Désactiver les boutons au départ
        analyzeButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void setupActions() {
        // Bouton Générer
        generateButton.setOnAction(event -> generatePacket());

        // Bouton Analyser
        analyzeButton.setOnAction(event -> analyzePacket());

        // Bouton Sauvegarder
        saveButton.setOnAction(event -> saveToBlockchain());

        // Bouton Retour
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Erreur de navigation", "Impossible de retourner au menu principal");
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

            // Afficher le paquet
            displayPacket(currentPacket);
            
            // Activer le bouton Analyser
            analyzeButton.setDisable(false);
            analysisResultArea.clear();
            saveButton.setDisable(true);
            
            showInfo("Paquet généré", "Le paquet a été créé avec succès. Cliquez sur 'Analyser' pour l'examiner.");

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
        
        packetDisplayArea.setText(sb.toString());
    }

    /**
     * Analyse le paquet généré.
     */
    private void analyzePacket() {
        if (currentPacket == null) {
            showWarning("Aucun paquet", "Veuillez d'abord générer un paquet");
            return;
        }

        try {
            // Analyse
            List<DetectionSignal> signals = analyzer.analyze(currentPacket);
            currentDecision = decisionEngine.decide(currentPacket, signals);

            // Afficher les résultats
            displayAnalysisResult(currentDecision);

            // Activer le bouton Sauvegarder
            saveButton.setDisable(false);

        } catch (Exception e) {
            showError("Erreur d'analyse", "Impossible d'analyser le paquet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les résultats de l'analyse.
     */
    private void displayAnalysisResult(DecisionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append("║            🔍 RÉSULTAT DE L'ANALYSE                       ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        String actionSymbol = result.getAction().getSymbol();
        String actionEmoji = getActionEmoji(actionSymbol);
        sb.append("⚖️  DÉCISION : ").append(actionEmoji).append(" ").append(actionSymbol).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        sb.append("📊 ÉVALUATION\n");
        sb.append(String.format("  Score de menace : %d/10\n", result.getTotalScore()));
        sb.append(String.format("  Niveau de risque: %s\n\n", 
            decisionEngine.evaluateRiskLevel(result.getTotalScore())));
        
        sb.append("📋 RAISON\n");
        sb.append("  ").append(result.getReason()).append("\n\n");
        
        if (!result.getSignals().isEmpty()) {
            sb.append("⚠️  SIGNAUX DÉTECTÉS (").append(result.getSignals().size()).append(")\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (int i = 0; i < result.getSignals().size(); i++) {
                DetectionSignal signal = result.getSignals().get(i);
                sb.append(String.format("  %d. %s\n", i + 1, signal.getDescription()));
                sb.append(String.format("     └─ Score: %d | Niveau: %s\n\n", 
                    signal.getScore(), signal.getThreatLevel()));
            }
        } else {
            sb.append("✅ AUCUN SIGNAL DE MENACE\n");
        }
        
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        analysisResultArea.setText(sb.toString());
    }

    /**
     * Sauvegarde la décision dans la blockchain et l'historique.
     */
    private void saveToBlockchain() {
        if (currentDecision == null) {
            showWarning("Aucune décision", "Veuillez d'abord analyser le paquet");
            return;
        }

        try {
            // Créer un bloc avec la décision
            Block newBlock = new Block(
                getNextBlockIndex(),
                List.of(currentDecision),
                getLastBlockHash()
            );

            // Sauvegarder dans l'historique
            storageManager.saveBlockToHistory(newBlock);

            showInfo("Sauvegardé", 
                "La décision a été sauvegardée dans l'historique blockchain\n" +
                "Bloc #" + newBlock.index() + " créé avec succès");

            // Réinitialiser pour un nouveau paquet
            resetForm();

        } catch (Exception e) {
            showError("Erreur de sauvegarde", "Impossible de sauvegarder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getNextBlockIndex() {
        try {
            return (int) storageManager.countBlocks() + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String getLastBlockHash() {
        try {
            List<String> history = storageManager.loadBlockHistory();
            if (history.isEmpty()) {
                return "0"; // Genesis
            }
            String lastLine = history.get(history.size() - 1);
            String[] parts = lastLine.split(",");
            return parts[parts.length - 1]; // Dernier champ = hash
        } catch (Exception e) {
            return "0";
        }
    }

    private void resetForm() {
        currentPacket = null;
        currentDecision = null;
        packetDisplayArea.clear();
        analysisResultArea.clear();
        payloadArea.clear();
        analyzeButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private String getActionEmoji(String action) {
        return switch (action) {
            case "[BLOCK]" -> "🚫";
            case "[ALERT]" -> "⚠️";
            case "[LOG]" -> "📝";
            case "[OK]" -> "✅";
            default -> "❓";
        };
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